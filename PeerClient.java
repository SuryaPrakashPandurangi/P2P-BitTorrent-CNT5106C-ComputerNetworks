
import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerClient extends Thread {

	Socket requestSocket;
	BufferedOutputStream bufferedOutputStream;
	BufferedInputStream bufferedInputStream;

	boolean isPeerClient;
	String peerID;
	byte[] bitFieldClient;


	boolean isRemotePeerClientInterested = true;
	boolean isRemotePeerClientChoked = true;
	AtomicBoolean peerProcessKillCommand = new AtomicBoolean(false);
	Float downloadSpeed = 1.0f;


	PeerDataConfig peerDataConfig;
	PeerMessageReader messageReader;
	MessageConstructorUtil messageUtil;


	public PeerClient(Socket clientSocket, boolean isRemotePeerClient, String peerID, PeerDataConfig peerDataCfg) {

		this.peerDataConfig = peerDataCfg;
		this.requestSocket = clientSocket;
		this.isPeerClient = isRemotePeerClient;

		messageUtil = new MessageConstructorUtil();
		messageReader = new PeerMessageReader();

		try {
			bufferedOutputStream = new BufferedOutputStream(requestSocket.getOutputStream());
			bufferedOutputStream.flush();
			bufferedInputStream = new BufferedInputStream(requestSocket.getInputStream());

			if (isRemotePeerClient) {
				initializeRemoteP2PClient(peerID);
			} else {
				initializeServer();
			}

			startDataCommunication();

		} catch (IOException ex) {
			ex.printStackTrace();
			peerProcess.peerDataLogger.info("Exception Occured while initializing Client: " + ex.getMessage());
		}
	}

	public void run() {
		Instant instant = Instant.now();
		long startEpoch = instant.getEpochSecond();

		try {
			long timeForprocessing = 0l;
			long overallTimeTaken = 0l;

			byte[] byteMessageLength, byteMessageType;
			int requestedIndex = 0;
			int noOfPiecesReceived = 0;

			byteMessageType = new byte[1];
			byteMessageLength = new byte[4];

			while (!peerProcessKillCommand.get()) {

				bufferedInputStream.read(byteMessageLength);
				bufferedInputStream.read(byteMessageType);
				int byteMessageInt = new BigInteger(byteMessageType).intValue();
				PeerCommunicationMessageType peerMessageType = PeerCommunicationMessageType.values()[byteMessageInt];

				switch (peerMessageType) {
					case INTERESTED:
						fetchAndgandleInterestedMsg();
						break;
					case REQUEST:
						fetchAndHandleRequestMessage();
						break;
					case HAVE:
						fetchAndHandleHaveMessage();
						break;
					case NOTINTERESTED:
						fetchAndHandleNotInterestedMessage();
						break;
					case CHOKE:
						peerProcess.peerDataLogger.info("Peer: " + peerProcess.peerProcessID + " is Choked by Peer: " + peerID);
						fetchAndProcessChokeMsg(requestedIndex);
						break;
					case UNCHOKE:
						peerProcess.peerDataLogger.info("Peer: " + peerProcess.peerProcessID + " is unchoked by Peer:" + peerID);
						int pieceIndexUnchoke = messageReader.requestPieceIndex(peerProcess.bitFieldData, bitFieldClient, peerProcess.noOfPiecesRequested);
						if (pieceIndexUnchoke >= 0) {
							requestedIndex = pieceIndexUnchoke;
							peerProcess.noOfPiecesRequested[pieceIndexUnchoke].set(true);
							transmitMessage(messageUtil.fetchAndConstructRequestMessage(pieceIndexUnchoke));
							timeForprocessing = System.nanoTime();
						}
						break;

					case PIECE:
						byte[] indexPiece = new byte[4];
						bufferedInputStream.read(indexPiece);

						int integerIndex = ByteIOUtil.byteArrayToInteger(indexPiece);

						int msgLenPeer = ByteIOUtil.byteArrayToInteger(byteMessageLength);

						byte[] pieceDataPayload = messageReader.fetchAndReadMessagePayload(bufferedInputStream, msgLenPeer - 5);

						peerProcess.bitFieldData[integerIndex / 8] |= 1 << (7 - (integerIndex % 8));

						int startingIndex = integerIndex * PeerDataConfig.transferPieceSize;
						for (int i = 0; i < pieceDataPayload.length; i++) {
							peerProcess.resourceDataPayload[startingIndex + i] = pieceDataPayload[i];
						}
						noOfPiecesReceived++;
						peerProcess.peerDataLogger.info("Peer: " + peerProcess.peerProcessID + " has downloaded the piece " + integerIndex + " from Peer: " + peerID + ". Now the number of pieces it has is : " + noOfPiecesReceived);

						overallTimeTaken += System.nanoTime() - timeForprocessing;
						downloadSpeed = (float) ((noOfPiecesReceived * PeerDataConfig.transferPieceSize) / overallTimeTaken);

						publishHaveMessageAcrossPeers(indexPiece);

						integerIndex = messageReader.requestPieceIndex(peerProcess.bitFieldData, bitFieldClient, peerProcess.noOfPiecesRequested);

						if (integerIndex >= 0) {
							requestedIndex = integerIndex;
							peerProcess.noOfPiecesRequested[integerIndex].set(true);
							transmitMessage(messageUtil.fetchAndConstructRequestMessage(integerIndex));
							timeForprocessing = System.currentTimeMillis();
						} else {
							transmitNotInterestedMessage();
						}

						break;

					default:
						break;
				}
				Instant instantNew = Instant.now();
				long endEpoch = instantNew.getEpochSecond();

				long diff = endEpoch - startEpoch;

				if(diff > 90){
					System.exit(0);
				}
			}
		}

		catch (IOException ex) {
			System.err.println("Exception while handling resource: "+ex.getMessage());
			//ex.printStackTrace();
		}

	}



	public void transmitMessage(byte[] transmitMsg) {
		try {
			bufferedOutputStream.write(transmitMsg);
			bufferedOutputStream.flush();
		} catch (Exception exception) {
			System.err.println("Error while Transmitting Message");
			exception.printStackTrace();
		}

	}

	public void validateStoppingCondition(boolean stopCommand)  {
		peerProcessKillCommand.set(stopCommand);
		if (peerProcessKillCommand.get()) {
			peerProcess.peerDataLogger.info("Closing Socket");
			closeSocket();
		}
	}

	public void fetchAndHandleNotInterestedMessage() {
		peerProcess.peerDataLogger.info("Peer " + peerProcess.peerProcessID + " received a Not Interested message from Peer: " + peerID);
		isRemotePeerClientInterested = false;
		isRemotePeerClientChoked = true;
	}

	public void fetchAndProcessChokeMsg(int requestedIndex) {
		byte dataAtIndex = peerProcess.bitFieldData[requestedIndex / 8];
		if (((1 << (7 - (requestedIndex % 8))) & dataAtIndex) == 0) {
			peerProcess.noOfPiecesRequested[requestedIndex].set(false);
		}
	}

	public void fetchAndgandleInterestedMsg() {
		peerProcess.peerDataLogger.info("Peer " + peerProcess.peerProcessID + " received an Interested message from " + peerID + ".");
		isRemotePeerClientInterested = true;
	}



	public void initializeRemoteP2PClient(String id) {
		this.peerID = id;
		PeerHandshakeMessage handshakeMsg = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));
		transmitMessage(handshakeMsg.constructHandshakeMessage());
		messageReader.fetchAndReadTCPHandShakeMessage(bufferedInputStream);
		peerProcess.peerDataLogger.info("Peer "+ peerProcess.peerProcessID + "initialized a connection to Peer:" + peerID);
	}

	public void initializeServer() {
		this.peerID = messageReader.fetchAndReadTCPHandShakeMessage(bufferedInputStream);
		PeerHandshakeMessage handshakeMsg = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));
		transmitMessage(handshakeMsg.constructHandshakeMessage());
		peerProcess.peerDataLogger.info("Peer: "+ peerProcess.peerProcessID + "initialized a connection to Peer: " + peerID);
	}

	public void startDataCommunication() {
		peerProcess.peerDataLogger.info("Peer: "+ peerProcess.peerProcessID + "is Successfully connected to Peer: " + peerID);
		transmitMessage(messageUtil.fetchAndConstructBitFieldMessage(peerProcess.bitFieldData));
		bitFieldClient = messageReader.fetchTCPBitFieldPayload(bufferedInputStream);

		if (messageReader.doSendInterestedMessage(peerProcess.bitFieldData, bitFieldClient)) {
			transmitMessage(messageUtil.fetchAndConstructInterestedMessage());
		} else {
			transmitMessage(messageUtil.fetchAndConstructNotInterestedMessage());
		}
	}

	public void transmitNotInterestedMessage() throws IOException {
		transmitMessage(messageUtil.fetchAndConstructNotInterestedMessage());
		if (Arrays.equals(peerProcess.bitFieldData, peerProcess.fullDataResource)) {
			for (PeerClient client : peerProcess.peerClients) {
				client.transmitMessage(client.messageUtil.fetchAndConstructNotInterestedMessage());
			}
			writeDataToFile();
		}
	}



	public void closeSocket() {
		try {
			if (!requestSocket.isClosed())
				requestSocket.close();
		} catch (Exception e) {
			System.err.println("Error while Closing Socket.");
			e.printStackTrace();
		}
	}

	public void writeDataToFile() throws IOException {
		String peerFilePath =  String.valueOf(peerProcess.peerProcessID);
		new File(peerFilePath).mkdir();
		File peerDataFile = new File(peerFilePath + "/" + PeerDataConfig.peerTransferFileName);
		FileOutputStream fileDataStream = new FileOutputStream(peerDataFile);
		fileDataStream.write(peerProcess.resourceDataPayload);
		fileDataStream.close();
		peerProcess.peerDataLogger.info("Peer " + peerProcess.peerProcessID + " has downloaded the complete file.");
	}

	public void publishHaveMessageAcrossPeers(byte[] dataPieceIndex) {
		for (PeerClient client : peerProcess.peerClients) {
			client.transmitMessage(client.messageUtil.fetchAndConstructHaveMessage(dataPieceIndex));
		}
	}

	public void fetchAndHandleHaveMessage() {

		byte[] pieceIndexAsAnArray = messageReader.fetchAndReadMessagePayload(bufferedInputStream, 4);
		int dataPieceAsInt = ByteIOUtil.byteArrayToInteger(pieceIndexAsAnArray);
		peerProcess.peerDataLogger.info("Peer: " + peerProcess.peerProcessID + " received a Have message from Peer: " + peerID + " for the index:" + dataPieceAsInt);

		bitFieldClient[dataPieceAsInt / 8] |= (1 << (7 - (dataPieceAsInt % 8)));
		byte indexDataAsByte = peerProcess.bitFieldData[dataPieceAsInt / 8];

		if(((1 << (7 - (dataPieceAsInt % 8))) & indexDataAsByte) == 0){
			transmitMessage(messageUtil.fetchAndConstructInterestedMessage());
		}else{
			transmitMessage(messageUtil.fetchAndConstructNotInterestedMessage());
		}
	}

	public void fetchAndHandleRequestMessage() {
		byte[] requestMessageBytes = messageReader.fetchAndReadMessagePayload(bufferedInputStream, 4);
		int pieceIndex = ByteIOUtil.byteArrayToInteger(requestMessageBytes);
		peerProcess.peerDataLogger.info("Peer: " + peerProcess.peerProcessID + " received a REQUEST message from Peer: " + peerID + " for the pieceIndex: " + pieceIndex);
		int beginningIndex = pieceIndex * PeerDataConfig.transferPieceSize;

		try {
			byte[] requestMessageData;

			if ((PeerDataConfig.peerTransferFileSize - beginningIndex) < PeerDataConfig.transferPieceSize) {
				requestMessageData = Arrays.copyOfRange(peerProcess.resourceDataPayload, beginningIndex, PeerDataConfig.peerTransferFileSize);
			}else {
				requestMessageData = Arrays.copyOfRange(peerProcess.resourceDataPayload, beginningIndex, beginningIndex + PeerDataConfig.transferPieceSize);
			}

			if (!isRemotePeerClientChoked) {
				transmitMessage(messageUtil.fetchAndConstructPieceMessage(pieceIndex, requestMessageData));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}

	}


}

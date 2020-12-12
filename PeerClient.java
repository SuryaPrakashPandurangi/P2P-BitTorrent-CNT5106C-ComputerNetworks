
import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerClient extends Thread {

	Socket requestSocket;
	BufferedOutputStream ops;
	BufferedInputStream ins;

	boolean isClient;
	String peerID;
	byte[] peerBitField;
	
	
	boolean clientInterested = true;
	boolean isClientChoked = true;
	AtomicBoolean killProcess = new AtomicBoolean(false);
	Float dwnldrate = 1.0f;
	
	
	PeerTorrConfig configObj;
	MessageReader messageReaderObj;
	MessageConstructionUtil msgHelper;
	

	public PeerClient(Socket s, boolean isPeerClient, String peerID, PeerTorrConfig cfg) {
	
		this.configObj = cfg;
		this.requestSocket = s;
		this.isClient = isPeerClient;
		messageReaderObj = new MessageReader();
		msgHelper = new MessageConstructionUtil();
		
		try {
			ops = new BufferedOutputStream(requestSocket.getOutputStream());
			ops.flush();
			ins = new BufferedInputStream(requestSocket.getInputStream());
		
			if (isPeerClient) {
				initializeClient(peerID);
			} else {
				initializeServer();
			}
			
			startCommunication();
		
		} catch (IOException ex) {
			ex.printStackTrace();
			peerProcess.objLogger.info("Exception: " + ex.toString());
		}
	}

	public void run() {
		Instant instant = Instant.now();
		long startEpoch = instant.getEpochSecond();

		try {
			long processingTime = 0l;
			long totalTime = 0l;
			
			byte[] msgLength, msgType;
			msgType = new byte[1];
			msgLength = new byte[4];
			
			int requestedIndex = 0;
			int noOfPiecesRcvd = 0;

			while (!killProcess.get()) {

				ins.read(msgLength);
				ins.read(msgType);
				int ordinal = new BigInteger(msgType).intValue();
				MessageTypes messageType = MessageTypes.values()[ordinal];

				switch (messageType) {
				case choke:
					peerProcess.objLogger.info("Peer: " + peerProcess.peerProcessID + " is choked by Peer: " + peerID);
					handleChokeMessage(requestedIndex);
					break;

				case unchoke:
					peerProcess.objLogger.info("Peer: " + peerProcess.peerProcessID + " is unchoked by Peer:" + peerID);
					int pieceInx = messageReaderObj.indexOfPieceToReq(peerProcess.bitField, peerBitField, peerProcess.piecesReqsted);
					if (pieceInx >= 0) {
						requestedIndex = pieceInx;
						peerProcess.piecesReqsted[pieceInx].set(true);
						sendMessage(msgHelper.constructRequestMessage(pieceInx));
						processingTime = System.nanoTime();
					}
					break;

				case interested:
					handleInterestedMessage();
					break;

				case not_interested:
					handleNotInterestedMessage();
					break;

				case have:
					handleHaveMessage();
					break;

				case request:
					handleRequestMessage();
					break;

				case piece:
					byte[] pInd = new byte[4];
					ins.read(pInd);

					int pieceIndex = ByteArrayUtil.convertByteArrayToInteger(pInd);

					int messageLength = ByteArrayUtil.convertByteArrayToInteger(msgLength);

					byte[] payload = messageReaderObj.readMessagePayload(ins, messageLength - 5);

					peerProcess.bitField[pieceIndex / 8] |= 1 << (7 - (pieceIndex % 8));

					int start = pieceIndex * PeerTorrConfig.PieceSize;
					for (int i = 0; i < payload.length; i++) {
						peerProcess.resourcePayload[start + i] = payload[i];
					}
					noOfPiecesRcvd++;
					peerProcess.objLogger.info("Peer: " + peerProcess.peerProcessID + " has downloaded the piece " + pieceIndex + " from Peer: "
							+ peerID + ". Now the number of pieces it has is : " + noOfPiecesRcvd);

					totalTime += System.nanoTime() - processingTime;
					dwnldrate = (float) ((noOfPiecesRcvd * PeerTorrConfig.PieceSize) / totalTime);

					publishHaveMessageToAllClients(pInd);

					pieceIndex = messageReaderObj.indexOfPieceToReq(peerProcess.bitField, peerBitField, peerProcess.piecesReqsted);

					if (pieceIndex >= 0) {
						requestedIndex = pieceIndex;
						peerProcess.piecesReqsted[pieceIndex].set(true);
						sendMessage(msgHelper.constructRequestMessage(pieceIndex));
						processingTime = System.currentTimeMillis();
					} else {
						checkAndPublishNotInterestedMessage();
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

	public void setStoppingCondition(boolean stop)  {
		killProcess.set(stop);
		if (killProcess.get()) {
			peerProcess.objLogger.info("Closing Socket");
			closeSocket();
		}
	}

	public void sendMessage(byte[] msg) {
		try {
			ops.write(msg);
			ops.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	public void handleChokeMessage(int requestedIndex) {
		byte indByte = peerProcess.bitField[requestedIndex / 8];
		if (((1 << (7 - (requestedIndex % 8))) & indByte) == 0) {
			peerProcess.piecesReqsted[requestedIndex].set(false);
		}
	}

	public void handleInterestedMessage() {
		peerProcess.objLogger.info("Peer " + peerProcess.peerProcessID + " received the 'interested' message from " + peerID + ".");
		clientInterested = true;

	}

	public void handleNotInterestedMessage() {
		peerProcess.objLogger
				.info("Peer " + peerProcess.peerProcessID + " received the 'not interested' message from Peer: " + peerID);
		clientInterested = false;
		isClientChoked = true;
	}

	public void handleHaveMessage() {

		byte[] pieceIndexbytes = messageReaderObj.readMessagePayload(ins, 4);
		int pieceIndex = ByteArrayUtil.convertByteArrayToInteger(pieceIndexbytes);
		peerProcess.objLogger.info("Peer: " + peerProcess.peerProcessID + " received the 'have' message from Peer: " + peerID
				+ " for the piece index:" + pieceIndex);

		peerBitField[pieceIndex / 8] |= (1 << (7 - (pieceIndex % 8)));
		byte indexByte = peerProcess.bitField[pieceIndex / 8];
		if (((1 << (7 - (pieceIndex % 8))) & indexByte) == 0) {
			sendMessage(msgHelper.constructInterestedMessage());
		} else {
			sendMessage(msgHelper.constructNotInterestedMessage());
		}
	}

	public void handleRequestMessage() {
		byte[] payload = messageReaderObj.readMessagePayload(ins, 4);

		int pieceInd = ByteArrayUtil.convertByteArrayToInteger(payload);

		peerProcess.objLogger.info("Peer: " + peerProcess.peerProcessID + " received the 'request' message from Peer: " + peerID
				+ " for the pieceIndex: " + pieceInd);
		int startInd = pieceInd * PeerTorrConfig.PieceSize;

		try {
			byte[] data;

			if ((PeerTorrConfig.fileSz - startInd) < PeerTorrConfig.PieceSize) {
				data = Arrays.copyOfRange(peerProcess.resourcePayload, startInd, PeerTorrConfig.fileSz);
			}

			else {
				data = Arrays.copyOfRange(peerProcess.resourcePayload, startInd, startInd + PeerTorrConfig.PieceSize);
			}

			if (!isClientChoked) {
				sendMessage(msgHelper.constructPieceMessage(pieceInd, data));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}

	}

	public void initializeClient(String clientId) {
		this.peerID = clientId;
		PeerHandshakeMessage hsm = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));
		sendMessage(hsm.constructHandshakeMessage());
		messageReaderObj.readTCPHSMessage(ins);
		peerProcess.objLogger.info("Peer "+ peerProcess.peerProcessID + "makes a connection to Peer:" + peerID);
	}

	public void initializeServer() {
		this.peerID = messageReaderObj.readTCPHSMessage(ins);
		PeerHandshakeMessage hsm = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));
		sendMessage(hsm.constructHandshakeMessage());
		peerProcess.objLogger.info("Peer: "+ peerProcess.peerProcessID + "makes a connection to Peer: " + peerID);
	}

	public void startCommunication() {
		peerProcess.objLogger.info("Peer: "+ peerProcess.peerProcessID + "is connected from Peer: " + peerID);
		sendMessage(msgHelper.constructBitFieldMessage(peerProcess.bitField));
		peerBitField = messageReaderObj.readTCPBitfieldPayload(ins);

		if (messageReaderObj.shouldSendIntrMessage(peerProcess.bitField, peerBitField)) {
			sendMessage(msgHelper.constructInterestedMessage());
		} else {
			sendMessage(msgHelper.constructNotInterestedMessage());
		}
	}

	public void closeSocket() {

		try {
			if (!requestSocket.isClosed())
				requestSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createAndWriteToFile() throws IOException {
		String path =  String.valueOf(peerProcess.peerProcessID);
		new File(path).mkdir();
		File file = new File(path + "/" + PeerTorrConfig.fileName);
		FileOutputStream fdata = new FileOutputStream(file);
		fdata.write(peerProcess.resourcePayload);
		fdata.close();
		peerProcess.objLogger.info("Peer " + peerProcess.peerProcessID + " has downloaded the complete file.");
	}

	public void publishHaveMessageToAllClients(byte[] pieceIndex) {
		for (PeerClient ct : peerProcess.peerClients) {
			ct.sendMessage(ct.msgHelper.constructHaveMessage(pieceIndex));
		}
	}

	public void checkAndPublishNotInterestedMessage() throws IOException {
		sendMessage(msgHelper.constructNotInterestedMessage());
		if (Arrays.equals(peerProcess.bitField, peerProcess.fullResource)) {
			for (PeerClient ct : peerProcess.peerClients) {
				ct.sendMessage(ct.msgHelper.constructNotInterestedMessage());
			}
			createAndWriteToFile();
		}
	}
}

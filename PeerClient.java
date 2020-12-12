
import java.net.*;
import java.io.*;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerClient extends Thread {

	Socket requestSocket;
	BufferedOutputStream bufferedOutputStream;
	BufferedInputStream bufferedIn;

	boolean isPeerClient;
	String peerID;
	byte[] peerBitFieldArray;
	
	
	boolean isClientInterested = true;
	boolean isClientChoked = true;
	AtomicBoolean killClientProcess = new AtomicBoolean(false);
	Float dataDownloadRate = 1.0f;

	PeerTorrConfig peerDataConfig;
	MessageReader messageDataReader;
	MessageConstructionUtil messageUtil;
	

	public PeerClient(Socket clientSocket, boolean isPeerClient, String peerID, PeerTorrConfig peerConfig) {
	
		this.peerDataConfig = peerConfig;
		this.requestSocket = clientSocket;
		this.isPeerClient = isPeerClient;

		messageDataReader = new MessageReader();
		messageUtil = new MessageConstructionUtil();
		
		try{
			bufferedOutputStream = new BufferedOutputStream(requestSocket.getOutputStream());
			bufferedOutputStream.flush();

			bufferedIn = new BufferedInputStream(requestSocket.getInputStream());

			if(isPeerClient){
				initializeP2PClient(peerID);
			}else{
				initializeP2PServer();
			}
			
			startP2PCommunication();
		} catch (IOException ex) {
			ex.printStackTrace();
			peerProcess.peerProcessLogger.info("Exception Occured while Creating Peer Client: " + ex.getMessage());
		}
	}

	public void run() {
		Instant thisInstant = Instant.now();
		long startEpoch = thisInstant.getEpochSecond();

		try {
			long processingTime = 0l;
			long totalTime = 0l;
			
			byte[] clientMessageLength, clientMessageType;
			clientMessageType = new byte[1];
			clientMessageLength = new byte[4];
			
			int requestedIndex = 0;
			int numberOfPiecesReceived = 0;

			while (!killClientProcess.get()) {

				bufferedIn.read(clientMessageLength);
				bufferedIn.read(clientMessageType);

				int clientMessageOrdValue = new BigInteger(clientMessageType).intValue();
				PeerCommunicationMessageType messageType = PeerCommunicationMessageType.values()[clientMessageOrdValue];

				if("CHOKE".equals(messageType)){
					peerProcess.peerProcessLogger.info("Peer: " + peerProcess.peerProcessID + " is choked by Peer: " + peerID);
					handleChokeMessage(requestedIndex);
				}else if("UNCHOKE".equals(messageType)){
					peerProcess.peerProcessLogger.info("Peer: " + peerProcess.peerProcessID + " is unchoked by Peer:" + peerID);
					int pieceInx = messageDataReader.fetchIndexOfPiece(peerProcess.bitField, peerBitFieldArray, peerProcess.piecesReqsted);
					if (pieceInx >= 0) {
						requestedIndex = pieceInx;
						peerProcess.piecesReqsted[pieceInx].set(true);
						sendMessage(messageUtil.constructRequestMessage(pieceInx));
						processingTime = System.nanoTime();
					}
				}else if("INTERESTED".equals(messageType)){
					fetchAndHandleInterestedMessage();
				}else if("NOTINTERESTED".equals(messageType)){
					fetchAndHandleNotInterestedMessage();
				}else if("HAVE".equals(messageType)){
					fetchAndHandleHaveMessage();
				}else if("REQUEST".equals(messageType)){
					handleRequestMessage();
				}else if("PIECE".equals(messageType)){
					byte[] pieceDataAsBytes = new byte[4];
					bufferedIn.read(pieceDataAsBytes);

					int pieceIndex = ByteArrayUtil.convertByteArrayToInteger(pieceDataAsBytes);
					int messageLength = ByteArrayUtil.convertByteArrayToInteger(clientMessageLength);

					byte[] messagePayload = messageDataReader.readAndfetchMessagePayload(bufferedIn, messageLength - 5);

					peerProcess.bitField[pieceIndex / 8] |= 1 << (7 - (pieceIndex % 8));

					int startingPoint = pieceIndex * PeerTorrConfig.PieceSize;

					for(int i = 0; i < messagePayload.length; i++) {
						peerProcess.resourcePayload[startingPoint + i] = messagePayload[i];
					}

					numberOfPiecesReceived++;

					peerProcess.peerProcessLogger.info("The Peer: " + peerProcess.peerProcessID + " downloaded the piece with Index " + pieceIndex + " from Peer: " + peerID + ". Now it has " + numberOfPiecesReceived+" pieces.");

					totalTime += System.nanoTime() - processingTime;
					dataDownloadRate = (float) ((numberOfPiecesReceived * PeerTorrConfig.PieceSize) / totalTime);

					publishAndBroadcastHaveMsg(pieceDataAsBytes);
					pieceIndex = messageDataReader.fetchIndexOfPiece(peerProcess.bitField, peerBitFieldArray, peerProcess.piecesReqsted);

					if(pieceIndex >= 0){
						requestedIndex = pieceIndex;
						peerProcess.piecesReqsted[pieceIndex].set(true);
						sendMessage(messageUtil.constructRequestMessage(pieceIndex));
						processingTime = System.currentTimeMillis();

					}else{
						checkAndPublishNotInterestedMessage();
					}
				}


				Instant instantNew = Instant.now();
				long endEpoch = instantNew.getEpochSecond();
				long diff = endEpoch - startEpoch;

				if(diff > 90){
					System.exit(0);
				}
			}
		}catch (IOException ex) {
			System.err.println("Exception while handling resource: "+ex.getMessage());
			System.err.println("Read the Stack Trace:");
			ex.printStackTrace();
		}
	}

	public void setConditionForStopping(boolean stopCondition)  {
		killClientProcess.set(stopCondition);

		if (killClientProcess.get()) {
			peerProcess.peerProcessLogger.info("Closing Socket");
			closePeerSocket();
		}
	}

	public void sendMessage(byte[] messagePayload) {
		try {
			bufferedOutputStream.write(messagePayload);
			bufferedOutputStream.flush();

		} catch (IOException ioException) {
			System.err.println("Exception occurred while Sending message across Peers: "+ioException.getMessage());
			ioException.printStackTrace();
		}
	}

	public void handleChokeMessage(int requestedIndex) {
		byte indexAsAByte = peerProcess.bitField[requestedIndex / 8];

		if (((1 << (7 - (requestedIndex % 8))) & indexAsAByte) == 0) {
			peerProcess.piecesReqsted[requestedIndex].set(false);
		}
	}

	public void fetchAndHandleInterestedMessage() {
		peerProcess.peerProcessLogger.info("Peer " + peerProcess.peerProcessID + " received An Interested message from " + peerID + ".");
		isClientInterested = true;

	}

	public void fetchAndHandleNotInterestedMessage() {
		peerProcess.peerProcessLogger.info("Peer " + peerProcess.peerProcessID + " received A Not Interested message from Peer: " + peerID+".");
		isClientInterested = false;
		isClientChoked = true;
	}

	public void fetchAndHandleHaveMessage() {
		byte[] pieceIndexbytes = messageDataReader.readAndfetchMessagePayload(bufferedIn, 4);
		int pieceIndex = ByteArrayUtil.convertByteArrayToInteger(pieceIndexbytes);

		peerProcess.peerProcessLogger.info("Peer: " + peerProcess.peerProcessID + " received the 'have' message from Peer: " + peerID + " for the piece index:" + pieceIndex);
		peerBitFieldArray[pieceIndex / 8] |= (1 << (7 - (pieceIndex % 8)));

		byte indexByte = peerProcess.bitField[pieceIndex / 8];

		if (((1 << (7 - (pieceIndex % 8))) & indexByte) == 0) {
			sendMessage(messageUtil.constructInterestedMessage());
		} else {
			sendMessage(messageUtil.constructNotInterestedMessage());
		}
	}

	public void handleRequestMessage() {
		byte[] requestMessagePayload = messageDataReader.readAndfetchMessagePayload(bufferedIn, 4);

		int receivedPieceIndex = ByteArrayUtil.convertByteArrayToInteger(requestMessagePayload);

		peerProcess.peerProcessLogger.info("Peer: " + peerProcess.peerProcessID + " received A Request from Peer: " + peerID + " for the Piece with Index: " + receivedPieceIndex);
		int pieceStartIndex = receivedPieceIndex * PeerTorrConfig.PieceSize;

		try {
			byte[] payloadData;

			if((PeerTorrConfig.fileSz - pieceStartIndex) < PeerTorrConfig.PieceSize){
				payloadData = Arrays.copyOfRange(peerProcess.resourcePayload, pieceStartIndex, PeerTorrConfig.fileSz);
			}else{
				payloadData = Arrays.copyOfRange(peerProcess.resourcePayload, pieceStartIndex, pieceStartIndex + PeerTorrConfig.PieceSize);
			}

			if(!isClientChoked){
				sendMessage(messageUtil.constructPieceMessage(receivedPieceIndex, payloadData));
			}
		}catch (Exception excep) {
			System.err.println("Exception occurred while handling request message: "+excep.getMessage());
			excep.printStackTrace();
		}
	}

	public void initializeP2PClient(String clientId) {
		this.peerID = clientId;

		PeerHandshakeMessage peerHandshakeMessage = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));

		sendMessage(peerHandshakeMessage.constructHandshakeMessage());
		messageDataReader.fetchTCPHasMessage(bufferedIn);
		peerProcess.peerProcessLogger.info("Peer "+ peerProcess.peerProcessID + "initiated a connection with Peer:" + peerID);
	}

	public void initializeP2PServer() {
		this.peerID = messageDataReader.fetchTCPHasMessage(bufferedIn);

		PeerHandshakeMessage peerHandShakeMsg = new PeerHandshakeMessage(String.valueOf(peerProcess.peerProcessID));
		sendMessage(peerHandShakeMsg.constructHandshakeMessage());
		peerProcess.peerProcessLogger.info("Peer: "+ peerProcess.peerProcessID + "accepted a connection with Peer: " + peerID);
	}

	public void startP2PCommunication() {
		sendMessage(messageUtil.constructBitFieldMessage(peerProcess.bitField));
		peerBitFieldArray = messageDataReader.fetchTCPBitfieldPayload(bufferedIn);

		peerProcess.peerProcessLogger.info("Peer: "+ peerProcess.peerProcessID + "is Connected to the Peer: " + peerID);

		if (messageDataReader.isPeerInterested(peerProcess.bitField, peerBitFieldArray)) {
			sendMessage(messageUtil.constructInterestedMessage());
		} else {
			sendMessage(messageUtil.constructNotInterestedMessage());
		}
	}

	public void createDirectory(String directory){
		new File(directory).mkdir();
	}

	public void writeDataToFile() throws IOException {
		String peerFolder =  String.valueOf(peerProcess.peerProcessID);

		new File(peerFolder).mkdir();

		String fileName = peerFolder + "/" + PeerTorrConfig.fileName;
		File file = new File(fileName);

		FileOutputStream fileOPStream = new FileOutputStream(file);
		fileOPStream.write(peerProcess.resourcePayload);
		fileOPStream.close();
	}

	public void publishAndBroadcastHaveMsg(byte[] pieceDataIndex) {
		for (PeerClient ct : peerProcess.peerClients) {
			ct.sendMessage(ct.messageUtil.constructHaveMessage(pieceDataIndex));
		}
	}

	public void checkAndPublishNotInterestedMessage() throws IOException {
		sendMessage(messageUtil.constructNotInterestedMessage());
		if (Arrays.equals(peerProcess.bitField, peerProcess.fullResource)) {
			for (PeerClient ct : peerProcess.peerClients) {
				ct.sendMessage(ct.messageUtil.constructNotInterestedMessage());
			}
			writeDataToFile();
			peerProcess.peerProcessLogger.info("Peer " + peerProcess.peerProcessID + " has downloaded the complete file.");
		}
	}

	public void closePeerSocket() {
		try {
			if (!requestSocket.isClosed()){
				requestSocket.close();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}

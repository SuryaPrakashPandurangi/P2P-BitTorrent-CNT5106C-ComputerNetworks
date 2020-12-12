
public class MessageConstructionUtil {

	private static final String LOGGER_CHOKE_MESSAGE = "Generating the CHOKE Message";
	private static final String LOGGER_UNCHOKE_MESSAGE = "Generating the UNCHOKE Message";
	private static final String LOGGER_INTERESTED_MESSAGE = "Generating the INTERESTED Message";
	private static final String LOGGER_NOTINTERESTED_MESSAGE = "Generating the NOTINTERESTED Message";
	private static final String LOGGER_HAVE_MESSAGE = "Generating the HAVE Message";
	private static final String LOGGER_BITFIELD_MESSAGE = "Generating the BITFIELD Message";
	private static final String LOGGER_REQUEST_MESSAGE = "Generating the REQUEST Message";
	private static final String LOGGER_PIECE_MESSAGE = "Generating the PIECE Message";

	public byte[] constructMessage(String messageType, int msg){
		peerProcess.peerProcessLogger.info(messageType);
		byte[] integerAsBytes = ByteArrayUtil.convertIntegerToByteArray(msg);
		byte messageAsByteArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.CHOKE.ordinal())[3];
		return ByteArrayUtil.mergeByteArraywithByte(integerAsBytes, messageAsByteArray);
	}

	public byte[] constructChokeMessage() {
		peerProcess.peerProcessLogger.info(LOGGER_CHOKE_MESSAGE);
		byte[] integerAsBytes = ByteArrayUtil.convertIntegerToByteArray(1);
		byte messageAsByteArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.CHOKE.ordinal())[3];
		return ByteArrayUtil.mergeByteArraywithByte(integerAsBytes, messageAsByteArray);
	}

	public byte[] constructUnChokeMessage() {
		peerProcess.peerProcessLogger.info(LOGGER_UNCHOKE_MESSAGE);
		byte[] messageIntAsArray = ByteArrayUtil.convertIntegerToByteArray(1);
		byte messageAsArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.UNCHOKE.ordinal())[3];
		byte[] unchokeMsg = ByteArrayUtil.mergeByteArraywithByte(messageIntAsArray, messageAsArray);
		return unchokeMsg;
	}

	public byte[] constructNotInterestedMessage() {
		peerProcess.peerProcessLogger.info(LOGGER_NOTINTERESTED_MESSAGE);
		byte[] messageIntAsArray = ByteArrayUtil.convertIntegerToByteArray(1);
		byte messageAsArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.NOTINTERESTED.ordinal())[3];
		byte[] notInterestedMessage = ByteArrayUtil.mergeByteArraywithByte(messageIntAsArray, messageAsArray);
		return notInterestedMessage;
	}

	public byte[] constructInterestedMessage() {
		peerProcess.peerProcessLogger.info(LOGGER_INTERESTED_MESSAGE);
		byte[] messageIntAsArray = ByteArrayUtil.convertIntegerToByteArray(1);
		byte messageAsArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.INTERESTED.ordinal())[3];
		byte[] interestedMsg = ByteArrayUtil.mergeByteArraywithByte(messageIntAsArray, messageAsArray);
		return interestedMsg;
	}



	public byte[] constructHaveMessage(byte[] pieceIndex) {
		peerProcess.peerProcessLogger.info(LOGGER_HAVE_MESSAGE);
		byte[] messageIntAsArray = ByteArrayUtil.convertIntegerToByteArray(5);
		byte messageAsArray = ByteArrayUtil.convertIntegerToByteArray(PeerCommunicationMessageType.HAVE.ordinal())[3];
		byte[] haveMessage = ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.mergeByteArraywithByte(messageIntAsArray, messageAsArray), pieceIndex);
		return haveMessage;
	}

	public byte[] fetchActualMessageAsByteArray(int messageLength, int messageType, byte[] payload) {
		byte[] lengthAsByteArray = ByteArrayUtil.convertIntegerToByteArray(messageLength);
		byte messageTypeInBytes = ByteArrayUtil.convertIntegerToByteArray(messageType)[3];
		return ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.mergeByteArraywithByte(lengthAsByteArray, messageTypeInBytes), payload);
	}


	public byte[] constructRequestMessage(int index) {
		peerProcess.peerProcessLogger.info(LOGGER_REQUEST_MESSAGE);
		ActualMessage requestMessageBody = new ActualMessage(PeerCommunicationMessageType.REQUEST, ByteArrayUtil.convertIntegerToByteArray(index));
		return requestMessageBody.fetchActualMessageAsByteArray();
	}

	public byte[] constructBitFieldMessage(byte[] payload) {
		peerProcess.peerProcessLogger.info(LOGGER_BITFIELD_MESSAGE);
		ActualMessage bitFieldMessage = new ActualMessage(PeerCommunicationMessageType.BITFIELD, payload);
		return bitFieldMessage.fetchActualMessageAsByteArray();
	}

	public byte[] constructPieceMessage(int idx, byte[] payload) {
		peerProcess.peerProcessLogger.info(LOGGER_PIECE_MESSAGE);
		ActualMessage pieceMessageBody = new ActualMessage(PeerCommunicationMessageType.PIECE, ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.convertIntegerToByteArray(idx), payload));
		return pieceMessageBody.fetchActualMessageAsByteArray();
	}



}

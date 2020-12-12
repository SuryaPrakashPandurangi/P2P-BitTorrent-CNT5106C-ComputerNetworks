
public class MessageConstructorUtil {
	private static final String LOGGER_CHOKE_MESSAGE = "Generating the CHOKE Message";
	private static final String LOGGER_UNCHOKE_MESSAGE = "Generating the UNCHOKE Message";
	private static final String LOGGER_INTERESTED_MESSAGE = "Generating the INTERESTED Message";
	private static final String LOGGER_NOTINTERESTED_MESSAGE = "Generating the NOTINTERESTED Message";
	private static final String LOGGER_HAVE_MESSAGE = "Generating the HAVE Message";
	private static final String LOGGER_BITFIELD_MESSAGE = "Generating the BITFIELD Message";
	private static final String LOGGER_REQUEST_MESSAGE = "Generating the REQUEST Message";
	private static final String LOGGER_PIECE_MESSAGE = "Generating the PIECE Message";

	public byte[] fetchAndConstructChokeMessage() {
		peerProcess.peerDataLogger.info(LOGGER_CHOKE_MESSAGE);
		byte[] cardinalIntegerAsBytes = ByteIOUtil.convertIntegerToByteArray(1);
		byte getMessageAsByte = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.CHOKE.ordinal())[3];
		byte[] chokeMessage = ByteIOUtil.mergeByteArraywithByte(cardinalIntegerAsBytes, getMessageAsByte);
		return chokeMessage;
	}

	public byte[] fetchAndConstructInterestedMessage() {
		peerProcess.peerDataLogger.info(LOGGER_INTERESTED_MESSAGE);
		byte[] cardinalIntegerAsBytes = ByteIOUtil.convertIntegerToByteArray(1);
		byte getMessageAsByte = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.INTERESTED.ordinal())[3];
		byte[] interestedMessage = ByteIOUtil.mergeByteArraywithByte(cardinalIntegerAsBytes, getMessageAsByte);
		return interestedMessage;
	}

	public byte[] fetchAndConstructUnChokeMessage() {
		peerProcess.peerDataLogger.info(LOGGER_UNCHOKE_MESSAGE);
		byte[] cardinalIntegerAsBytes = ByteIOUtil.convertIntegerToByteArray(1);
		byte getMessageAsByte = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.UNCHOKE.ordinal())[3];
		byte[] unchokeMessage = ByteIOUtil.mergeByteArraywithByte(cardinalIntegerAsBytes, getMessageAsByte);
		return unchokeMessage;
	}

	public byte[] fetchAndConstructHaveMessage(byte[] havePieceIndex) {
		peerProcess.peerDataLogger.info(LOGGER_HAVE_MESSAGE);
		byte[] cardinalIntegerAsBytes = ByteIOUtil.convertIntegerToByteArray(5);
		byte getMessageAsByte = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.HAVE.ordinal())[3];
		byte[] haveMessage = ByteIOUtil.mergeTwoByteArrays(ByteIOUtil.mergeByteArraywithByte(cardinalIntegerAsBytes, getMessageAsByte), havePieceIndex);
		return haveMessage;
	}

	public byte[] fetchAndConstructNotInterestedMessage() {
		peerProcess.peerDataLogger.info(LOGGER_NOTINTERESTED_MESSAGE);
		byte[] cardinalIntegerAsBytes = ByteIOUtil.convertIntegerToByteArray(1);
		byte getMessageAsByte = ByteIOUtil.convertIntegerToByteArray(PeerCommunicationMessageType.NOTINTERESTED.ordinal())[3];
		byte[] notInterestedMessage = ByteIOUtil.mergeByteArraywithByte(cardinalIntegerAsBytes, getMessageAsByte);
		return notInterestedMessage;
	}

	public byte[] fetchAndConstructRequestMessage(int requestIndex) {
		peerProcess.peerDataLogger.info(LOGGER_REQUEST_MESSAGE);
		ActualMessage requestMessage = new ActualMessage(PeerCommunicationMessageType.REQUEST, ByteIOUtil.convertIntegerToByteArray(requestIndex));
		return requestMessage.getActualMessageInBytes();
	}

	public byte[] fetchAndConstructBitFieldMessage(byte[] inputPayload) {
		peerProcess.peerDataLogger.info(LOGGER_BITFIELD_MESSAGE);
		ActualMessage bitFieldMessage = new ActualMessage(PeerCommunicationMessageType.BITFIELD, inputPayload);
		return bitFieldMessage.getActualMessageInBytes();
	}

	public byte[] fetchAndConstructPieceMessage(int pieceIndex, byte[] piecePayload) {
		peerProcess.peerDataLogger.info(LOGGER_PIECE_MESSAGE);
		ActualMessage constructPieceMessage = new ActualMessage(PeerCommunicationMessageType.PIECE, ByteIOUtil.mergeTwoByteArrays(ByteIOUtil.convertIntegerToByteArray(pieceIndex), piecePayload));
		return constructPieceMessage.getActualMessageInBytes();
	}

}

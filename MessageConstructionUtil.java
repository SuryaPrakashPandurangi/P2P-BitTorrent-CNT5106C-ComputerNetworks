
public class MessageConstructionUtil {

	public byte[] constructChokeMessage() {
		peerProcess.objLogger.info("Constructing CHOKE Message");
		byte[] len = ByteArrayUtil.convertIntegerToByteArray(1);
		byte byt = ByteArrayUtil.convertIntegerToByteArray(MessageTypes.choke.ordinal())[3];
		byte[] res = ByteArrayUtil.mergeByteArraywithByte(len, byt);
		return res;
	}

	public byte[] constructUnChokeMessage() {
		peerProcess.objLogger.info("Constructing UNCHOKE Message");
		byte[] len = ByteArrayUtil.convertIntegerToByteArray(1);
		byte byt = ByteArrayUtil.convertIntegerToByteArray(MessageTypes.unchoke.ordinal())[3];
		byte[] res = ByteArrayUtil.mergeByteArraywithByte(len, byt);
		return res;
	}

	public byte[] constructInterestedMessage() {
		peerProcess.objLogger.info("Constructing INTERESTED Message");
		byte[] len = ByteArrayUtil.convertIntegerToByteArray(1);
		byte byt = ByteArrayUtil.convertIntegerToByteArray(MessageTypes.interested.ordinal())[3];
		byte[] res = ByteArrayUtil.mergeByteArraywithByte(len, byt);
		return res;
	}

	public byte[] constructNotInterestedMessage() {
		peerProcess.objLogger.info("Constructing NOTINTERESTED Message");
		byte[] len = ByteArrayUtil.convertIntegerToByteArray(1);
		byte byt = ByteArrayUtil.convertIntegerToByteArray(MessageTypes.not_interested.ordinal())[3];
		byte[] res = ByteArrayUtil.mergeByteArraywithByte(len, byt);
		return res;
	}

	public byte[] constructHaveMessage(byte[] pieceIndex) {
		peerProcess.objLogger.info("Constructing HAVE message");
		byte[] len = ByteArrayUtil.convertIntegerToByteArray(5);
		byte byt = ByteArrayUtil.convertIntegerToByteArray(MessageTypes.have.ordinal())[3];
		byte[] res = ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.mergeByteArraywithByte(len, byt), pieceIndex);
		return res;
	}

	public byte[] constructBitFieldMessage(byte[] payload) {
		peerProcess.objLogger.info("Constructing BITFIELD Message");
		ActualMessage actlMessage = new ActualMessage(MessageTypes.bitfield, payload);
		return actlMessage.fetchActualMessageAsByteArray();
	}

	public byte[] constructRequestMessage(int index) {
		peerProcess.objLogger.info("Constructing REQUEST Message");
		ActualMessage actlMessage = new ActualMessage(MessageTypes.request, ByteArrayUtil.convertIntegerToByteArray(index));
		return actlMessage.fetchActualMessageAsByteArray();
	}

	public byte[] constructPieceMessage(int idx, byte[] payload) {
		peerProcess.objLogger.info("Constructing PIECE Message");
		ActualMessage actlMessage = new ActualMessage(MessageTypes.piece,
				ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.convertIntegerToByteArray(idx), payload));
		return actlMessage.fetchActualMessageAsByteArray();
	}

}

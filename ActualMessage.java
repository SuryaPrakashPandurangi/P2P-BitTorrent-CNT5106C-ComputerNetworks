

public class ActualMessage {
	int exchangeMessagelength;
	PeerCommunicationMessageType messageType;
	byte[] messagePayload;
	
	public ActualMessage(PeerCommunicationMessageType messageType, byte[] payLoad) {
		this.exchangeMessagelength = payLoad.length;
		this.messageType = messageType;
		this.messagePayload = payLoad;
	}
	
	public int getExchangeMessagelength() {
		return exchangeMessagelength;
	}
	public void setExchangeMessagelength(int exchangeMessagelength) {
		this.exchangeMessagelength = exchangeMessagelength;
	}
	public PeerCommunicationMessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(PeerCommunicationMessageType messageType) {
		this.messageType = messageType;
	}
	public byte[] getMessagePayload() {
		return messagePayload;
	}
	public void setMessagePayload(byte[] messagePayload) {
		this.messagePayload = messagePayload;
	}
	
	public byte[] getActualMessageInBytes() {
		Integer msgLength = getExchangeMessagelength() + 1;
		byte[] messagelength = ByteIOUtil.convertIntegerToByteArray(msgLength);
		byte byteArrayInt = ByteIOUtil.convertIntegerToByteArray(getMessageType().ordinal())[3];
		byte[] actualMessageBytes = ByteIOUtil.mergeTwoByteArrays(ByteIOUtil.mergeByteArraywithByte(messagelength, byteArrayInt), getMessagePayload());
		return actualMessageBytes;
	}
}

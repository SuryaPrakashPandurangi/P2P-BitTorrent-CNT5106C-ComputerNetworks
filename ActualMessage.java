
public class ActualMessage {
	int exchangeMessagelength;//Is 4 Bytes long
	PeerCommunicationMessageType messageType;	//This is 1Byte long
	byte[] messagePayload;	//The Length is not fixed to a Value
	
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
	
	public byte[] fetchActualMessageAsByteArray() {
		Integer messageLength = getExchangeMessagelength() + 1;
		byte[] lengthAsByteArray = ByteArrayUtil.convertIntegerToByteArray(messageLength);
		byte messageTypeInBytes = ByteArrayUtil.convertIntegerToByteArray(getMessageType().ordinal())[3];
		return ByteArrayUtil.mergeTwoByteArrays(ByteArrayUtil.mergeByteArraywithByte(lengthAsByteArray, messageTypeInBytes), getMessagePayload());
	}
}

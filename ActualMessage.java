
public class ActualMessage {
	int exchangeMessagelength;//Is 4 Bytes long
	MessageTypes messageType;	//This is 1Byte long
	byte[] messagePayload;	//The Length is not fixed to a Value
	
	public ActualMessage(MessageTypes messageType, byte[] payLoad) {
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

	public MessageTypes getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageTypes messageType) {
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

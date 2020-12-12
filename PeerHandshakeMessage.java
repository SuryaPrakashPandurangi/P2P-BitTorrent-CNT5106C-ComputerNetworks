

public class PeerHandshakeMessage {
	private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";

	String handshakeHeader;
	byte[] zeroBits;
	String peerID;
	
	public PeerHandshakeMessage(String peerID) {
		this.handshakeHeader = HANDSHAKE_HEADER;
		this.zeroBits = new byte[]{0,0,0,0,0,0,0,0,0,0};
		this.peerID = peerID;
	}
	
	
	public String getHandshakeHeader() {
		return handshakeHeader;
	}
	
	public void setHandshakeHeader(String handshakeHeader) {
		this.handshakeHeader = handshakeHeader;
	}
	
	public String getPeerID() {
		return peerID;
	}
	
	public void setPeerID(String peerID) {
		this.peerID = peerID;
	}
	
	public byte[] getZeroBits() {
		return zeroBits;
	}
	
	public void setZeroBits(byte[] zeroBits) {
		this.zeroBits = zeroBits;
	}
	
	public byte[] constructHandshakeMessage(){
		byte[] paddedHeader = ByteArrayUtil.mergeTwoByteArrays(getHandshakeHeader().getBytes(), getZeroBits());
		byte[] peerhandshakeMessage = ByteArrayUtil.mergeTwoByteArrays(paddedHeader, getPeerID().getBytes());
		return peerhandshakeMessage;
	}
}

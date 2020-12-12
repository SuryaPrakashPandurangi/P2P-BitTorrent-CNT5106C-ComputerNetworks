import java.util.Arrays;

public class PeerHandshakeMessage {
	private static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";

	String handshakeHeader;
	byte[] padding;
	String peerID;

	private static byte[] returnEightBitPadding(){
		return new byte[]{0,0,0,0,0,0,0,0,0,0};
	}

	public PeerHandshakeMessage(String peerID) {
		this.handshakeHeader = HANDSHAKE_HEADER;
		this.padding = new byte[]{0,0,0,0,0,0,0,0,0,0};
		this.peerID = peerID;
	}

	public byte[] constructHandshakeMessage() {
		byte[] paddedHeader = ByteIOUtil.mergeTwoByteArrays(getHandshakeHeader().getBytes(), getPadding());
		byte[] constructedHandshakeMessage = ByteIOUtil.mergeTwoByteArrays(paddedHeader, getPeerID().getBytes());
		return constructedHandshakeMessage;
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

	public byte[] getPadding() {
		return padding;
	}

	public void setPadding(byte[] padding) {
		this.padding = padding;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PeerHandshakeMessage{");
		sb.append("handshakeHeader='").append(handshakeHeader).append('\'');
		sb.append(", padding=").append(Arrays.toString(padding));
		sb.append(", peerID='").append(peerID).append('\'');
		sb.append('}');
		return sb.toString();
	}
}


public class RemotePeer {

	public String peerId;
	public String peerAddress;
	public String peerPort;
	public String peerHasFile;

	public RemotePeer(String ipPeerID, String ipPeerAddress, String ipPeerPort, String ipPeerHasFile) {
		peerId = ipPeerID;
		peerAddress = ipPeerAddress;
		peerPort = ipPeerPort;
		peerHasFile = ipPeerHasFile;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(String peerPort) {
		this.peerPort = peerPort;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}

	public String getPeerHasFile() {
		return peerHasFile;
	}

	public void setPeerHasFile(String peerHasFile) {
		this.peerHasFile = peerHasFile;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Remote Peer Info {");
		sb.append("peerId='").append(peerId).append('\'');
		sb.append(", peerAddress='").append(peerAddress).append('\'');
		sb.append(", peerPort='").append(peerPort).append('\'');
		sb.append(", peerHasFile='").append(peerHasFile).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

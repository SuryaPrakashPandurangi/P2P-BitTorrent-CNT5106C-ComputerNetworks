
public class RemotePeerInfo {
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public String peerHasFile;

	public RemotePeerInfo(String ipPeerID, String ipPeerAddress, String ipPeerPort, String ipPeerHasFile) {
		peerId = ipPeerID;
		peerHasFile = ipPeerHasFile;
		peerAddress = ipPeerAddress;
		peerPort = ipPeerPort;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public String getPeerHasFile() {
		return peerHasFile;
	}

	public void setPeerHasFile(String peerHasFile) {
		this.peerHasFile = peerHasFile;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}

	public String getPeerPort() {
		return peerPort;
	}

	public void setPeerPort(String peerPort) {
		this.peerPort = peerPort;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Remote Peer Info{");
		sb.append("Peer Id='").append(peerId).append('\'');
		sb.append(", Peer Address='").append(peerAddress).append('\'');
		sb.append(", Peer Port='").append(peerPort).append('\'');
		sb.append(", Does Peer Have File='").append(peerHasFile).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

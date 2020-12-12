
public class RemotePeerInfo {
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public String peerHasFile;

	public RemotePeerInfo(String pId, String pAddress, String pPort, String pHasFile) {
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		peerHasFile = pHasFile;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
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

	public String getPeerHasFile() {
		return peerHasFile;
	}

	public void setPeerHasFile(String peerHasFile) {
		this.peerHasFile = peerHasFile;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RemotePeerInfo{");
		sb.append("peerId='").append(peerId).append('\'');
		sb.append(", peerAddress='").append(peerAddress).append('\'');
		sb.append(", peerPort='").append(peerPort).append('\'');
		sb.append(", peerHasFile='").append(peerHasFile).append('\'');
		sb.append('}');
		return sb.toString();
	}
}

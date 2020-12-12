
import java.io.IOException;
import java.util.ArrayList;

public class PeerDataConfig {
	static Integer peerCount;
	static Integer preferredUnchokingTime;
	static Integer optimisticUnchokingInterval;
	static String peerTransferFileName;
	static Integer peerTransferFileSize;
	static Integer transferPieceSize;
	static Integer port = 8000;
	static ArrayList<RemotePeer> peerList;
	static Integer pieceCount;
	static Integer byteCount;

	public PeerDataConfig() throws IOException {
		peerList = new ArrayList<RemotePeer>();

		PeerDataConfigUtil.readCommonConfigInfo();
		PeerDataConfigUtil.readPeerConfigInfo(peerList);

		pieceCount = (peerTransferFileSize % transferPieceSize == 0) ? peerTransferFileSize / transferPieceSize : (peerTransferFileSize / transferPieceSize) + 1;
		byteCount = (pieceCount % 8 == 0) ? pieceCount / 8 : (pieceCount / 8) + 1;
	}
}

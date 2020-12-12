
import java.io.IOException;
import java.util.ArrayList;

public class PeerTorrConfig {
	static Integer noOfPrefPeers;
	static Integer prefPeersUnchokingTime;
	static Integer optUnchokingTime;
	static String fileName;
	static Integer fileSz;
	static Integer PieceSize;
	static Integer port = 8000;
	static ArrayList<RemotePeerInfo> peerInfoList;
	static Integer noOfPieces;
	static Integer noOfBytes;

	public PeerTorrConfig() throws IOException {
		peerInfoList = new ArrayList<RemotePeerInfo>();
		IOUtil.fetchCommonConfigData();
		IOUtil.fetchPeerInfoConfigData(peerInfoList);
		noOfPieces = (fileSz % PieceSize == 0) ? fileSz / PieceSize : (fileSz / PieceSize) + 1;
		noOfBytes = (noOfPieces % 8 == 0) ? noOfPieces / 8 : (noOfPieces / 8) + 1;
	}
}

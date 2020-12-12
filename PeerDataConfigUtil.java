import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;


public class PeerDataConfigUtil {

	private static BufferedReader configBufferedReader;
	private static final String PEERINFO = "PeerInfo.cfg";
	private static final String COMMONCONFIG = "Common.cfg";

	public static void readPeerConfigInfo(List<RemotePeer> peerData) throws IOException {
		FileReader peerInfoReader = new FileReader(PEERINFO);
		configBufferedReader = new BufferedReader(peerInfoReader);
		String peerConfigData = configBufferedReader.readLine();
		while (null != peerConfigData) {
			StringTokenizer tokens = new StringTokenizer(peerConfigData," ");

			String[] individualPeerDataChunks = peerConfigData.split("\\s+");
			peerData.add(new RemotePeer(individualPeerDataChunks[0], individualPeerDataChunks[1], individualPeerDataChunks[2], individualPeerDataChunks[3]));
			peerConfigData = configBufferedReader.readLine();
		}
	}
	
	public static void readCommonConfigInfo() throws IOException {

		FileReader commonConfigFileReader = new FileReader(COMMONCONFIG);
		configBufferedReader = new BufferedReader(commonConfigFileReader);
		String commonConfParam = configBufferedReader.readLine();
		while (null != commonConfParam) {
			String[] configParam = commonConfParam.split("\\s+");
			
			if("NumberOfPreferredNeighbors".equals(configParam[0])){
				PeerDataConfig.peerCount = Integer.parseInt(configParam[1]);
			}else if("UnchokingInterval".equals(configParam[0])){
				PeerDataConfig.preferredUnchokingTime = Integer.parseInt(configParam[1]);
			}else if("OptimisticUnchokingInterval".equals(configParam[0])){
				PeerDataConfig.optimisticUnchokingInterval = Integer.parseInt(configParam[1]);
			}else if("FileName".equals(configParam[0])){
				PeerDataConfig.peerTransferFileName = configParam[1];
			}else if("FileSize".equals(configParam[0])){
				PeerDataConfig.peerTransferFileSize = Integer.parseInt(configParam[1]);
			}else if("PieceSize".equals(configParam[0])){
				PeerDataConfig.transferPieceSize = Integer.parseInt(configParam[1]);
			}

			commonConfParam = configBufferedReader.readLine();
		}

	}
	
}

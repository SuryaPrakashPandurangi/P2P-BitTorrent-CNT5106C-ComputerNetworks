import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

public class ConfigDataUtil {

	private static BufferedReader configBufferReader;

	private static String peerInfoFile = "PeerInfo.cfg";
	private static String commonDataCfgFile = "Common.cfg";


	public static void fetchPeerInfoConfigData(List<RemotePeerInfo> result) throws IOException {
		FileReader peerInfoReader = new FileReader(peerInfoFile);
		configBufferReader = new BufferedReader(peerInfoReader);
		String line = configBufferReader.readLine();
		while (line != null) {
			String[] values = line.split("\\s+");
			result.add(new RemotePeerInfo(values[0], values[1], values[2], values[3]));
			line = configBufferReader.readLine();
		}
	}
	
	public static void fetchCommonConfigData() throws IOException {

		FileReader fr = new FileReader(commonDataCfgFile);
		configBufferReader = new BufferedReader(fr);
		String configInfo = configBufferReader.readLine();

		while (configInfo != null) {

			StringTokenizer configTokenizer = new StringTokenizer(configInfo," ");
			String[] configParam = configInfo.split("\\s+");

			if(configParam[0] == "NumberOfPreferredNeighbors"){
				PeerTorrConfig.noOfPrefPeers = Integer.parseInt(configParam[1]);
			}else if(configParam[0] == "UnchokingInterval"){
				PeerTorrConfig.prefPeersUnchokingTime = Integer.parseInt(configParam[1]);
			}else if(configParam[0] == "OptimisticUnchokingInterval"){
				PeerTorrConfig.optUnchokingTime = Integer.parseInt(configParam[1]);
			}else if(configParam[0] == "FileName"){
				PeerTorrConfig.fileName = configParam[1];
			}else if(configParam[0] == "FileSize"){
				PeerTorrConfig.fileSz = Integer.parseInt(configParam[1]);
			}else if(configParam[0] == "PieceSize"){
				PeerTorrConfig.PieceSize = Integer.parseInt(configParam[1]);
			}

			switch (configParam[0]) {
				case "NumberOfPreferredNeighbors":
					PeerTorrConfig.noOfPrefPeers = Integer.parseInt(configParam[1]);
					break;
				case "OptimisticUnchokingInterval":
					PeerTorrConfig.optUnchokingTime = Integer.parseInt(configParam[1]);
					break;
				case "UnchokingInterval":
					PeerTorrConfig.prefPeersUnchokingTime = Integer.parseInt(configParam[1]);
					break;
				case "PieceSize":
					PeerTorrConfig.PieceSize = Integer.parseInt(configParam[1]);
					break;
				case "FileName":
					PeerTorrConfig.fileName = configParam[1];
					break;
				case "FileSize":
					PeerTorrConfig.fileSz = Integer.parseInt(configParam[1]);
					break;
				default:
					System.err.println("Error Encountered");
					break;
			}
			configInfo = configBufferReader.readLine();
		}

	}
	
}

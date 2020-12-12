import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class peerProcess {

	static List<PeerClient> peerClients = Collections.synchronizedList(new ArrayList<PeerClient>());
	static PeerClient optUnchokedNeighbor;
	static byte[] bitField, resourcePayload, fullResource;
	static AtomicBoolean[] piecesReqsted;
	static Logger peerProcessLogger;

	ScheduledExecutorService taskSchedlr = Executors.newScheduledThreadPool(3);

	Integer port = 8000;
	static Integer peerProcessID;
	static ServerSocket serverSocket;

	public static void main(String[] args) throws Exception {

		peerProcess peerProcessObj = new peerProcess();
		peerProcessID = Integer.parseInt(args[0]);

		PeerTorrConfig configObj = new PeerTorrConfig();
		peerProcessLogger = ProcessLogger.getLogger(peerProcessID);

		List<RemotePeerInfo> connectionEstablishedPeers = new ArrayList<RemotePeerInfo>();
		List<RemotePeerInfo> yetToConnectPeers = new ArrayList<RemotePeerInfo>();

		boolean isFileAvailable = false;
		for (RemotePeerInfo rpi : PeerTorrConfig.peerInfoList) {
			if (Integer.parseInt(rpi.peerId) < peerProcessID) {
				connectionEstablishedPeers.add(rpi);
			} else if (Integer.parseInt(rpi.peerId) == peerProcessID) {
				peerProcessObj.port = Integer.parseInt(rpi.peerPort);
				if (rpi.peerHasFile.equals("1"))
					isFileAvailable = true;
			} else {
				yetToConnectPeers.add(rpi);
			}
		}
		bitField = new byte[PeerTorrConfig.noOfBytes];
		piecesReqsted = new AtomicBoolean[PeerTorrConfig.noOfPieces];
		Arrays.fill(piecesReqsted, new AtomicBoolean(false));
		resourcePayload = new byte[PeerTorrConfig.fileSz];
		fullResource = new byte[PeerTorrConfig.noOfBytes];
		intializeVariables(isFileAvailable, PeerTorrConfig.noOfPieces);
		listenToConnectedPeers(connectionEstablishedPeers, configObj);
		serverSocket = new ServerSocket(peerProcessObj.port);
		peerProcessLogger.info("Socket Opened on port: " + peerProcessObj.port);
		listenToFuturePeers(yetToConnectPeers, configObj);
		selectOptimisticallyUnchokedNeighbour();
		startTaskSchedulers(peerProcessObj);


	}

	public static void readResourceFile() throws IOException {
		try {
			File resource = new File( peerProcess.peerProcessID + "/" + PeerTorrConfig.fileName);
			FileInputStream filePayload = new FileInputStream(resource);
			filePayload.read(resourcePayload);
			filePayload.close();
		} catch (FileNotFoundException ex) {
			System.err.println("Exception occured while reading the File from Peer: "+peerProcess.peerProcessID+" .Message: "+ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void intializeVariables(boolean fileAvailable, int pieces) throws IOException {
		Arrays.fill(fullResource, (byte) 255);
		if (fileAvailable) {
			readResourceFile();
			Arrays.fill(bitField, (byte) 255);
			if (pieces % 8 != 0) {
				int end = (int) pieces % 8;
				bitField[bitField.length - 1] = 0;
				fullResource[bitField.length - 1] = 0;
				while (end != 0) {
					bitField[bitField.length - 1] |= (1 << (8 - end));
					fullResource[bitField.length - 1] |= (1 << (8 - end));
					end--;
				}
			}
		} else {
			if (pieces % 8 != 0) {
				int end = (int) pieces % 8;
				fullResource[bitField.length - 1] = 0;
				while (end != 0) {
					fullResource[bitField.length - 1] |= (1 << (8 - end));
					end--;
				}
			}
		}
	}

	public static void listenToConnectedPeers(List<RemotePeerInfo> connectionEstablishedPeers, PeerTorrConfig configObj) {

		for (RemotePeerInfo pInfo : connectionEstablishedPeers) {
			try {
				PeerClient client = new PeerClient(new Socket(pInfo.peerAddress, Integer.parseInt(pInfo.peerPort)),
						true, pInfo.peerId, configObj);

				client.start();
				peerClients.add(client);
				peerProcessLogger.info("Peer " + peerProcessID + " makes a connection to Peer " + pInfo.peerId + ".");
			} catch (Exception ex) {
				ex.printStackTrace();
				peerProcessLogger.info(ex.toString());
			}

		}

	}

	public static void selectOptimisticallyUnchokedNeighbour() {
		List<PeerClient> interestedAndChokedNeighbour = new ArrayList<PeerClient>();

		for (PeerClient peerClient : peerClients) {
			if (peerClient.isClientInterested && peerClient.isClientChoked) {
				interestedAndChokedNeighbour.add(peerClient);
			}
		}

		if (interestedAndChokedNeighbour.isEmpty()) {
			optUnchokedNeighbor = null;
		} else {
			optUnchokedNeighbor = interestedAndChokedNeighbour
					.get(new Random().nextInt(interestedAndChokedNeighbour.size()));
		}
	}

	public static void listenToFuturePeers(List<RemotePeerInfo> yetToConnectPeers, PeerTorrConfig configObj) {
		try {
			for (RemotePeerInfo remotePeerInfoObj : yetToConnectPeers) {
				Runnable peerConn = () -> {
					try {
						PeerClient futurePeer = new PeerClient(serverSocket.accept(), false, remotePeerInfoObj.peerId,
								configObj);
						peerProcessLogger.info(
								"Future Peer " + peerProcessID + " is connected from Peer " + remotePeerInfoObj.peerId + ".");
						peerClients.add(futurePeer);
						futurePeer.start();
					} catch (IOException e) {
						peerProcessLogger.info(e.getMessage());
					}
				};
				new Thread(peerConn).start();
			}
		} catch (Exception ex) {
			peerProcessLogger.info("Exception while listening to future peers :" + ex.getMessage());
			//ex.printStackTrace();
		}
	}

	public void startPrefferreNeighbourScheduler(int k, int p) {
		Runnable findPreferredNeibhbours = () -> {
			refreshPreferredNeighbours(k);
		};
		taskSchedlr.scheduleAtFixedRate(findPreferredNeibhbours, p, p, TimeUnit.SECONDS);
	}

	public void refreshPreferredNeighbours(int noOfPreferreNeighbours) {
		try {
			Collections.sort(peerClients, (ct1, ct2) -> ct2.dataDownloadRate.compareTo(ct1.dataDownloadRate));
			int counter = 0;
			List<String> prefferredList = new ArrayList<String>();

			for (PeerClient client : peerClients) {
				if (client.isClientInterested) {
					if (counter < noOfPreferreNeighbours) {
						if (client.isClientChoked) {
							client.isClientChoked = false;
							client.sendMessage(client.messageUtil.constructUnChokeMessage());
						}
						prefferredList.add(client.peerID);
					} else {

						if (!client.isClientChoked && client != optUnchokedNeighbor) {
							client.isClientChoked = true;
							client.sendMessage(client.messageUtil.constructChokeMessage());
						}
					}

					counter++;
				}
			}
			peerProcessLogger.info("Peer " + peerProcessID + " with preferred neighbours:" + prefferredList);
		} catch (Exception e) {
			peerProcessLogger.info(e.toString());
		}
	}

	public void startOptimisticallyPreferreScheduler(int m) {

		Runnable findOptimisticallyPreferreNeighbour = () -> {
			refreshOptimisticallyPreferredNeighmour();
		};
		taskSchedlr.scheduleAtFixedRate(findOptimisticallyPreferreNeighbour, m, m, TimeUnit.SECONDS);
	}

	public void refreshOptimisticallyPreferredNeighmour() {
		try {

			List<PeerClient> interestedAndChokedNeighbour = new ArrayList<PeerClient>();

			for (PeerClient peerClient : peerClients) {
				if (peerClient.isClientInterested && peerClient.isClientChoked) {
					interestedAndChokedNeighbour.add(peerClient);
				}
			}

			if (!interestedAndChokedNeighbour.isEmpty()) {
				if (optUnchokedNeighbor != null) {
					optUnchokedNeighbor.isClientChoked = true;
					optUnchokedNeighbor.sendMessage(optUnchokedNeighbor.messageUtil.constructChokeMessage());
				}
				optUnchokedNeighbor = interestedAndChokedNeighbour
						.get(new Random().nextInt(interestedAndChokedNeighbour.size()));
				optUnchokedNeighbor.isClientChoked = false;
				optUnchokedNeighbor.sendMessage(optUnchokedNeighbor.messageUtil.constructUnChokeMessage());

			} else {
				if (optUnchokedNeighbor != null) {
					if (!optUnchokedNeighbor.isClientChoked) {
						optUnchokedNeighbor.isClientChoked = true;
						optUnchokedNeighbor.sendMessage(optUnchokedNeighbor.messageUtil.constructChokeMessage());
					}
					optUnchokedNeighbor = null;
				}
			}

			if (optUnchokedNeighbor != null)
				peerProcessLogger.info("Peer: " + peerProcessID + " has the optimistically unchoked neighbor Peer: "
						+ optUnchokedNeighbor.peerID);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	public void checkForCompleteFile() {
		Runnable pollForPeerFile = () -> {
			checkAndCloseSocket(hasAllClientsReceived());
		};
		taskSchedlr.scheduleAtFixedRate(pollForPeerFile, 10, 5, TimeUnit.SECONDS);
	}

	public boolean hasAllClientsReceived() {
		boolean isFileReceived = true;
		for (PeerClient ct : peerClients) {
			if (!Arrays.equals(ct.peerBitFieldArray, fullResource)) {
				peerProcessLogger.info("Peer " + ct.peerID + " yet to receive the full file.");
				isFileReceived = false;
				break;
			}
		}
		return isFileReceived;
	}

	public void checkAndCloseSocket(boolean isTrasferred) {
		peerProcessLogger.info("Complete File Status: " + isTrasferred);
		if (isTrasferred && Arrays.equals(bitField, fullResource)) {
			for (PeerClient ct : peerClients) {
				ct.setConditionForStopping(true);
			}
			taskSchedlr.shutdown();

			try {
				if (!serverSocket.isClosed())
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				peerProcessLogger.info("Exception During socket closing");
			} finally {
				peerProcessLogger.info("ShuttingDown the PeerProcess with Id: " + peerProcessID);
				System.exit(0);
			}
		}
	}

	public static void startTaskSchedulers(peerProcess peerProc) {
		peerProc.startPrefferreNeighbourScheduler(PeerTorrConfig.noOfPrefPeers, PeerTorrConfig.prefPeersUnchokingTime);
		peerProc.startOptimisticallyPreferreScheduler(PeerTorrConfig.optUnchokingTime);
		peerProc.checkForCompleteFile();
	}

}
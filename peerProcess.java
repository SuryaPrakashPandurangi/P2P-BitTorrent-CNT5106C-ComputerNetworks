import java.io.File;
import java.io.FileInputStream;
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
	static PeerClient optimallyUnchokedNeighbour;
	static AtomicBoolean[] noOfPiecesRequested;
	static Logger peerDataLogger;
	static byte[] bitFieldData, resourceDataPayload, fullDataResource;

	ScheduledExecutorService taskScheduleExecutor = Executors.newScheduledThreadPool(3);
	static Integer peerProcessID;
	static ServerSocket peerServerSocket;
	Integer port = 8000;

	public static void readPeerDataFromFile() throws IOException {
		try {
			File dataResource = new File( peerProcess.peerProcessID + "/" + PeerDataConfig.peerTransferFileName);
			FileInputStream dataObtainedFromResource = new FileInputStream(dataResource);
			dataObtainedFromResource.read(resourceDataPayload);
			dataObtainedFromResource.close();
		} catch (Exception ex) {
			System.err.println("Exception occurred while reading the File from Peer: "+peerProcess.peerProcessID+" .Message: "+ex.getMessage());
			ex.printStackTrace();
		}
	}

	public static void fetchAllInitialData(boolean isFileAvailable, int dataPiece) throws IOException {
		Arrays.fill(fullDataResource, (byte) 255);
		if (isFileAvailable) {
			readPeerDataFromFile();
			Arrays.fill(bitFieldData, (byte) 255);
			if (dataPiece % 8 != 0) {
				int finalIndex = (int) dataPiece % 8;
				bitFieldData[bitFieldData.length - 1] = 0;
				fullDataResource[bitFieldData.length - 1] = 0;
				while (finalIndex != 0) {
					bitFieldData[bitFieldData.length - 1] |= (1 << (8 - finalIndex));
					fullDataResource[bitFieldData.length - 1] |= (1 << (8 - finalIndex));
					finalIndex--;
				}
			}
		} else {
			if (dataPiece % 8 != 0) {
				int finalIndex = (int) dataPiece % 8;
				fullDataResource[bitFieldData.length - 1] = 0;
				while (finalIndex != 0) {
					fullDataResource[bitFieldData.length - 1] |= (1 << (8 - finalIndex));
					finalIndex--;
				}
			}
		}
	}

	public static void pollAndStartOptimisticallyUnchokedNeighbour() {
		List<PeerClient> chokedNeighbours = new ArrayList<PeerClient>();

		for (PeerClient client : peerClients) {
			if (client.isRemotePeerClientInterested && client.isRemotePeerClientChoked) {
				chokedNeighbours.add(client);
			}
		}

		if (chokedNeighbours.isEmpty()) {
			optimallyUnchokedNeighbour = null;
		} else {
			optimallyUnchokedNeighbour = chokedNeighbours.get(new Random().nextInt(chokedNeighbours.size()));
		}
	}

	public static void listenToDataPeers(List<RemotePeer> peersConnected, PeerDataConfig peerDataConfig) {

		for (RemotePeer remotePeer : peersConnected) {
			try {
				PeerClient client = new PeerClient(new Socket(remotePeer.peerAddress, Integer.parseInt(remotePeer.peerPort)), true, remotePeer.peerId, peerDataConfig);
				client.start();
				peerClients.add(client);

				peerDataLogger.info("Peer " + peerProcessID + " Initiated/Connected to Peer " + remotePeer.peerId + ".");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	public void startOptimisticallyPreferredScheduler(int neighbourCount) {

		Runnable optimisticallyPreferredNeighbour = () -> {
			relistOptimisticNeighbours();
		};
		taskScheduleExecutor.scheduleAtFixedRate(optimisticallyPreferredNeighbour, neighbourCount, neighbourCount, TimeUnit.SECONDS);
	}

	public void startPrefferreNeighbourScheduler(int k, int p) {
		Runnable findPreferredNeibhbours = () -> {
			refreshPreferredNeighbours(k);
		};
		taskScheduleExecutor.scheduleAtFixedRate(findPreferredNeibhbours, p, p, TimeUnit.SECONDS);
	}

	public void refreshPreferredNeighbours(int noOfPreferreNeighbours) {
		try {
			Collections.sort(peerClients, (clientA, clientB) -> clientB.downloadSpeed.compareTo(clientA.downloadSpeed));
			int neighoutCount = 0;
			List<String> preferredNeighbours = new ArrayList<String>();

			for (PeerClient client : peerClients) {
				if (client.isRemotePeerClientInterested) {
					if (neighoutCount < noOfPreferreNeighbours) {
						if (client.isRemotePeerClientChoked) {
							client.isRemotePeerClientChoked = false;
							client.transmitMessage(client.messageUtil.fetchAndConstructUnChokeMessage());
						}
						preferredNeighbours.add(client.peerID);
					} else {

						if (!client.isRemotePeerClientChoked && client != optimallyUnchokedNeighbour) {
							client.isRemotePeerClientChoked = true;
							client.transmitMessage(client.messageUtil.fetchAndConstructChokeMessage());
						}
					}

					neighoutCount++;
				}
			}
			peerDataLogger.info("Peer " + peerProcessID + " has picked it's preferred neighbours:" + preferredNeighbours);
		} catch (Exception e) {
			System.err.println("Error while pickin Neighbours: "+e.getMessage());
		}
	}



	public void relistOptimisticNeighbours() {
		try {

			List<PeerClient> interestedChokedneighbouringPeers = new ArrayList<PeerClient>();

			for (PeerClient client : peerClients) {
				if (client.isRemotePeerClientInterested && client.isRemotePeerClientChoked) {
					interestedChokedneighbouringPeers.add(client);
				}
			}

			if (!interestedChokedneighbouringPeers.isEmpty()) {
				if (optimallyUnchokedNeighbour != null) {
					optimallyUnchokedNeighbour.isRemotePeerClientChoked = true;
					optimallyUnchokedNeighbour.transmitMessage(optimallyUnchokedNeighbour.messageUtil.fetchAndConstructChokeMessage());
				}
				optimallyUnchokedNeighbour = interestedChokedneighbouringPeers.get(new Random().nextInt(interestedChokedneighbouringPeers.size()));

				optimallyUnchokedNeighbour.isRemotePeerClientChoked = false;
				optimallyUnchokedNeighbour.transmitMessage(optimallyUnchokedNeighbour.messageUtil.fetchAndConstructUnChokeMessage());

			} else {
				if (optimallyUnchokedNeighbour != null) {
					if (!optimallyUnchokedNeighbour.isRemotePeerClientChoked) {
						optimallyUnchokedNeighbour.isRemotePeerClientChoked = true;
						optimallyUnchokedNeighbour.transmitMessage(optimallyUnchokedNeighbour.messageUtil.fetchAndConstructChokeMessage());
					}
					optimallyUnchokedNeighbour = null;
				}
			}

			if (optimallyUnchokedNeighbour != null)
				peerDataLogger.info("Peer: " + peerProcessID + " has the optimistically unchoked neighbor Peer: "
						+ optimallyUnchokedNeighbour.peerID);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	public void validateAndCloseSocket(boolean isTrasferred) {
		peerDataLogger.info("Is File Transferred: " + isTrasferred);
		if (isTrasferred && Arrays.equals(bitFieldData, fullDataResource)) {
			for (PeerClient client : peerClients) {
				client.validateStoppingCondition(true);
			}
			taskScheduleExecutor.shutdown();

			try {
				if (!peerServerSocket.isClosed())
					peerServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				peerDataLogger.info("Socket Didnot Close Properly");
			} finally {
				peerDataLogger.info("ShuttingDown the PeerProcess with Id: " + peerProcessID);
				System.exit(0);
			}
		}
	}

	public static void listenForProspectivePeers(List<RemotePeer> futurePeers, PeerDataConfig peerDataConfig) {
		try {
			for (RemotePeer remotePeer : futurePeers) {
				Runnable connectionToPeers = () -> {
					try {
						PeerClient futurePeer = new PeerClient(peerServerSocket.accept(), false, remotePeer.peerId, peerDataConfig);
						peerDataLogger.info("Peer " + peerProcessID + " is now Connected to " + remotePeer.peerId + ".");
						peerClients.add(futurePeer);
						futurePeer.start();
					} catch (IOException e) {
						peerDataLogger.info(e.getMessage());
					}
				};
				new Thread(connectionToPeers).start();
			}
		} catch (Exception ex) {
			peerDataLogger.info("Exception while listening to Prospective peers :" + ex.getMessage());
		}
	}

	public void checkFileIfComplete() {
		Runnable pollForPeerFile = () -> {
			validateAndCloseSocket(isFileWithAllPeers());
		};
		taskScheduleExecutor.scheduleAtFixedRate(pollForPeerFile, 10, 5, TimeUnit.SECONDS);
	}

	public boolean isFileWithAllPeers() {
		boolean isFileWithAllPeers = true;
		for (PeerClient client : peerClients) {
			if (!Arrays.equals(client.bitFieldClient, fullDataResource)) {
				peerDataLogger.info("Peer " + client.peerID + " didnot receive receive the full file.");
				isFileWithAllPeers = false;
				break;
			}
		}
		return isFileWithAllPeers;
	}

	public static void startTaskSchedulers(peerProcess peerProcessScheduler) {
		peerProcessScheduler.startPrefferreNeighbourScheduler(PeerDataConfig.peerCount, PeerDataConfig.preferredUnchokingTime);
		peerProcessScheduler.startOptimisticallyPreferredScheduler(PeerDataConfig.optimisticUnchokingInterval);
		peerProcessScheduler.checkFileIfComplete();
	}

	public static void main(String[] args) throws Exception {
		peerProcessID = Integer.parseInt(args[0]);
		peerDataLogger = ProcessLogger.fetchPeerDataLogger(peerProcessID);

		peerProcess peerProcessInstantiator = new peerProcess();
		PeerDataConfig peerDataConfig = new PeerDataConfig();

		List<RemotePeer> connectedPeers = new ArrayList<RemotePeer>();
		List<RemotePeer> peersYetToConnect = new ArrayList<RemotePeer>();

		boolean isFileAvailable = false;
		for (RemotePeer remotePeer : PeerDataConfig.peerList) {
			if (Integer.parseInt(remotePeer.peerId) < peerProcessID) {
				connectedPeers.add(remotePeer);
			} else if (Integer.parseInt(remotePeer.peerId) == peerProcessID) {
				peerProcessInstantiator.port = Integer.parseInt(remotePeer.peerPort);
				if (remotePeer.peerHasFile.equals("1"))
					isFileAvailable = true;
			} else {
				peersYetToConnect.add(remotePeer);
			}
		}

		bitFieldData = new byte[PeerDataConfig.byteCount];
		noOfPiecesRequested = new AtomicBoolean[PeerDataConfig.pieceCount];
		Arrays.fill(noOfPiecesRequested, new AtomicBoolean(false));

		fullDataResource = new byte[PeerDataConfig.byteCount];
		resourceDataPayload = new byte[PeerDataConfig.peerTransferFileSize];

		fetchAllInitialData(isFileAvailable, PeerDataConfig.pieceCount);

		listenToDataPeers(connectedPeers, peerDataConfig);

		peerServerSocket = new ServerSocket(peerProcessInstantiator.port);

		peerDataLogger.info("Socket Opened on port: " + peerProcessInstantiator.port);

		listenForProspectivePeers(peersYetToConnect, peerDataConfig);
		pollAndStartOptimisticallyUnchokedNeighbour();
		startTaskSchedulers(peerProcessInstantiator);
	}

}
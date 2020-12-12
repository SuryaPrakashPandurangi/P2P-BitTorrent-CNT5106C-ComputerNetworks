import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class startRemotePeers {
    private static final String peerInfoCfgFile = "PeerInfo.cfg";
    public static Map<String, RemotePeerInfo> remotePeerInfo = new HashMap<>();
    public List<Process> peerProcesses = new ArrayList<>();

    public Map<String, RemotePeerInfo> readPeerInfo() {
        Map<String, RemotePeerInfo> peers = new HashMap<String, RemotePeerInfo>();

        String st;

        try {
            BufferedReader in = new BufferedReader(new FileReader(peerInfoCfgFile));
            int i = 0;
            while ((st = in.readLine()) != null) {
                String[] tokens = st.split("\\s+");
                RemotePeerInfo remotePeer = new RemotePeerInfo(tokens[0],tokens[1],tokens[2],tokens[3]);

                i++;
                peers.put(tokens[0],remotePeer);
            }


            in.close();
        } catch (Exception ex) {
            System.err.println("Exception whle Starting Peers: "+ex.getMessage().toString());
        }

        return peers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("startRemotePeers{");
        sb.append("peerProcesses=").append(peerProcesses);
        sb.append('}');
        return sb.toString();
    }

    public static synchronized boolean isFinished() {

        String line;
        int hasFileCount = 1;

        try {
            BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while ((line = in.readLine()) != null) {
                hasFileCount = hasFileCount * Integer.parseInt(line.trim().split("\\s+")[3]);
            }
            if (hasFileCount == 0) {
                in.close();
                return false;
            } else {
                in.close();
                return true;
            }

        } catch (Exception e) {

            return false;
        }

    }

    public static boolean notHasFile(String peer){
        //System.out.println("Peer: "+peer);
        //System.out.println("PeerInfo: "+remotePeerInfo);

        int file = Integer.parseInt(remotePeerInfo.get(peer).getPeerHasFile());

        //System.out.println("Does it have file: "+file);

        if(file == 1){
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        try{
            startRemotePeers remotePeers = new startRemotePeers();

            remotePeerInfo = remotePeers.readPeerInfo();


            String path = System.getProperty("user.dir");
            System.out.println("Path: "+path);

            for(Map.Entry remotePeerEntry: remotePeerInfo.entrySet()){
                //if(!remotePeerEntry.getKey().equals(peerID)){
                //if(notHasFile((String) remotePeerEntry.getKey())){
                    System.out.println("Entry: "+remotePeerEntry.getKey());
                    RemotePeerInfo value = (RemotePeerInfo) remotePeerEntry.getValue();
                    System.out.println("Entry: "+value.getPeerAddress());


                    System.out.println("Start remote peer " + value.peerId +  " at " + value.peerAddress);

                    String command = "java peerProcess " + value.peerId;
                    //command = "ssh " + pInfo.getPeerAddress() + " cd " + path + "; java PeerProcess " + pInfo.getPeerId();
                    //System.out.println(command);
                    remotePeers.peerProcesses.add(Runtime.getRuntime().exec(command));
                //}

            }

            System.out.println("Waiting for remote peers to terminate.." );

            boolean isFinished = false;
            while(true)
            {
                // checks for termination
                isFinished = isFinished();
                if (isFinished)
                {
                    System.out.println("All peers are terminated!");
                    break;
                }
                else
                {
                    try {
                        Thread.currentThread();
                        Thread.sleep(120000);

                    } catch (InterruptedException ex) {
                    }
                    break;
                }
            }
        }catch(Exception ex){
            System.out.println("Error occured: "+ex.getMessage());
        }
    }
}

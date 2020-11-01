import com.whirvis.jraknet.RakNet;
import com.whirvis.jraknet.RakNetException;
import com.whirvis.jraknet.RakNetPacket;
import com.whirvis.jraknet.client.RakNetClient;
import com.whirvis.jraknet.client.RakNetClientListener;
import com.whirvis.jraknet.identifier.MinecraftIdentifier;
import com.whirvis.jraknet.peer.RakNetClientPeer;
import com.whirvis.jraknet.peer.RakNetServerPeer;
import com.whirvis.jraknet.protocol.Reliability;
import com.whirvis.jraknet.server.RakNetServer;
import com.whirvis.jraknet.server.RakNetServerListener;

import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws RakNetException {
        RakNetServer server = new RakNetServer(19135, 10);
        server.setIdentifier(new MinecraftIdentifier("Test", 408, "1.16.40", 1, 10, server.getGloballyUniqueId(), "", "Survival"));
        server.addListener(new RakNetServerListener() {
            @Override
            public void handleMessage(RakNetServer server, RakNetClientPeer peer, RakNetPacket packet, int channel) {
                System.out.println("Received message from client");
                server.shutdown();
                try {
                    InetSocketAddress address = new InetSocketAddress("127.0.0.1", 19133);
                    System.out.println("Identifier: " + RakNet.getServerIdentifier(address));

                    RakNetClient client = new RakNetClient();
                    client.addListener(new RakNetClientListener() {
                        @Override
                        public void onLogin(RakNetClient client, RakNetServerPeer peer) {
                            System.out.println("Logged in");

                            client.sendMessage(Reliability.RELIABLE_ORDERED, packet);
                            System.out.println("Resent packet");
                        }

                        @Override
                        public void handleMessage(RakNetClient client, RakNetServerPeer peer, RakNetPacket packet, int channel) {
                            System.out.println("Received message from server");
                            peer.disconnect();
                            System.exit(1);
                        }

                        @Override
                        public void onPeerException(RakNetClient client, RakNetServerPeer peer, Throwable throwable) {
                            throwable.printStackTrace();
                            peer.disconnect();
                            System.exit(-1);
                        }
                    });
                    client.connect(address);
                } catch (RakNetException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

            @Override
            public void onPeerException(RakNetServer server, RakNetClientPeer peer, Throwable throwable) {
                throwable.printStackTrace();
                peer.disconnect();
                System.exit(-1);
            }
        });
        server.start();
        System.out.println("Listening on port " + server.getPort());
    }
}

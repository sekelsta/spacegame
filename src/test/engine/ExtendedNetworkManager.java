package sekelsta.test.engine;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.NetworkDirection;
import sekelsta.engine.network.NetworkManager;

// Note: This could be improved by removing the requirement to wait between when a message is sent and when it arrives.
// As is with real sockets, there may be a small chance tests could fail even if the code is correct.
class ExtendedNetworkManager extends NetworkManager {
    public ExtendedNetworkManager(int port) {
        super(port);
        registerMessageType(ByteMessage::new);
    }

    @Override
    public void update(INetworked game) {
        super.update(game);
        // Take a break, make sure the sockets get a chance to update and the listener thread gets a chance to run
        try {
            Thread.sleep(10);
        }
        catch (InterruptedException e) {
            // Don't care
        }
    }

    void shortcutConnect(InetSocketAddress address) {
        addBroadcastRecipient(address);
    }

    InetSocketAddress getAddress() {
        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName("127.0.0.1");
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        return new InetSocketAddress(netAddress, socket.getLocalPort());
    }

    void becomeClient() {
        acceptDirection = NetworkDirection.SERVER_TO_CLIENT;
    }
}

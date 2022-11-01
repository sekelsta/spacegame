package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.Log;
import sekelsta.engine.network.Connection;

class TestNetworkConnectionRequired {
    private static class InitialByteMessage extends ByteMessage {
        public InitialByteMessage() {}

        public InitialByteMessage(byte[] bytes) {
            super(bytes);
        }

        @Override
        public boolean requiresConfirmedAddress() {
            return false;
        }
    }

    private ExtendedNetworkManager sender = new ExtendedNetworkManager(4321);
    private ExtendedNetworkManager receiver = new ExtendedNetworkManager(1234);
    private NetworkedGame senderGame = new NetworkedGame(sender);
    private NetworkedGame receiverGame = new NetworkedGame(receiver);

    public TestNetworkConnectionRequired() {
        sender.registerMessageType(InitialByteMessage::new);

        receiver.registerMessageType(InitialByteMessage::new);

        sender.shortcutConnect(receiver.getAddress());
        // Purposely not calling receiver.shortcutConnect(sender.getAddress());

        sender.start();
        receiver.start();
    }

    @AfterEach
    void tearDown() {
        sender.close();
        receiver.close();
        Connection.closeAll();
    }

    @Test
    void sendInitialMessageWithoutConnection() {
        ByteMessage message = new InitialByteMessage(new byte[10]);
        sender.queueBroadcast(message);
        senderGame.update();
        receiverGame.update();
        assertEquals(1, receiverGame.handledTestMessages.size());
    }

    @Test
    void sendMessageWithoutConnection() {
        ByteMessage message = new ByteMessage(new byte[10]);
        sender.queueBroadcast(message);
        senderGame.update();
        receiverGame.update();
        assertEquals(0, receiverGame.handledTestMessages.size());
    }
}

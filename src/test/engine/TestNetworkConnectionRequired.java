package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.Log;

class TestNetworkConnectionRequired {
    private static class InitialByteMessage extends ByteMessage {
        public InitialByteMessage() {}

        public InitialByteMessage(byte[] bytes) {
            super(bytes);
        }

        @Override
        public boolean requiresConnection() {
            return false;
        }
    }

    private ExtendedNetworkManager sender;
    private ExtendedNetworkManager receiver;
    private NetworkedGame senderGame;
    private NetworkedGame receiverGame;

    public TestNetworkConnectionRequired() {
        sender = new ExtendedNetworkManager(4321);
        sender.registerMessageType(InitialByteMessage::new);
        receiver = new ExtendedNetworkManager(1234);
        receiver.registerMessageType(InitialByteMessage::new);

        sender.shortcutConnect(receiver.getAddress());
        // Purposely not calling receiver.shortcutConnect(sender.getAddress());

        senderGame = new NetworkedGame();
        senderGame.networkManager = sender;

        receiverGame = new NetworkedGame();
        receiverGame.networkManager = receiver;

        sender.start();
        receiver.start();
    }

    @AfterEach
    void tearDown() {
        sender.close();
        receiver.close();
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

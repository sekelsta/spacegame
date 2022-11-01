package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.network.Connection;
import sekelsta.engine.network.Message;

class TestMessageSize {
    private ExtendedNetworkManager network = new ExtendedNetworkManager(0);
    private NetworkedGame networkedGame = new NetworkedGame(network);

    public TestMessageSize() {
        network.shortcutConnect(network.getAddress());
        network.start();
    }

    @AfterEach
    void tearDown() {
        network.close();
        Connection.closeAll();
    }

    @Test
    void sanityCheckSendingWorks() {
        ByteMessage message = new ByteMessage(new byte[10]);
        network.queueBroadcast(message);
        // One update to send, one to receive
        networkedGame.update();
        networkedGame.update();
        assertEquals(1, networkedGame.handledTestMessages.size());
    }

    @Test
    void sendManyLargeMessages() {
        final byte NUM_MESSAGES = 3;
        final int SIZE = Connection.BUFFER_SIZE / NUM_MESSAGES;
        byte[][] messageContents = new byte[NUM_MESSAGES][SIZE];
        Random random = new Random();
        for (byte i = 0; i < NUM_MESSAGES; ++i) {
            random.nextBytes(messageContents[i]);
            messageContents[i][0] = i;
            ByteMessage message = new ByteMessage(messageContents[i]);
            network.queueBroadcast(message);
        }
        networkedGame.update();
        networkedGame.update();
        assertEquals(NUM_MESSAGES, networkedGame.handledTestMessages.size());
        // Packets may not arrive in-order, so use the first byte as the index
        for (int i = 0; i < NUM_MESSAGES; ++i) {
            Message message = networkedGame.handledTestMessages.get(i);
            byte[] bytes = ((ByteMessage)message).bytes;
            assertTrue(Arrays.equals(messageContents[bytes[0]], ((ByteMessage)message).bytes));
        }
    }
}

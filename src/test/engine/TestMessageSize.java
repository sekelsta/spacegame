package sekelsta.test.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sekelsta.engine.network.Message;
import sekelsta.engine.network.NetworkSender;

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
        final int NUM_MESSAGES = 3;
        final int SIZE = NetworkSender.BUFFER_SIZE / NUM_MESSAGES;
        byte[][] messageContents = new byte[NUM_MESSAGES][SIZE];
        Random random = new Random();
        for (int i = 0; i < NUM_MESSAGES; ++i) {
            random.nextBytes(messageContents[i]);
            ByteMessage message = new ByteMessage(messageContents[i]);
            network.queueBroadcast(message);
        }
        networkedGame.update();
        networkedGame.update();
        assertEquals(NUM_MESSAGES, networkedGame.handledTestMessages.size());
        // Yes, we do expect them to arrive in-order
        for (int i = 0; i < NUM_MESSAGES; ++i) {
            Message message = networkedGame.handledTestMessages.get(i);
            assertTrue(Arrays.equals(messageContents[i], ((ByteMessage)message).bytes));
        }
    }
}

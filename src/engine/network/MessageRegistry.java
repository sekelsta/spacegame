package sekelsta.engine.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MessageRegistry {

    public static class MessageTypeNotRegisteredException extends Exception {
        public MessageTypeNotRegisteredException(int type) {
            super("Message type not registered: " + type);
        }
    }

    private List<Supplier<Message>> messageSuppliers = new ArrayList<>();
    private Map<Class<? extends Message>, Integer> messageClasses = new HashMap<>();

    private boolean frozen = false;

    public void registerMessageType(Supplier<Message> messageSupplier) {
        assert(!frozen);
        messageClasses.put(messageSupplier.get().getClass(), messageSuppliers.size());
        messageSuppliers.add(messageSupplier);
    }

    public void freeze() {
        frozen = true;
    }

    public Message createMessage(int type) 
        throws MessageTypeNotRegisteredException
    {
        try {
            return messageSuppliers.get(type).get();
        }
        catch (IndexOutOfBoundsException e) {
            throw new MessageTypeNotRegisteredException(type);
        }
    }

    public int getMessageType(Message message) {
        return messageClasses.get(message.getClass());
    }
}

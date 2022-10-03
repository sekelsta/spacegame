package sekelsta.engine.network;

public class MessageParsingException extends RuntimeException {
    public MessageParsingException() {
        super();
    }

    public MessageParsingException(String message) {
        super(message);
    }

    public MessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageParsingException(Throwable cause) {
        super(cause);
    }
}

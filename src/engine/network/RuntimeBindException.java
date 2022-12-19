package sekelsta.engine.network;

public class RuntimeBindException extends RuntimeException {
    public RuntimeBindException() {
        super();
    }

    public RuntimeBindException(String message) {
        super(message);
    }

    public RuntimeBindException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuntimeBindException(Throwable cause) {
        super(cause);
    }
}

package no.obos.util.servicebuilder.mq;

public class MessageQueueException extends RuntimeException {

    public MessageQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageQueueException(Throwable cause) {
        super(cause);
    }

}

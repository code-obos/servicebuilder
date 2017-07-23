package no.obos.util.servicebuilder.mq;

/**
 * Interface for handling messages to be implemented by application
 */
public interface MessageHandler<T> {
    void handle(T message, MessageMeta meta);
}

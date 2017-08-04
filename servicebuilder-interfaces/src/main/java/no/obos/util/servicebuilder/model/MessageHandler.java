package no.obos.util.servicebuilder.model;

/**
 * Interface for handling messages to be implemented by application
 */
public interface MessageHandler<T> {
    void handle(T message, MessageMetadata meta);
}

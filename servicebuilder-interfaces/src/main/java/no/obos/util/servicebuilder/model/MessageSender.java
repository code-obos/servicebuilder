package no.obos.util.servicebuilder.model;

/**
 * Interface for sending messages. Is available as injection candidate.
 */
public interface MessageSender<T> {
    void send(T message);
}

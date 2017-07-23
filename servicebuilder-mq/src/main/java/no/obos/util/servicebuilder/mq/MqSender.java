package no.obos.util.servicebuilder.mq;

/**
 * Interface for sending messages. Is available as injection candidate.
 */
public interface MqSender<T> {
    void send(T message);
}

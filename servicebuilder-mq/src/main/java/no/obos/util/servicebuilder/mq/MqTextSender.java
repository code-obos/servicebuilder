package no.obos.util.servicebuilder.mq;

/**
 * Interface for queueing message. Should be provided as injection target by underlying queueing system.
 */
public interface MqTextSender {
    void queueMessage(String message, String queue);
}

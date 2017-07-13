package no.obos.util.servicebuilder.mq;

public interface MessageQueueListener {
    void receiveMessages(MessageHandler handler);

    void requeueFailedMessages();

    int getErrorQueueSize();
}

package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.JsonNode;

public interface MessageQueueSender {
    void queueMessage(JsonNode message);
}

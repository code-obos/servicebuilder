package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.JsonNode;

public interface MessageHandler {
    void handle(JsonNode message);
}

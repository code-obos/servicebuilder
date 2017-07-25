package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static no.obos.util.servicebuilder.model.Constants.X_OBOS_REQUEST_ID;

/**
 * Parses messages and forwards to handler. Also handles requestid. Should log any errors as underlying handler
 * does not know requestid and is responsible only for behavioral error handling
 */
@Slf4j
@AllArgsConstructor
public class MqHandlerForwarder {
    final ObjectMapper objectMapper;

    public <T> void forward(MqHandlerImpl<T> handler, String messageText) {
        MqMessage<T> message = parseMessage(handler.handlerDescription, messageText);
        String requestId = readRequestId(messageText, message);
        MDC.put(X_OBOS_REQUEST_ID, requestId);
        try {
            forwardMessage(handler, message);
        } finally {
            MDC.remove(X_OBOS_REQUEST_ID);
        }
    }

    private <T> String readRequestId(String messageText, MqMessage<T> message) {
        String requestId = message.requestId != null
                ? message.requestId
                : UUID.randomUUID().toString();
        if (message.requestId == null) {
            log.warn("Message did not contain requestid: " + messageText);
        }
        return requestId;
    }

    private <T> MqMessage<T> parseMessage(HandlerDescription<T> handlerDescription, String messageText) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(MqMessage.class, handlerDescription.messageDescription.MessageType);
        try {
            return objectMapper.readValue(messageText, javaType);
        } catch (IOException e) {
            log.error("Problem parsing text of message."
                    + "\nType of message: " + handlerDescription.messageDescription.MessageType.getName()
                    + "\nMessage: " + messageText);
            throw new RuntimeException("Problem parsing message text");
        }
    }

    private <T> void forwardMessage(MqHandlerImpl<T> handler, MqMessage<T> message) {
        MessageMeta messageMeta = MessageMeta.builder()
                .requestId(message.requestId)
                .sourceApp(message.sourceApp)
                .build();

        MessageHandler<T> messageHandler = handler.messageHandler;

        try {
            messageHandler.handle(message.content, messageMeta);
        } catch (RuntimeException ex) {
            log.error("Exception during processing message.", ex);
            throw ex;
        }
    }
}

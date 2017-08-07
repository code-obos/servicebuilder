package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.MessageSender;
import org.slf4j.MDC;

@AllArgsConstructor
@Builder
@Slf4j
public class MessageSenderImpl<T> implements MessageSender<T> {

    private final MqTextSender mqTextSender;
    private final MessageDescription<T> messageDescription;
    private final String senderName;

    @Override
    public void send(T message) {
        log.info("Sending message to queue " + messageDescription.getQueueName() + ": " + message.toString());
        MqMessage<T> mqMessage = MqMessage.<T>builder()
                .content(message)
                .requestId(MDC.get(Constants.X_OBOS_REQUEST_ID))
                .sourceApp(senderName)
                .build();
        String serializedMessage;
        try {
            serializedMessage = messageDescription.jsonConfig.get().writeValueAsString(mqMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        mqTextSender.queueMessage(serializedMessage, messageDescription.getQueueName());
    }

}

package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.MessageDescription;
import org.slf4j.MDC;

@AllArgsConstructor
@Builder
@Slf4j
public class MqSenderImpl<T> implements MqSender<T> {
    private final MqTextSender mqTextSender;
    private final ObjectMapper objectMapper;
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
            serializedMessage = objectMapper.writeValueAsString(mqMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        mqTextSender.queueMessage(serializedMessage, messageDescription.getQueueName());
    }
}

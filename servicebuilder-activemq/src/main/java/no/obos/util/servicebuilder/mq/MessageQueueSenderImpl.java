package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;

import javax.jms.Session;

@Slf4j
public class MessageQueueSenderImpl implements MessageQueueSender {

    private final String url;
    private final String user;
    private final String password;
    private final String queue;

    public MessageQueueSenderImpl(String url, String user, String password, String queue) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.queue = queue;
    }

    public void queueMessage(JsonNode message) {
        queueMessage(message.toString());
    }

    private void queueMessage(String message) {
        log.debug("Connecting to {}", url);
        ActiveMQConnection connection = MessageQueueUtils.openConnection(user, password, url);
        Session session = MessageQueueUtils.startSession(connection);

        try {
            log.info("Sending message '{}' to {}", message, queue);
            MessageQueueUtils.queueMessage(session, message, queue);
        } finally {
            MessageQueueUtils.closeConnection(session, connection);
        }
    }

}

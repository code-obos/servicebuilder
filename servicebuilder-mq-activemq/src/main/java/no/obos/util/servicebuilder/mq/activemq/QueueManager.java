package no.obos.util.servicebuilder.mq.activemq;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MessageQueueException;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides statistics and requeue operations for a activemq queues. Available for injection for use in clients.
 */
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class QueueManager {

    @Inject
    private final ActiveMqConnectionProvider activeMqConnectionProvider;
    private static final long REQUEUE_TIMEOUT = 1000;

    public void requeueFailedMessages(HandlerDescription handlerDescription) {
        activeMqConnectionProvider.inSession(session -> {
            String queueError = handlerDescription.messageDescription.getErrorQueueName();
            String queueInput = handlerDescription.messageDescription.getQueueName();
            int count = getQueueSize(session, queueError);

            if (count < 1) {
                return;
            }

            log.info("Requeuing {} failed messages...", count);

            try {
                Queue queueErr = session.createQueue(queueError);
                MessageConsumer consumer = session.createConsumer(queueErr);

                Queue queueRetry = session.createQueue(queueInput);
                MessageProducer producer = session.createProducer(queueRetry);

                for (int consumed = 0; consumed < count; consumed++) {
                    TextMessage message = (TextMessage) consumer.receive(REQUEUE_TIMEOUT);

                    if (message == null) {
                        continue;
                    }

                    String text = message.getText();

                    log.info("Requeuing message '{}'", text);

                    try {
                        TextMessage newMessage = session.createTextMessage(text);

                        producer.send(newMessage);
                        message.acknowledge();
                    } catch (Exception e) {
                        log.error("Failed to requeue message", e);
                    }

                    session.commit();
                }

                producer.close();
                consumer.close();

            } catch (JMSException ex) {
                throw new MessageQueueException(ex);
            }
        });

    }

    public int getErrorQueueSize(HandlerDescription handlerDescription) {
        return activeMqConnectionProvider.inSessionWithReturn(session ->
                getQueueSize(session, handlerDescription.messageDescription.getErrorQueueName())
        );
    }

    private int getQueueSize(Session session, String queueName) {
        String size = queryQueueStatistics(session, queueName).get("size");
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            throw new MessageQueueException("Failed to read queue size for " + queueName, e);
        }
    }

    private Map<String, String> queryQueueStatistics(Session session, String queueName) {
        try {
            String replyQueueName = "ActiveMQ.Statistics.Destination." + queueName;
            TemporaryQueue replyQueue = session.createTemporaryQueue();
            Queue queryQueue = session.createQueue(replyQueueName);

            MessageProducer producer = session.createProducer(queryQueue);
            Message emptyMessage = session.createMessage();
            emptyMessage.setJMSReplyTo(replyQueue);
            producer.send(emptyMessage);

            MessageConsumer consumer = session.createConsumer(replyQueue);
            MapMessage reply = (MapMessage) consumer.receive(REQUEUE_TIMEOUT);

            if (reply == null) {
                return Collections.emptyMap();
            }

            Map<String, String> statistics = new HashMap<>();
            for (Enumeration<?> e = reply.getMapNames(); e.hasMoreElements(); ) {
                String name = e.nextElement().toString();
                statistics.put(name, reply.getString(name));
            }
            return statistics;
        } catch (JMSException e) {
            throw new MessageQueueException("Could not query statistics for " + queueName, e);
        }
    }
}

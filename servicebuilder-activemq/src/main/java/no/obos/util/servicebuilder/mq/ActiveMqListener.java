package no.obos.util.servicebuilder.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;
import org.slf4j.MDC;

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
import java.util.UUID;

import static no.obos.util.servicebuilder.model.Constants.X_REQUEST_ID;


@Slf4j
public class ActiveMqListener implements MessageQueueListener {

    private static final long REQUEUE_TIMEOUT = 1000;

    private final String url;
    private final String user;
    private final String password;
    private final String queueInput;
    private final String queueError;

    private boolean listenerStarted;

    public ActiveMqListener(String url, String user, String password, String queueInput, String queueError) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.queueInput = queueInput;
        this.queueError = queueError;
    }

    @Override
    public void receiveMessages(MessageHandler handler) {
        if (listenerStarted) {
            throw new RuntimeException("Listener has already been started");
        }
        listenerStarted = true;

        startListener(handler);
    }

    private void startListener(MessageHandler handler) {
        log.debug("Starting listener...");
        try {
            ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(queueInput);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(message -> handleMessage(handler, message, session));
            log.debug("Listening to {}", queueInput);
        } catch (Exception e) {
            log.error("Error starting listening to queue {}", queueInput, e);
        }
    }

    private void handleMessage(MessageHandler handler, Message message, Session session) {
        if (! (message instanceof TextMessage)) {
            return;
        }
        TextMessage textMessage = (TextMessage) message;
        String text = null;
        String requestId = UUID.randomUUID().toString();
        try {
            text = textMessage.getText();
            if (! Strings.isNullOrEmpty(message.getJMSCorrelationID())) {
                requestId = message.getJMSCorrelationID();
            }

            MDC.put(X_REQUEST_ID, requestId);

            log.info("Received message '{}'", text);

            handler.handle(new ObjectMapper().readTree(text));
        } catch (Exception e) {
            log.error("Failed to process message", e);
            try {
                TextMessage errorMessage = session.createTextMessage(text);
                errorMessage.setJMSCorrelationID(requestId);

                Queue queue = session.createQueue(queueError);
                MessageProducer errorProducer = session.createProducer(queue);
                errorProducer.send(errorMessage);
            } catch (JMSException jmse) {
                log.error("Failed to create error message", jmse);
            }
        } finally {
            MDC.remove(X_REQUEST_ID);
        }
    }

    @Override
    public void requeueFailedMessages() {
        try {
            ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);
            Session session = ActiveMqUtils.startSession(connection);

            int count = getQueueSize(session, queueError);

            if (count < 1) {
                return;
            }

            log.info("Requeuing {} failed messages...", count);

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
                String requestId = message.getJMSCorrelationID();

                log.info("Requeuing message '{}'", text);

                try {
                    TextMessage newMessage = session.createTextMessage(text);
                    newMessage.setJMSCorrelationID(requestId);

                    producer.send(newMessage);
                } catch (Exception e) {
                    log.error("Failed to requeue message", e);
                }

                message.acknowledge();
                session.commit();
            }

            producer.close();
            consumer.close();
        } catch (JMSException ex) {
            throw new MessageQueueException("Failed to requeue failed messages", ex);
        }
    }

    @Override
    public int getErrorQueueSize() {
        ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);
        Session session = ActiveMqUtils.startSession(connection);
        return getQueueSize(session, queueError);
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

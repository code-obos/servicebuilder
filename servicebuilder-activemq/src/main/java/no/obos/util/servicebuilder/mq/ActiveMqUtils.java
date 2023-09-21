package no.obos.util.servicebuilder.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.net.URISyntaxException;

import static no.obos.util.servicebuilder.model.Constants.X_OBOS_REQUEST_ID;

@Slf4j
class ActiveMqUtils {

    private static final String BROKER_URL_JUNIT = "vm://localhost";

    static void queueMessage(Session session, String text, String queueName) {
        try {
            Queue queue = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(queue);

            TextMessage message = session.createTextMessage(text);
            message.setJMSCorrelationID(MDC.get(X_OBOS_REQUEST_ID));

            producer.send(message);
            session.commit();
        } catch (JMSException ex) {
            throw new MessageQueueException("Could not queue message '" + text + "'", ex);
        }
    }

    static ActiveMQConnection openConnection(String user, String password, String url) {
        String brokerUrl = brokerUrl(url);
        try {
            ActiveMQConnection connection = ActiveMQConnection.makeConnection(user, password, brokerUrl);
            connection.start();
            return connection;
        } catch (JMSException | URISyntaxException ex) {
            throw new MessageQueueException("Could not establish connection to ActiveMQ", ex);
        }
    }

    static String brokerUrl(String url) {
        if (url.startsWith(BROKER_URL_JUNIT)) {
            return url;
        }

        return url.startsWith("failover:") ? url : "failover:" + url;
    }

    static Session startSession(ActiveMQConnection connection) {
        try {
            return connection.createSession(true, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException ex) {
            throw new MessageQueueException("Could not start ActiveMQ session ", ex);
        }
    }

    static void closeConnection(Session session, ActiveMQConnection connection) {
        try {
            session.close();
            connection.close();
        } catch (JMSException ex) {
            throw new MessageQueueException("Could not close session connection", ex);
        }
    }

    static String truncateMessageForLogging(String text) {
        if (StringUtils.isEmpty(text) || text.length() <= ActiveMqListener.MAX_LENGTH_PER_MESSAGE) {
            return text;
        }
        try {
            log.info("Truncating message for logging...");
            String truncatedText = text.substring(0, ActiveMqListener.MAX_LENGTH_PER_MESSAGE);
            String dataKey = "\"data\":";
            if (truncatedText.contains(dataKey)) {
                return truncatedText.substring(0, truncatedText.indexOf(dataKey)) + dataKey + "\"...\"}";
            }
            return truncatedText;
        } catch (Exception e) {
            log.warn("Failed to truncate message for logging");
            return text;
        }
    }
}

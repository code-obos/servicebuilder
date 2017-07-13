package no.obos.util.servicebuilder.mq;

import org.apache.activemq.ActiveMQConnection;
import org.slf4j.MDC;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.net.URISyntaxException;

import static no.obos.util.servicebuilder.model.Constants.X_OBOS_REQUEST_ID;

class ActiveMqUtils {

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
        try {
            ActiveMQConnection connection = ActiveMQConnection.makeConnection(user, password, url);
            connection.start();
            return connection;
        } catch (JMSException | URISyntaxException ex) {
            throw new MessageQueueException("Could not establish connection to ActiveMQ", ex);
        }
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

}

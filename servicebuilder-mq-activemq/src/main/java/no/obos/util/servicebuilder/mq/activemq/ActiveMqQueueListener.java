package no.obos.util.servicebuilder.mq.activemq;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.mq.MqHandlerForwarder;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;


/**
 * Listens to a single activemq queue and forwards message to handler.
 * Startup and reconnect is handled by ActiveMqListener. Session is shared between queues.
 */
@Slf4j
@Builder
public class ActiveMqQueueListener {



    private final Session session;
    private final MqHandlerForwarder mqHandlerForwarder;
    private final MqHandlerImpl<?> handler;



    public void startListener() throws JMSException {

        Queue queue = session.createQueue(handler.handlerDescription.messageDescription.getQueueName());
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(it -> this.handleMessage(it, session));
        log.debug("Listening to {}", handler.handlerDescription.messageDescription.getQueueName());
    }


    private void handleMessage(Message message, Session session) {
        if (! (message instanceof TextMessage)) {
            log.error("Expected text message, got: ", message.getClass().getName());
            return;
        }
        TextMessage textMessage = (TextMessage) message;
        String text = null;
        try {
            text = textMessage.getText();
        } catch (JMSException e) {
            log.error("Could not read text from message", e);
            toErrorQueue(message);
            ActiveMqUtils.commitSession(session);
        }
        try {
            mqHandlerForwarder.forward(handler, text);
            ActiveMqUtils.commitSession(session);
        } catch (RuntimeException ex) {
            log.info("Problem forwarding message. Prober logging should be logged by MqHandlerForwarder.");
            toErrorQueue(text);
            ActiveMqUtils.commitSession(session);
        }
    }

    private void toErrorQueue(String text) {
        try {
            log.info("Sending message to error queue: " + text);
            TextMessage errorMessage = session.createTextMessage(text);
            Queue queue = session.createQueue(handler.handlerDescription.messageDescription.getErrorQueueName());
            MessageProducer errorProducer = session.createProducer(queue);
            errorProducer.send(errorMessage);
            errorProducer.close();
        } catch (JMSException jmse) {
            log.error("Failed to create error message", jmse);
        }
    }

    private void toErrorQueue(Message message) {
        try {
            log.info("Sending message to error queue. Message, text not available. Message id: " + message.getJMSMessageID());
            Queue queue = session.createQueue(handler.handlerDescription.messageDescription.getErrorQueueName());
            MessageProducer errorProducer = session.createProducer(queue);
            errorProducer.send(message);
            errorProducer.close();
        } catch (JMSException jmse) {
            log.error("Failed to create error message", jmse);
        }
    }

}

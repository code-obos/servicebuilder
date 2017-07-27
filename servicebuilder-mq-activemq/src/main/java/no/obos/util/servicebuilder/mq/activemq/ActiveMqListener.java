package no.obos.util.servicebuilder.mq.activemq;

import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MqHandlerForwarder;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;
import no.obos.util.servicebuilder.mq.MqListener;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Handles connecting several listeners to activemq in single session, reconnection and status for monitoring.
 */
@Slf4j
@Builder
public class ActiveMqListener implements MqListener {
    private final ActiveMqConnectionProvider activeMqConnectionProvider;

    private final ImmutableSet<HandlerDescription<?>> handlerDescriptions;
    private final MqHandlerForwarder mqHandlerForwarder;

    @Getter
    private boolean listenerActive = false;

    private boolean abort = false;

    private Connection connection = null;
    private Session session = null;

    ImmutableSet<MqHandlerImpl<?>> handlers;

    public void setHandlers(Iterable<MqHandlerImpl<?>> handlers) {
        this.handlers = ImmutableSet.copyOf(handlers);
    }

    public void startListener() {
        log.debug("Starting listener...");
        if (listenerActive) {
            throw new RuntimeException("Multiple active sessions in same listener. Check if starting connection threw exception and ActiveMQ ActiveMQConnection.setExceptionListener() failed at the same time.");
        }
        abort = false;
        try {
            activeMqConnectionProvider.startListenerSession((connection, session) -> {
                this.connection = connection;
                this.session = session;
                for (MqHandlerImpl<?> handlerDescription : handlers) {
                    try {
                        ActiveMqQueueListener activeMqListener = ActiveMqQueueListener.builder()
                                .handler(handlerDescription)
                                .session(session)
                                .mqHandlerForwarder(mqHandlerForwarder)
                                .build();
                        activeMqListener.startListener();
                        listenerActive = true;
                    } catch (JMSException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, () -> restartListener(handlers));
        } catch (RuntimeException e) {
            this.session = null;
            log.error("Caught exception during start of listener, restarting");
            restartListener(handlers);
        }
    }

    private void restartListener(ImmutableSet<MqHandlerImpl<?>> handlers) {
        log.info("Restarting activeMQ connection in 10 seconds.");
        listenerActive = false;
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            log.info("Interrupted.");
            Thread.currentThread().interrupt();
            abort = true;
        }
        if (! abort) {
            startListener();
        }
    }

    public void stop() {
        try {
            session.close();
        } catch (JMSException e) {
            log.warn("Problem stopping session", e);
        }
        try {
            connection.close();
        } catch (JMSException e) {
            log.warn("Problem stopping connection", e);
        }
    }
}
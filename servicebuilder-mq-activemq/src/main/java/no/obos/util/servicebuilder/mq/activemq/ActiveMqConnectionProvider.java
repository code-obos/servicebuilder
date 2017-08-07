package no.obos.util.servicebuilder.mq.activemq;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.mq.MessageQueueException;
import org.apache.activemq.ActiveMQConnection;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static no.obos.util.servicebuilder.mq.activemq.ActiveMqUtils.closeConnection;
import static no.obos.util.servicebuilder.mq.activemq.ActiveMqUtils.closeConnectionNoException;

/**
 * Handles connections to activeMQ so other classes wont have to deal with connection-specific information.
 */
@Slf4j
@Builder
public class ActiveMqConnectionProvider {

    private final String url;
    private final String user;
    private final String password;

    public void startListenerSession(BiConsumer<Connection, Session> listener, ErrorCallback onError) {
        ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);

        ExceptionListener onErrorWithClose = (JMSException ex) -> {
            log.error("Problem with ActiveMQ session, closing connection and forwarding", ex);
            closeConnectionNoException(connection);
            onError.onError();
        };
        try {
            connection.setExceptionListener(onErrorWithClose);
        } catch (JMSException e) {
            closeConnection(connection);
            throw new MessageQueueException(e);
        }
        Session session = ActiveMqUtils.startSession(connection);
        listener.accept(connection, session);
    }

    public <T> T inSessionWithReturn(Function<Session, T> fun) {
        ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);
        Session session = ActiveMqUtils.startSession(connection);
        T ret = fun.apply(session);
        closeSession(connection, session);
        closeConnection(connection);
        return ret;
    }

    public void inSession(Consumer<Session> fun) {
        ActiveMQConnection connection = ActiveMqUtils.openConnection(user, password, url);
        Session session = ActiveMqUtils.startSession(connection);
        fun.accept(session);
        closeSession(connection, session);
        closeConnection(connection);
    }

    private void closeSession(ActiveMQConnection connection, Session session) {
        try {
            session.commit();
            session.close();
        } catch (JMSException e) {
            closeConnection(connection);
            throw new MessageQueueException(e);
        }
    }


    public interface ErrorCallback {
        void onError();
    }
}

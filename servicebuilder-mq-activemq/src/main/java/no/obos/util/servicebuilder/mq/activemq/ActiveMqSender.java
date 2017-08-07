package no.obos.util.servicebuilder.mq.activemq;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.mq.MqTextSender;

/**
 * Forwards messages as text to activeMQ. Applications should inject MQSender instead of using directly.
 */
@Slf4j
@AllArgsConstructor
public class ActiveMqSender implements MqTextSender {

    private final ActiveMqConnectionProvider activeMqConnectionProvider;

    public void queueMessage(String message, String queue) {
        activeMqConnectionProvider.inSession(session ->
                ActiveMqUtils.queueMessage(session, message, queue)
        );
    }

}

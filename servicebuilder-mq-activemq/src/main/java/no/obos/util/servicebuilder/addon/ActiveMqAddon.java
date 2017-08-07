package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.metrics.healthcheck.ObosHealthCheckResult;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.MessageDescription;
import no.obos.util.servicebuilder.model.MessageSender;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.MessageSenderImpl;
import no.obos.util.servicebuilder.mq.MqListener;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqConnectionProvider;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqListener;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqSender;
import no.obos.util.servicebuilder.mq.activemq.QueueManager;
import org.glassfish.hk2.api.TypeLiteral;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ActiveMQ backend for MqAddon
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActiveMqAddon implements Addon {

    public static final String CONFIG_KEY_URL = "queue.url";
    public static final String CONFIG_KEY_USER = "queue.user";
    public static final String CONFIG_KEY_PASSWORD = "queue.password";

    @Wither(AccessLevel.PRIVATE)
    public final ActiveMqConnectionProvider connectionProvider;

    @Wither(AccessLevel.PRIVATE)
    public final ActiveMqListener listener;

    @Wither(AccessLevel.PRIVATE)
    public final MqAddon mqAddon;

    @Wither(AccessLevel.PRIVATE)
    public final String url;
    @Wither(AccessLevel.PRIVATE)
    public final String user;
    @Wither(AccessLevel.PRIVATE)
    public final String password;

    @Wither(AccessLevel.PRIVATE)
    public final ServiceDefinition serviceDefinition;

    public static ActiveMqAddon defaults = new ActiveMqAddon(null, null, null, null, null, null, null);

    @Override
    public ActiveMqAddon initialize(ServiceConfig serviceConfig) {
        MqAddon mqAddon = serviceConfig.requireAddonInstance(MqAddon.class);

        ActiveMqConnectionProvider connectionProvider = ActiveMqConnectionProvider.builder()
                .url(url)
                .user(user)
                .password(password)
                .build();

        ActiveMqListener listener = new ActiveMqListener(connectionProvider, mqAddon.mqHandlerForwarder);

        return this
                .withMqAddon(mqAddon)
                .withConnectionProvider(connectionProvider)
                .withListener(listener)
                .withServiceDefinition(serviceConfig.serviceDefinition)
                ;
    }

    @Override
    public void cleanUp() {
        listener.stop();
    }


    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        ActiveMqSender activeMqSender = new ActiveMqSender(this.connectionProvider);
        serviceConfig.addBinder((binder) -> {
            binder.bind(this.connectionProvider).to(ActiveMqConnectionProvider.class);
            binder.bind(this.listener).to(MqListener.class);
            binder.bindAsContract(QueueManager.class);

            ImmutableMap<String, MessageSender> senderMap = ImmutableMap.copyOf(
                    mqAddon.senders.stream()
                            .collect(Collectors.toMap(
                                    md -> md.MessageType.getName(),
                                    md -> getMqSender(activeMqSender, md)
                            ))
            );
            binder.bind(senderMap).to(new TypeLiteral<Map<String, MessageSender>>() {});
        });


    }

    private <T> MessageSenderImpl<T> getMqSender(ActiveMqSender activeMqSender, MessageDescription<T> messageDescription) {
        return MessageSenderImpl.<T>builder()
                .messageDescription(messageDescription)
                .mqTextSender(activeMqSender)
                .senderName(serviceDefinition.getName())
                .build();
    }


    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        mqAddon.handlers.forEach(handlerDescription -> {
            String queueInput = handlerDescription.messageDescription.getQueueName();
            String queueError = handlerDescription.messageDescription.getErrorQueueName();
            ObosHealthCheckRegistry.registerActiveMqCheck("Input queue: " + queueInput + " on " + url, url, queueInput, handlerDescription.healthCheckEntriesMax, handlerDescription.healthCheckEntriesGrace, user, password);
            ObosHealthCheckRegistry.registerActiveMqCheck("Error queue: " + queueError + " on " + url, url, queueError, 1, 1, user, password);
        });
        ObosHealthCheckRegistry.registerCustomCheck("ActiveMqListener active", () ->
                listener.isListenerActive()
                        ? ObosHealthCheckResult.ok()
                        : ObosHealthCheckResult.error("ActiveMqListener not active")
        );
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {

        if (url == null) {
            properties.failIfNotPresent(
                    CONFIG_KEY_URL,
                    CONFIG_KEY_USER,
                    CONFIG_KEY_PASSWORD
            );

            return this
                    .url(properties.get(CONFIG_KEY_URL))
                    .user(properties.get(CONFIG_KEY_USER))
                    .password(properties.get(CONFIG_KEY_PASSWORD))
                    ;
        } else {
            return this;
        }
    }



    public ActiveMqAddon url(String url) {return withUrl(url);}

    public ActiveMqAddon user(String user) {return withUser(user);}

    public ActiveMqAddon password(String password) {return withPassword(password);}

    @Override
    public Set<Class<?>> initializeAfter() {return ImmutableSet.of(MqAddon.class);}

}

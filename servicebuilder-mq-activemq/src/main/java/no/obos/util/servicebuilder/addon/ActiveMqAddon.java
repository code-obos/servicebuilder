package no.obos.util.servicebuilder.addon;

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
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.HandlerDescription;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MqHandlerImpl;
import no.obos.util.servicebuilder.mq.MqSenderImpl;
import no.obos.util.servicebuilder.mq.SenderDescription;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqConnectionProvider;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqListener;
import no.obos.util.servicebuilder.mq.activemq.ActiveMqSender;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Set;

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

        ActiveMqListener listener = ActiveMqListener.builder()
                .mqHandlerForwarder(mqAddon.mqHandlerForwarder)
                .activeMqConnectionProvider(connectionProvider)
                .handlerDescriptions(mqAddon.handlers)
                .build();

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
            binder.bind(this.listener).to(ActiveMqListener.class);

            mqAddon.senders.forEach(senderDescription -> {
                MqSenderImpl<?> mqSenderImpl = getMqSender(activeMqSender, senderDescription);
                binder.bind(mqSenderImpl).to(senderDescription.typeLiteral);
            });
        });

        // Feature is used to start the listeners immediately once dependencies are bound
        serviceConfig.addRegistations(registrator -> registrator
                .register(StartListenersFeature.class)
        );
    }

    private <T> MqSenderImpl<T> getMqSender(ActiveMqSender activeMqSender, SenderDescription<T> senderDescription) {
        return MqSenderImpl.<T>builder()
                .messageDescription(senderDescription.messageDescription)
                .mqTextSender(activeMqSender)
                .senderName(serviceDefinition.getName())
                .objectMapper(senderDescription.objectMapper)
                .build();
    }


    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        mqAddon.handlers.forEach(handlerDescription -> {
            String queueInput = handlerDescription.messageDescription.getQueueName();
            String queueError = handlerDescription.messageDescription.getErrorQueueName();
            ObosHealthCheckRegistry.registerActiveMqCheck("Input queue: " + queueInput + " on " + url, url, queueInput, handlerDescription.healthCheckEntriesMax, handlerDescription.healthCheckEntriesMax, user, password);
            ObosHealthCheckRegistry.registerActiveMqCheck("Error queue: " + queueError + " on " + url, url, queueError, user, password);
            ObosHealthCheckRegistry.registerCustomCheck("ActiveMqListener active", () ->
                    listener.isListenerActive()
                            ? ObosHealthCheckResult.ok()
                            : ObosHealthCheckResult.error("ActiveMqListener not active")
            );
        });
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

    private static class StartListenersFeature implements Feature {
        @Inject
        private ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            // Iterates through all configurations, which contains the names of the listeners and handlers
            ActiveMqListener listener = serviceLocator.getService(ActiveMqListener.class);
            MqAddon mqAddon = serviceLocator.getService(MqAddon.class);
            ImmutableSet<MqHandlerImpl<?>> handlers = mqAddon.handlers.stream()
                    .map(this::getHandlerImpl)
                    .collect(GuavaHelper.setCollector());
            listener.startListener(handlers);
            return true;
        }

        private <T> MqHandlerImpl<T> getHandlerImpl(HandlerDescription<T> handlerDescription) {
            MessageHandler<T> service = serviceLocator.getService(handlerDescription.messageHandlerClass);
            return MqHandlerImpl.<T>builder()
                    .handlerDescription(handlerDescription)
                    .messageHandler(service)
                    .build();
        }

    }

    public ActiveMqAddon url(String url) {return withUrl(url);}

    public ActiveMqAddon user(String user) {return withUser(user);}

    public ActiveMqAddon password(String password) {return withPassword(password);}

    @Override
    public Set<Class<?>> initializeAfter() {return ImmutableSet.of(MqAddon.class);}

}

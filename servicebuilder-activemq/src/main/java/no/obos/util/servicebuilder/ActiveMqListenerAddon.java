package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.mq.ActiveMqListener;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MessageQueueListener;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Initializes a MessageQueueListener and routes the messages to a specified handler class.
 * The name is used as a prefix in the AppConfig and to bind the MessageQueueListener.
 * <p>
 * For one listener, a name is optional:
 * <pre>
 * MessageQueueListenerAddon.defaults(ExampleHandler.class)
 * </pre>
 * or
 * <pre>
 * MessageQueueListenerAddon.defaults("exampleName", ExampleHandler.class)
 * </pre>
 * For multiple listeners, unique names are required:
 * <pre>
 * MessageQueueListenerAddon.defaults("firstExampleName", FirstExampleHandler.class)
 * MessageQueueListenerAddon.defaults("secondExampleName", SecondExampleHandler.class)
 * </pre>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActiveMqListenerAddon implements Addon {

    public static final String CONFIG_KEY_URL = "queue.url";
    public static final String CONFIG_KEY_USER = "queue.user";
    public static final String CONFIG_KEY_PASSWORD = "queue.password";
    public static final String CONFIG_KEY_QUEUE_INPUT = "queue.name.input";
    public static final String CONFIG_KEY_QUEUE_ERROR = "queue.name.error";
    public static final String CONFIG_KEY_PREFETCH_AMOUNT = "queue.prefetch";

    @Wither(AccessLevel.PRIVATE)
    public final MessageQueueListener mqListener;

    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final String url;
    @Wither(AccessLevel.PRIVATE)
    public final String user;
    @Wither(AccessLevel.PRIVATE)
    public final String password;
    @Wither(AccessLevel.PRIVATE)
    public final String queueInput;
    @Wither(AccessLevel.PRIVATE)
    public final String queueError;
    @Wither(AccessLevel.PRIVATE)
    public final int prefetchAmount;
    @Wither(AccessLevel.PRIVATE)
    public final Class<? extends MessageHandler> handler;

    private static final ActiveMqListenerAddon defaults = new ActiveMqListenerAddon(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            10,
            null
    );

    public static ActiveMqListenerAddon defaults(Class<? extends MessageHandler> messageHandler) {
        return defaults.handler(messageHandler);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        serviceConfig.addBinder((binder) -> {
            String name = StringUtils.trimToNull(this.name);
            binder.bind(this.mqListener).named(name).to(MessageQueueListener.class);
            binder.bind(handler).named(name).to(MessageHandler.class);
            binder.bind(this).named(name).to(ActiveMqListenerAddon.class);
        });

        // Feature is used to start the listeners immediately once dependencies are bound
        serviceConfig.addRegistations(registrator -> registrator
                .register(StartListenersFeature.class)
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerActiveMqCheck("Input queue: " + queueInput + " on " + url, url);
        ObosHealthCheckRegistry.registerActiveMqCheck("Error queue: " + queueError + " on " + url, url);
    }

    @Override
    public ActiveMqListenerAddon initialize(ServiceConfig serviceConfig) {
        return this.withMqListener(new ActiveMqListener(url, user, password, queueInput, queueError, prefetchAmount));
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";

        properties.failIfNotPresent(
                prefix + CONFIG_KEY_URL,
                prefix + CONFIG_KEY_USER,
                prefix + CONFIG_KEY_PASSWORD,
                prefix + CONFIG_KEY_QUEUE_INPUT,
                prefix + CONFIG_KEY_QUEUE_ERROR
        );

        ActiveMqListenerAddon addon = this
                .url(properties.get(prefix + CONFIG_KEY_URL))
                .user(properties.get(prefix + CONFIG_KEY_USER))
                .password(properties.get(prefix + CONFIG_KEY_PASSWORD))
                .queueInput(properties.get(prefix + CONFIG_KEY_QUEUE_INPUT))
                .queueError(properties.get(prefix + CONFIG_KEY_QUEUE_ERROR));

        if (isNotBlank(properties.getOrNull(prefix + CONFIG_KEY_PREFETCH_AMOUNT))) {
            addon = addon.prefetchAmount(Integer.parseInt(properties.get(prefix + CONFIG_KEY_PREFETCH_AMOUNT)));
        }
        return addon;
    }

    private static class StartListenersFeature implements Feature {
        @Inject
        private ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            // Iterates through all configurations, which contains the names of the listeners and handlers
            serviceLocator.getAllServices(ActiveMqListenerAddon.class).forEach(configuration -> {
                String name = StringUtils.trimToNull(configuration.name);
                MessageQueueListener listener = serviceLocator.getService(MessageQueueListener.class, name);
                MessageHandler handler = serviceLocator.getService(MessageHandler.class, name);
                listener.receiveMessages(handler);
            });
            return true;
        }
    }

    public ActiveMqListenerAddon mqListener(MessageQueueListener mqListener) {
        return withMqListener(mqListener);
    }

    public ActiveMqListenerAddon name(String name) {
        return withName(name);
    }

    public ActiveMqListenerAddon url(String url) {
        return withUrl(url);
    }

    public ActiveMqListenerAddon user(String user) {
        return withUser(user);
    }

    public ActiveMqListenerAddon password(String password) {
        return withPassword(password);
    }

    public ActiveMqListenerAddon queueInput(String queueInput) {
        return withQueueInput(queueInput);
    }

    public ActiveMqListenerAddon queueError(String queueError) {
        return withQueueError(queueError);
    }

    public ActiveMqListenerAddon prefetchAmount(int prefetchAmount) {
        return withPrefetchAmount(prefetchAmount);
    }

    public ActiveMqListenerAddon handler(Class<? extends MessageHandler> handler) {
        return withHandler(handler);
    }

}

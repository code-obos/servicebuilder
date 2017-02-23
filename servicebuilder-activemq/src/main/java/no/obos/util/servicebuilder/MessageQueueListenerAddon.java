package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.mq.MessageHandler;
import no.obos.util.servicebuilder.mq.MessageQueueListener;
import no.obos.util.servicebuilder.mq.MessageQueueListenerImpl;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

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
@SuppressWarnings("unused")
public class MessageQueueListenerAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_URL = "queue.url";
    public static final String CONFIG_KEY_USER = "queue.user";
    public static final String CONFIG_KEY_PASSWORD = "queue.password";
    public static final String CONFIG_KEY_QUEUE_INPUT = "queue.name.input";
    public static final String CONFIG_KEY_QUEUE_ERROR = "queue.name.error";
    public static final String CONFIG_KEY_ENTRIES_MAX = "queue.entries.max";
    public static final String CONFIG_KEY_ENTRIES_GRACE = "queue.entries.grace";

    public final Configuration configuration;
    public final MessageQueueListener mqListener;

    public MessageQueueListenerAddon(Configuration configuration) {
        this.configuration = configuration;
        this.mqListener = new MessageQueueListenerImpl(configuration.url, configuration.user, configuration.password, configuration.queueInput, configuration.queueError);
    }

    public static void configFromAppConfig(AppConfig appConfig, Configuration.ConfigurationBuilder configBuilder) {
        String name = configBuilder.build().name;
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";

        appConfig.failIfNotPresent(
                prefix + CONFIG_KEY_URL,
                prefix + CONFIG_KEY_USER,
                prefix + CONFIG_KEY_PASSWORD,
                prefix + CONFIG_KEY_QUEUE_INPUT,
                prefix + CONFIG_KEY_QUEUE_ERROR,
                prefix + CONFIG_KEY_ENTRIES_MAX,
                prefix + CONFIG_KEY_ENTRIES_GRACE
        );

        configBuilder
                .url(appConfig.get(prefix + CONFIG_KEY_URL))
                .user(appConfig.get(prefix + CONFIG_KEY_USER))
                .password(appConfig.get(prefix + CONFIG_KEY_PASSWORD))
                .queueInput(appConfig.get(prefix + CONFIG_KEY_QUEUE_INPUT))
                .queueError(appConfig.get(prefix + CONFIG_KEY_QUEUE_ERROR))
                .queueEntriesMax(Integer.parseInt(appConfig.get(prefix + CONFIG_KEY_ENTRIES_MAX)))
                .queueEntriesGrace(Integer.parseInt(appConfig.get(prefix + CONFIG_KEY_ENTRIES_GRACE)))
        ;
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder();
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder((binder) -> {
            String name = StringUtils.trimToNull(configuration.name);
            binder.bind(this.mqListener).named(name).to(MessageQueueListener.class);
            binder.bind(configuration.handler).named(name).to(MessageHandler.class);
            binder.bind(configuration).named(name).to(Configuration.class);
        });

        // Feature is used to start the listeners immediately once dependencies are bound
        jerseyConfig.addRegistations(registrator -> registrator
                .register(StartListenersFeature.class)
        );
    }

    private static class StartListenersFeature implements Feature {
        @Inject
        private ServiceLocator serviceLocator;

        @Override
        public boolean configure(FeatureContext context) {
            // Iterates through all configurations, which contains the names of the listeners and handlers
            serviceLocator.getAllServices(Configuration.class).forEach(configuration -> {
                String name = StringUtils.trimToNull(configuration.name);
                MessageQueueListener listener = serviceLocator.getService(MessageQueueListener.class, name);
                MessageHandler handler = serviceLocator.getService(MessageHandler.class, name);
                listener.receiveMessages(handler);
            });
            return true;
        }
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerActiveMqCheck("Input queue: " + configuration.queueInput + " on " + configuration.url, configuration.url, configuration.queueInput, configuration.queueEntriesMax, configuration.queueEntriesGrace, configuration.user, configuration.password);
        ObosHealthCheckRegistry.registerActiveMqCheck("Error queue: " + configuration.queueError + " on " + configuration.url, configuration.url, configuration.queueError, configuration.user, configuration.password);
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String name;
        public final String url;
        public final String user;
        public final String password;
        public final String queueInput;
        public final String queueError;
        public final int queueEntriesMax;
        public final int queueEntriesGrace;
        public final Class<? extends MessageHandler> handler;
    }

    public static AddonBuilder configure(String name, Class<? extends MessageHandler> handler, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration().name(name).handler(handler));
    }

    public static AddonBuilder defaults(String name, Class<? extends MessageHandler> handler) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration().name(name).handler(handler));
    }

    public static AddonBuilder configure(Class<? extends MessageHandler> handler, Configurator options) {
        return configure(null, handler, options);
    }

    public static AddonBuilder defaults(Class<? extends MessageHandler> handler) {
        return defaults(null, handler);
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<MessageQueueListenerAddon> {
        Configurator options;
        Configuration.ConfigurationBuilder configBuilder;

        @Override
        public void addAppConfig(AppConfig appConfig) {
            configFromAppConfig(appConfig, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public MessageQueueListenerAddon init() {
            configBuilder = options.apply(configBuilder);
            return new MessageQueueListenerAddon(configBuilder.build());
        }
    }


    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

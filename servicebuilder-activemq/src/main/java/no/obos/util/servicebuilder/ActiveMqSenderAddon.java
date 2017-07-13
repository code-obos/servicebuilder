package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.mq.ActiveMqSender;
import no.obos.util.servicebuilder.mq.MessageQueueSender;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ActiveMqSenderAddon extends ServiceAddonEmptyDefaults {
    public static final int MAX_QUEUE_ENTRIES = 1000;

    public static final String CONFIG_KEY_URL = "queue.url";
    public static final String CONFIG_KEY_USER = "queue.user";
    public static final String CONFIG_KEY_PASSWORD = "queue.password";
    public static final String CONFIG_KEY_QUEUE = "queue.name";
    public static final String CONFIG_KEY_ENTRIES_GRACE = "queue.entries.grace";
    public static final boolean DEFAULT_REGISTER_HEALTHCHECK = true;

    public final Configuration configuration;
    public final MessageQueueSender mqSender;

    public ActiveMqSenderAddon(Configuration configuration) {
        this.configuration = configuration;
        this.mqSender = new ActiveMqSender(configuration.url, configuration.user, configuration.password, configuration.queue);
    }

    public static void configFromAppConfig(AppConfig appConfig, Configuration.ConfigurationBuilder configBuilder) {
        String name = configBuilder.build().name;
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";

        appConfig.failIfNotPresent(
                prefix + CONFIG_KEY_URL,
                prefix + CONFIG_KEY_USER,
                prefix + CONFIG_KEY_PASSWORD,
                prefix + CONFIG_KEY_QUEUE,
                prefix + CONFIG_KEY_ENTRIES_GRACE
        );

        configBuilder
                .url(appConfig.get(prefix + CONFIG_KEY_URL))
                .user(appConfig.get(prefix + CONFIG_KEY_USER))
                .password(appConfig.get(prefix + CONFIG_KEY_PASSWORD))
                .queue(appConfig.get(prefix + CONFIG_KEY_QUEUE))
                .queueEntriesGrace(Integer.parseInt(appConfig.get(prefix + CONFIG_KEY_ENTRIES_GRACE)))
                .registerHealthcheck(DEFAULT_REGISTER_HEALTHCHECK)
        ;
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder();
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder((binder) -> {
            if (Strings.isNullOrEmpty(configuration.name)) {
                binder.bind(this.mqSender).to(MessageQueueSender.class);
            } else {
                binder.bind(this.mqSender).named(configuration.name).to(MessageQueueSender.class);
            }
        });
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        if (configuration.registerHealthcheck) {
            ObosHealthCheckRegistry.registerActiveMqCheck("Sender queue: " + configuration.queue + " on " + configuration.url,
                    configuration.url, configuration.queue, MAX_QUEUE_ENTRIES, configuration.queueEntriesGrace,
                    configuration.user, configuration.password);
        }
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String name;
        public final String url;
        public final String user;
        public final String password;
        public final String queue;
        public final int queueEntriesGrace;
        public final boolean registerHealthcheck;
    }

    public static AddonBuilder configure(String name, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration().name(name));
    }

    public static AddonBuilder defaults(String name) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration().name(name));
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ActiveMqSenderAddon> {
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
        public ActiveMqSenderAddon init() {
            configBuilder = options.apply(configBuilder);
            return new ActiveMqSenderAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfiguration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

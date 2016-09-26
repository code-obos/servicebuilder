package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.clientGenerator.ClientGenerator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Knytter opp en datakilde og binder BasicDatasource og QueryRunner til hk2.
 * Ved initialisering (defaults og config) kan det legges til et navn til datakilden
 * for å støtte flere datakilder. Parametre fre properties vil da leses fra
 * navnet (databasenavn).db.url osv.
 */
public class ServiceClientAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_URL = "service.url";

    public static final boolean DEFAULT_APPTOKEN = true;
    public static final boolean DEFAULT_USERTOKEN = true;
    public static final boolean DEFAULT_MONITOR_INTEGRATION = true;

    public final Configuration configuration;

    public ServiceClientAddon(Configuration configuration) {

        this.configuration = configuration;
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final ServiceDefinition serviceDefinition;
        public final String url;
        public final boolean apptoken;
        public final boolean usertoken;
        public final boolean monitorIntegration;
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration(ServiceDefinition serviceDefinition) {
        return Configuration.builder()
                .serviceDefinition(serviceDefinition)
                .monitorIntegration(DEFAULT_MONITOR_INTEGRATION)
                .apptoken(DEFAULT_APPTOKEN)
                .usertoken(DEFAULT_USERTOKEN);
    }

    public static void configFromProperties(PropertyProvider properties, Configuration.ConfigurationBuilder configBuilder) {
        String name = configBuilder.build().serviceDefinition.getName();
        String prefix = name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_URL);
        configBuilder
                .url(properties.get(prefix + CONFIG_KEY_URL));

    }


    private static <T> void generateAndBind(Class<T> clazz, AbstractBinder binder, Configuration configuration) {
        T client = ClientGenerator.createClient(clazz, configuration);
        binder.bind(client).to(clazz);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    configuration.serviceDefinition.getResources().forEach(resource ->
                            generateAndBind(resource, binder, configuration)
                    );
                    binder
                            .bind(configuration.serviceDefinition.getJsonConfig().get())
                            .named(configuration.serviceDefinition.getName())
                            .to(ObjectMapper.class);
                }
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        if (configuration.monitorIntegration) {
            ObosHealthCheckRegistry.registerPingCheck(configuration.serviceDefinition.getName() + ": " + configuration.url, configuration.url);
        }
    }

    public static AddonBuilder configure(ServiceDefinition serviceDefinition, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration(serviceDefinition));
    }

    public static AddonBuilder defaults(ServiceDefinition serviceDefinition) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration(serviceDefinition));
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ServiceClientAddon> {
        Configurator options;
        Configuration.ConfigurationBuilder configBuilder;

        @Override
        public void addProperties(PropertyProvider properties) {
            configFromProperties(properties, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public ServiceClientAddon init() {
            configBuilder = options.apply(configBuilder);
            return new ServiceClientAddon(configBuilder.build());
        }
    }


    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

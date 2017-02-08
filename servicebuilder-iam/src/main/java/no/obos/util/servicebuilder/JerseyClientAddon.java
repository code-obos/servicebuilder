package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.ClientConfig;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import java.net.URI;

/**
 * Genererer klienter for en service med jersey klient-api og binder dem til context.
 */
public class JerseyClientAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_URL = "service.url";

    public static final boolean DEFAULT_USERTOKEN = true;

    public final Configuration configuration;

    public JerseyClientAddon(Configuration configuration) {
        this.configuration = configuration;
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final ServiceDefinition serviceDefinition;
        public final URI uri;
        public final boolean usertoken;
        public final ClientConfig clientConfigBase;
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration(ServiceDefinition serviceDefinition) {
        return Configuration.builder()
                .serviceDefinition(serviceDefinition)
                .usertoken(DEFAULT_USERTOKEN);
    }

    public static void configFromProperties(PropertyProvider properties, Configuration.ConfigurationBuilder configBuilder) {
        String name = configBuilder.build().serviceDefinition.getName();
        String prefix = name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_URL);
        configBuilder
                .uri(URI.create(properties.get(prefix + CONFIG_KEY_URL)));

    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = configuration.serviceDefinition.getName();
                    binder.bind(configuration).to(Configuration.class).named(serviceName);
                    Client client = ClientGenerator.builder()
                            .clientConfigBase(configuration.clientConfigBase)
                            .jsonConfig(configuration.serviceDefinition.getJsonConfig())
                            .build().generate();
                    binder.bind(client).to(Client.class).named(serviceName);
                    configuration.serviceDefinition.getResources().forEach(clazz -> {
                                binder.bind(configuration).to(Configuration.class).named(clazz.getCanonicalName());
                                binder.bind(client).to(Client.class).named(clazz.getCanonicalName());
                                //noinspection unchecked
                                binder.bindFactory(StubFactory3.class).to(clazz).named(serviceName);
                            }

                    );
                }
        );
    }


    public static class StubFactory3 implements Factory<Object> {

        final HttpHeaders headers;
        final InstantiationService instantiationService;
        final ServiceLocator serviceLocator;

        @Inject
        public StubFactory3(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator) {
            this.headers = headers;
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
        }

        public Object provide() {
            Class<?> requiredType = getStubClass();
            Client client = serviceLocator.getService(Client.class, requiredType.getCanonicalName());

            Configuration configuration = serviceLocator.getService(Configuration.class, requiredType.getCanonicalName());
            String userToken = configuration.usertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            return StubGenerator.builder()
                    .client(client)
                    .uri(configuration.uri)
                    .userToken(userToken)
                    .build().generateClient(requiredType);
        }

        @Override
        public void dispose(Object instance) {

        }

        private Class<?> getStubClass() {
            InstantiationData instantiationData = instantiationService.getInstantiationData();
            Injectee parentInjectee = instantiationData.getParentInjectee();
            return (Class) parentInjectee.getRequiredType();
        }
    }



    @Override
    public void addToJettyServer(JettyServer jettyServer) {
    }

    public static AddonBuilder configure(ServiceDefinition serviceDefinition, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration(serviceDefinition));
    }

    public static AddonBuilder defaults(ServiceDefinition serviceDefinition) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration(serviceDefinition));
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<JerseyClientAddon> {
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
        public JerseyClientAddon init() {
            configBuilder = options.apply(configBuilder);
            return new JerseyClientAddon(configBuilder.build());
        }
    }


    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

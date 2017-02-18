package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StringProvider;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.util.Hk2Helper;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.ClientConfig;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import java.net.URI;

/**
 * Genererer klienter for en service med jersey klient-api og binder dem til context.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JerseyClientAddon implements Addon {

    public static final String CONFIG_KEY_URL = "service.url";

    public final ServiceDefinition serviceDefinition;
    public final URI uri;
    public final boolean forwardUsertoken;
    public final boolean apptoken;

    public final ClientConfig clientConfigBase;
    public final boolean monitorIntegration;
    public final boolean targetThrowsExceptions;

    public final Runtime runtime;

    public static JerseyClientAddon defaults(ServiceDefinition serviceDefinition) {
        return new JerseyClientAddon(serviceDefinition, null, false, true, null, true, true, null);
    }



    @Override
    public Addon withProperties(PropertyProvider properties) {
        String name = serviceDefinition.getName();
        String prefix = name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_URL);
        return this
                .uri(URI.create(properties.get(prefix + CONFIG_KEY_URL)));
    }

    @Override
    public Addon finalize(ServiceConfig serviceConfig) {
        Client client = ClientGenerator.defaults(serviceDefinition)
                .withClientConfigBase(clientConfigBase)
                .generate();
        return runtime(new Runtime(client));
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = serviceDefinition.getName();
                    binder.bind(this).to(JerseyClientAddon.class).named(serviceName);
                    binder.bind(runtime.client).to(Client.class).named(serviceName);
                    binder.bindFactory(WebTargetFactory.class).to(WebTarget.class).named(serviceName);
                    serviceDefinition.getResources().forEach(clazz -> {
                                binder.bind(this).to(JerseyClientAddon.class).named(clazz.getCanonicalName());
                                binder.bind(runtime.client).to(Client.class).named(clazz.getCanonicalName());
                                //noinspection unchecked
                                binder.bindFactory(StubFactory.class).to(clazz);
                            }

                    );
                }
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        if (monitorIntegration) {
            ObosHealthCheckRegistry.registerPingCheck(serviceDefinition.getName(), uri.toString());
        }
    }


    public static class StubFactory implements Factory<Object> {

        final HttpHeaders headers;
        final InstantiationService instantiationService;
        final ServiceLocator serviceLocator;
        final StringProvider apptokenProvider;

        @Inject
        public StubFactory(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator, @Named(Constants.APPTOKENID_HEADER) @Optional StringProvider apptokenProvider) {
            this.headers = headers;
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
            this.apptokenProvider = apptokenProvider;
        }

        public Object provide() {
            Class<?> requiredType = getStubClass();
            Client client = serviceLocator.getService(Client.class, requiredType.getCanonicalName());

            JerseyClientAddon configuration = serviceLocator.getService(JerseyClientAddon.class, requiredType.getCanonicalName());

            StubGenerator generator = StubGenerator.defaults(client, configuration.uri);
            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;

            String appTokenId = configuration.apptoken ? apptokenProvider.get() : null;
            if (appTokenId != null) {
                generator = generator.withHeader(Constants.APPTOKENID_HEADER, appTokenId);
            }
            return generator
                    .withUserToken(userToken)
                    .generateClient(requiredType);
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


    public static class WebTargetFactory implements Factory<WebTarget> {

        final HttpHeaders headers;
        final InstantiationService instantiationService;
        final ServiceLocator serviceLocator;
        final StringProvider apptokenProvider;

        @Inject
        public WebTargetFactory(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator, @Named(Constants.APPTOKENID_HEADER) @Optional StringProvider apptokenProvider) {
            this.headers = headers;
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
            this.apptokenProvider = apptokenProvider;
        }

        public WebTarget provide() {
            String serviceName = Hk2Helper.getInjecteeName(instantiationService);
            Client client = serviceLocator.getService(Client.class, serviceName);

            JerseyClientAddon configuration = serviceLocator.getService(JerseyClientAddon.class, serviceName);
            TargetGenerator generator = TargetGenerator.defaults(client, configuration.uri)
                    .withThrowExceptionForErrors(true);
            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            if (userToken != null) {
                generator = generator.withHeader(Constants.USERTOKENID_HEADER, userToken);
            }
            String appTokenId = configuration.apptoken ? apptokenProvider.get() : null;
            if (appTokenId != null) {
                generator = generator.withHeader(Constants.APPTOKENID_HEADER, appTokenId);
            }

            return generator.generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }


    @AllArgsConstructor
    public static class Runtime {
        public final Client client;
    }

    public JerseyClientAddon uri(URI uri) {return this.uri == uri ? this : new JerseyClientAddon(this.serviceDefinition, uri, this.forwardUsertoken, apptoken, this.clientConfigBase, true, targetThrowsExceptions, runtime);}

    public JerseyClientAddon forwardUsertoken(boolean forwardUsertoken) {return this.forwardUsertoken == forwardUsertoken ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, forwardUsertoken, apptoken, this.clientConfigBase, monitorIntegration, targetThrowsExceptions, runtime);}

    public JerseyClientAddon apptoken(boolean apptoken) {return new JerseyClientAddon(this.serviceDefinition, this.uri, forwardUsertoken, apptoken, this.clientConfigBase, monitorIntegration, targetThrowsExceptions, runtime);}

    public JerseyClientAddon clientConfigBase(ClientConfig clientConfigBase) {return this.clientConfigBase == clientConfigBase ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, this.forwardUsertoken, apptoken, clientConfigBase, monitorIntegration, targetThrowsExceptions, runtime);}

    public JerseyClientAddon monitorIntegration(boolean monitorIntegration) {return this.monitorIntegration == monitorIntegration ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, this.forwardUsertoken, apptoken, this.clientConfigBase, monitorIntegration, targetThrowsExceptions, runtime);}

    public JerseyClientAddon runtime(Runtime runtime) {return this.runtime == runtime ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, this.forwardUsertoken, apptoken, this.clientConfigBase, this.monitorIntegration, targetThrowsExceptions, runtime);}
}

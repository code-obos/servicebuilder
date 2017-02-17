package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.util.Hk2Helper;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.client.ClientConfig;

import javax.inject.Inject;
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
    public final ClientConfig clientConfigBase;

    public static JerseyClientAddon defaults(ServiceDefinition serviceDefinition) {
        return new JerseyClientAddon(serviceDefinition, null, false, null);
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
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = serviceDefinition.getName();
                    binder.bind(this).to(JerseyClientAddon.class).named(serviceName);
                    Client client = ClientGenerator.defaults
                            .clientConfigBase(clientConfigBase)
                            .jsonConfig(serviceDefinition.getJsonConfig())
                            .generate();
                    binder.bind(client).to(Client.class).named(serviceName);
                    binder.bindFactory(WebTargetFactory.class).to(WebTarget.class).named(serviceName);
                    serviceDefinition.getResources().forEach(clazz -> {
                                binder.bind(this).to(JerseyClientAddon.class).named(clazz.getCanonicalName());
                                binder.bind(client).to(Client.class).named(clazz.getCanonicalName());
                                //noinspection unchecked
                                binder.bindFactory(StubFactory.class).to(clazz);
                            }

                    );
                }
        );
    }



    public static class StubFactory implements Factory<Object> {

        final HttpHeaders headers;
        final InstantiationService instantiationService;
        final ServiceLocator serviceLocator;

        @Inject
        public StubFactory(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator) {
            this.headers = headers;
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
        }

        public Object provide() {
            Class<?> requiredType = getStubClass();
            Client client = serviceLocator.getService(Client.class, requiredType.getCanonicalName());

            JerseyClientAddon configuration = serviceLocator.getService(JerseyClientAddon.class, requiredType.getCanonicalName());
            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            return StubGenerator.defaults(client, configuration.uri)
                    .userToken(userToken)
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

        @Inject
        public WebTargetFactory(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator) {
            this.headers = headers;
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
        }

        public WebTarget provide() {
            String serviceName = Hk2Helper.getInjecteeName(instantiationService);
            Client client = serviceLocator.getService(Client.class, serviceName);

            JerseyClientAddon configuration = serviceLocator.getService(JerseyClientAddon.class, serviceName);
            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            return TargetGenerator.defaults(client, configuration.uri)
                    .userToken(userToken)
                    .generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }

    public JerseyClientAddon uri(URI uri) {return this.uri == uri ? this : new JerseyClientAddon(this.serviceDefinition, uri, this.forwardUsertoken, this.clientConfigBase);}

    public JerseyClientAddon forwardUsertoken(boolean usertoken) {return this.forwardUsertoken == usertoken ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, usertoken, this.clientConfigBase);}

    public JerseyClientAddon clientConfigBase(ClientConfig clientConfigBase) {return this.clientConfigBase == clientConfigBase ? this : new JerseyClientAddon(this.serviceDefinition, this.uri, this.forwardUsertoken, clientConfigBase);}
}

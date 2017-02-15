package no.obos.util.servicebuilder;

import lombok.Builder;
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
@Builder(toBuilder = true)
public class JerseyClientAddon implements Addon {

    public static final String CONFIG_KEY_URL = "service.url";

    public final ServiceDefinition serviceDefinition;
    public final URI uri;
    public final boolean usertoken;
    public final ClientConfig clientConfigBase;


    public static class JerseyClientAddonBuilder {
        boolean usertoken = false;
    }


    @Override
    public Addon withProperties(PropertyProvider properties) {
        String name = serviceDefinition.getName();
        String prefix = name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_URL);
        return toBuilder()
                .uri(URI.create(properties.get(prefix + CONFIG_KEY_URL)))
                .build();
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = serviceDefinition.getName();
                    binder.bind(this).to(JerseyClientAddon.class).named(serviceName);
                    Client client = ClientGenerator.builder()
                            .clientConfigBase(clientConfigBase)
                            .jsonConfig(serviceDefinition.getJsonConfig())
                            .build().generate();
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
            String userToken = configuration.usertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            return TargetGenerator.builder()
                    .client(client)
                    .uri(configuration.uri)
                    .userToken(userToken)
                    .build().generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }
}

package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StringProvider;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.interfaces.ApplicationTokenIdAddon;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
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
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Genererer klienter for en service med jersey klient-api og binder dem til context.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JerseyClientAddon implements Addon {

    public static final String CONFIG_KEY_URL = "service.url";

    public final ServiceDefinition serviceDefinition;
    @Wither(AccessLevel.PRIVATE)
    public final URI uri;
    @Wither(AccessLevel.PRIVATE)
    public final boolean forwardUsertoken;
    @Wither(AccessLevel.PRIVATE)
    public final boolean apptoken;
    @Wither(AccessLevel.PRIVATE)
    public final String apiPrefix;
    @Wither(AccessLevel.PRIVATE)
    public final ClientConfig clientConfigBase;
    @Wither(AccessLevel.PRIVATE)
    public final boolean monitorIntegration;
    @Wither(AccessLevel.PRIVATE)
    public final boolean targetThrowsExceptions;
    @Wither(AccessLevel.PRIVATE)
    public final boolean addApiVersionToPath;
    @Wither(AccessLevel.PRIVATE)
    public final Supplier<String> appTokenIdSupplier;
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;

    public static JerseyClientAddon defaults(ServiceDefinition serviceDefinition) {
        return new JerseyClientAddon(serviceDefinition, null, false, true, "api", null, true, true, true, null, null);
    }



    @Override
    public Addon withProperties(PropertyProvider properties) {
        String name = serviceDefinition.getName();
        String prefix = name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_URL);

        URI uri = URI.create(properties.get(prefix + CONFIG_KEY_URL));
        if(addApiVersionToPath) {
            uri = UriBuilder.fromUri(uri).path("s" + serviceDefinition.getVersion()).build();
        }
        return this
                .uri(uri);
    }

    @Override
    public Addon finalize(ServiceConfig serviceConfig) {
        Supplier<String> appTokenIdSupplier = null;
        if (this.apptoken && this.appTokenIdSupplier == null) {
            ApplicationTokenIdAddon appTokenIdSource = serviceConfig.addonInstance(ApplicationTokenIdAddon.class);
            if (appTokenIdSource == null) {
                throw new DependenceException(
                        this.getClass(),
                        ApplicationTokenIdAddon.class,
                        "Missing application id source provider for jerseyclientAddon. "
                                + "Either disable appliation token id usage (.apptoken(false)), "
                                + "provide an appTokenIdSupplier manually (.withApplicationTokenIdSource(<something>) "
                                + "or use an ApplicationTokenIdAddon (e.g. TokenServiceAddon)");
            }
            appTokenIdSupplier = appTokenIdSource.getApptokenIdSupplier();
        }
        Client client = ClientGenerator.defaults(serviceDefinition)
                .clientConfigBase(clientConfigBase)
                .generate();
        StubGenerator stubGenerator = StubGenerator.defaults(client, uri);

        if (appTokenIdSupplier != null) {
            stubGenerator = stubGenerator.appTokenSupplier(appTokenIdSupplier);
        }
        return withAppTokenIdSupplier(appTokenIdSupplier).withRuntime(new Runtime(client, stubGenerator));
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = serviceDefinition.getName();
                    binder.bind(this).to(JerseyClientAddon.class).named(serviceName);
                    binder.bind(runtime.client).to(Client.class).named(serviceName);
                    binder.bindFactory(WebTargetFactory.class).to(WebTarget.class).named(serviceName);
                    binder.bind(runtime.generator).to(StubGenerator.class).named(serviceName);
                    serviceDefinition.getResources().forEach(clazz -> {
                                binder.bind(this).to(JerseyClientAddon.class).named(clazz.getCanonicalName());
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
            ObosHealthCheckRegistry.registerPingCheck(serviceDefinition.getName() + ": " + uri.toString(), uri.toString());
        }
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
            JerseyClientAddon configuration = serviceLocator.getService(JerseyClientAddon.class, requiredType.getCanonicalName());

            StubGenerator generator = configuration.runtime.generator
                    .apiPath(configuration.apiPrefix);

            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            if(userToken != null) {
                generator = generator.header(Constants.USERTOKENID_HEADER, userToken);
            }


            return generator
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
                    .throwExceptionForErrors(true);

            String userToken = configuration.forwardUsertoken ? headers.getHeaderString(Constants.USERTOKENID_HEADER) : null;
            if(userToken != null) {
                generator = generator.plusHeader(Constants.USERTOKENID_HEADER, userToken);
            }

            Supplier<String> appTokenIdSupplier = configuration.apptoken ? configuration.appTokenIdSupplier : null;
            if (appTokenIdSupplier != null) {
                generator = generator.appTokenSupplier(appTokenIdSupplier);
            }

            return generator.generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }

    public Set<Class<?>> finalizeAfter() {return ImmutableSet.of(ApplicationTokenIdAddon.class);}


    @AllArgsConstructor
    public static class Runtime {
        public final Client client;
        StubGenerator generator;
    }


    public JerseyClientAddon uri(URI uri) {return withUri(uri);}

    public JerseyClientAddon forwardUsertoken(boolean forwardUsertoken) {return withForwardUsertoken(forwardUsertoken);}

    public JerseyClientAddon apptoken(boolean apptoken) {return withApptoken(apptoken);}

    public JerseyClientAddon apiPrefix(String apiPrefix) {return withApiPrefix(apiPrefix);}

    public JerseyClientAddon clientConfigBase(ClientConfig clientConfigBase) {return withClientConfigBase(clientConfigBase);}

    public JerseyClientAddon monitorIntegration(boolean monitorIntegration) {return withMonitorIntegration(monitorIntegration);}

    public JerseyClientAddon targetThrowsExceptions(boolean targetThrowsExceptions) {return withTargetThrowsExceptions(targetThrowsExceptions);}

    public JerseyClientAddon addApiVersionToPath(boolean addApiVersionToPath) {return withAddApiVersionToPath(addApiVersionToPath);}
}

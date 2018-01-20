package no.obos.util.servicebuilder.addon;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.util.ApiVersionUtil;
import no.obos.util.servicebuilder.util.ObosHealthCheckRegistry;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.client.ClientConfig;

import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

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
    public final String apiVersion;
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;

    public static JerseyClientAddon jerseyClientAddon(ServiceDefinition serviceDefinition) {
        String apiVersion = ApiVersionUtil.getApiVersion(serviceDefinition.getClass());
        return new JerseyClientAddon(serviceDefinition, null, false, "api", null, true, true, true, apiVersion, null);
    }


    @Override
    public Addon withProperties(PropertyProvider properties) {
        String name = serviceDefinition.getName();
        String prefix = name + ".";

        String url = properties.requireWithFallback(prefix + CONFIG_KEY_URL, uri==null?null:uri.toString());
        URI uri = URI.create(url);
        if (addApiVersionToPath) {
            uri = UriBuilder.fromUri(uri).path("s" + apiVersion).build();
        }
        return this
                .uri(uri);
    }

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        String clientAppName = serviceConfig.serviceDefinition.getName()
                + ":"
                + ApiVersionUtil.getApiVersion(serviceConfig.serviceDefinition.getClass());
        Client client = ClientGenerator.defaults(serviceDefinition)
                .clientConfigBase(clientConfigBase)
                .clientAppName(clientAppName)
                .generate();
        StubGenerator stubGenerator = StubGenerator.defaults(client, uri)
                .apiPath(apiPrefix);

        TargetGenerator targetGenerator = TargetGenerator.defaults(client, uri)
                .throwExceptionForErrors(true);

        return withRuntime(new Runtime(client, stubGenerator, targetGenerator));
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    String serviceName = serviceDefinition.getName();
                    if (!Strings.isNullOrEmpty(serviceName)) {
                        binder.bind(this).to(JerseyClientAddon.class).named(serviceName);
                        binder.bind(runtime.client).to(Client.class).named(serviceName);
                        binder.bindFactory(new WebTargetFactory(runtime.targetGenerator)).to(WebTarget.class).named(serviceName);
                        binder.bind(runtime.stubGenerator).to(StubGenerator.class).named(serviceName);
                    } else {
                        binder.bind(this).to(JerseyClientAddon.class);
                        binder.bind(runtime.client).to(Client.class);
                        binder.bindFactory(new WebTargetFactory(runtime.targetGenerator)).to(WebTarget.class);
                        binder.bind(runtime.stubGenerator).to(StubGenerator.class);
                    }

                    serviceDefinition.getResources().forEach(clazz -> {
                                //noinspection unchecked
                                binder.bindFactory(new StubFactory(clazz, runtime.stubGenerator)).to(clazz).in(Singleton.class);
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


    @AllArgsConstructor
    public static class StubFactory implements Factory<Object> {
        final Class<?> requiredType;
        final StubGenerator generator;

        public Object provide() {
            return generator
                    .generateClient(requiredType);
        }

        @Override
        public void dispose(Object instance) {

        }
    }


    @AllArgsConstructor
    public static class WebTargetFactory implements Factory<WebTarget> {
        TargetGenerator generator;

        public WebTarget provide() {
            return generator.generate();
        }

        @Override
        public void dispose(WebTarget instance) {
        }
    }


    @AllArgsConstructor
    public static class Runtime {
        public final Client client;
        public final StubGenerator stubGenerator;
        public final TargetGenerator targetGenerator;
    }


    public JerseyClientAddon uri(URI uri) {
        return withUri(uri);
    }

    public JerseyClientAddon apptoken(boolean apptoken) {
        return withApptoken(apptoken);
    }

    public JerseyClientAddon apiPrefix(String apiPrefix) {
        return withApiPrefix(apiPrefix);
    }

    public JerseyClientAddon clientConfigBase(ClientConfig clientConfigBase) {
        return withClientConfigBase(clientConfigBase);
    }

    public JerseyClientAddon monitorIntegration(boolean monitorIntegration) {
        return withMonitorIntegration(monitorIntegration);
    }

    public JerseyClientAddon targetThrowsExceptions(boolean targetThrowsExceptions) {
        return withTargetThrowsExceptions(targetThrowsExceptions);
    }

    public JerseyClientAddon addApiVersionToPath(boolean addApiVersionToPath) {
        return withAddApiVersionToPath(addApiVersionToPath);
    }
}

package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.config.PropertyMap;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.function.Function.identity;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestServiceRunner {
    @Wither(AccessLevel.PRIVATE)
    public final ServiceConfig serviceConfig;
    @Wither(AccessLevel.PRIVATE)
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    @Wither(AccessLevel.PRIVATE)
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    @Wither(AccessLevel.PRIVATE)
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;
    @Wither(AccessLevel.PRIVATE)
    public final PropertyMap propertyMap;

    public static TestServiceRunner defaults(ServiceConfig serviceConfig) {
        return new TestServiceRunner(serviceConfig, identity(), identity(), identity(), null, PropertyMap.empty);
    }


    @AllArgsConstructor
    public static class Runtime {
        public final JerseyConfig jerseyConfig;
        public final TestContainer testContainer;
        public final ClientConfig clientConfig;
        public final URI uri;
        public final Client client;
        @Wither(AccessLevel.PRIVATE)
        public final Function<StubGenerator, StubGenerator> stubConfigurator;
        @Wither(AccessLevel.PRIVATE)
        public final Function<TargetGenerator, TargetGenerator> targetConfigurator;

        public void stop() {
            testContainer.stop();
        }

        public <T> T call(BiFunction<ClientConfig, URI, T> testfun) {
            return testfun.apply(clientConfig, uri);
        }

        public <T> T call(Function<WebTarget, T> testfun) {
            TargetGenerator targetGenerator = targetConfigurator.apply(TargetGenerator.defaults(client, uri));
            return testfun.apply(targetGenerator.generate());
        }

        public <T, Y> T call(Class<Y> clazz, Function<Y, T> testfun) {
            StubGenerator stubGenerator = stubConfigurator.apply(StubGenerator.defaults(client, uri).apiPath(null));
            return testfun.apply(stubGenerator.generateClient(clazz));
        }

        public ResourceConfig getResourceConfig() {
            return jerseyConfig.getResourceConfig();
        }

        public Runtime stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {
            return withStubConfigurator(stubConfigurator);
        }

        public Runtime targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {
            return withTargetConfigurator(targetConfigurator);
        }
    }

    public TestServiceRunner start() {

        ServiceConfig serviceConfigwithProps = serviceConfig.addPropertiesAndApplyToBindings(propertyMap);
        ServiceConfig serviceConfigWithContext = ServiceConfigInitializer.finalize(serviceConfigwithProps);
        JerseyConfig jerseyConfig = new JerseyConfig(serviceConfigWithContext.serviceDefinition)
                .addRegistrators(serviceConfigWithContext.registrators)
                .addBinders(serviceConfigWithContext.binders);
        serviceConfigWithContext.addons.forEach(it -> it.addToJerseyConfig(jerseyConfig));

        DeploymentContext context = DeploymentContext.builder(jerseyConfig.getResourceConfig()).build();
        URI uri = UriBuilder.fromUri("http://localhost/").port(0).build();
        TestContainer testContainer = new InMemoryTestContainerFactory().create(uri, context);
        testContainer.start();
        ClientConfig clientConfig = testContainer.getClientConfig();
        ClientGenerator clientGenerator = clientConfigurator.apply(
                ClientGenerator.defaults(serviceConfigWithContext.serviceDefinition)
                        .clientConfigBase(clientConfig)
        );
        Client client = clientGenerator.generate();

        Runtime runtime = new Runtime(jerseyConfig, testContainer, clientConfig, uri, client, stubConfigurator, targetConfigurator);
        return withServiceConfig(serviceConfigWithContext).withRuntime(runtime);
    }

    public <T> T oneShot(BiFunction<ClientConfig, URI, T> testfun) {
        Runtime runner = start().runtime;
        try {
            return testfun.apply(runner.clientConfig, runner.uri);
        } finally {
            runner.stop();
        }
    }

    public <T, Y> T oneShot(Class<Y> clazz, Function<Y, T> testfun) {
        Runtime runner = start().runtime;
        try {
            return runner.call(clazz, testfun);
        } finally {
            runner.stop();
        }
    }

    public <T> T oneShot(Function<WebTarget, T> testfun) {
        Runtime runner = start().runtime;
        try {
            return runner.call(testfun);
        } finally {
            runner.stop();
        }
    }

    public TestServiceRunner property(String key, String value) {
        return propertyMap(this.propertyMap.put(key, value));
    }


    public TestServiceRunner clientConfigurator(Function<ClientGenerator, ClientGenerator> clientConfigurator) {
        return withClientConfigurator(clientConfigurator);
    }

    public TestServiceRunner stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {
        return withStubConfigurator(stubConfigurator);
    }

    public TestServiceRunner targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {
        return withTargetConfigurator(targetConfigurator);
    }

    public TestServiceRunner propertyMap(PropertyMap propertyMap) {
        return withPropertyMap(propertyMap);
    }
}

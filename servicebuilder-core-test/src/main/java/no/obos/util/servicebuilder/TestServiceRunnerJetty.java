package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.config.PropertyMap;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.function.Function.identity;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestServiceRunnerJetty implements TestServiceRunnerBase {

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final ServiceConfig serviceConfig;
    @Wither(AccessLevel.PRIVATE)
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    @Wither(AccessLevel.PRIVATE)
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    @Wither(AccessLevel.PRIVATE)
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;
    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;
    @Wither(AccessLevel.PRIVATE)
    public final PropertyMap propertyMap;

    public static TestServiceRunnerJetty defaults(ServiceConfig serviceConfig) {
        return new TestServiceRunnerJetty(serviceConfig, identity(), identity(), identity(), null, PropertyMap.empty);
    }


    @AllArgsConstructor
    public static class Runtime implements TestRuntime {
        public final ServiceRunner serviceRunner;
        public final URI uri;
        public final StubGenerator stubGenerator;
        public final ClientGenerator clientGenerator;
        public final TargetGenerator targetGenerator;

        public void stop() {
            serviceRunner.stop();
            serviceRunner.join();
        }

        public void join() {
            serviceRunner.join();
        }

        @Override
        public <T> T call(Function<WebTarget, T> testfun) {
            return testfun.apply(targetGenerator.generate());
        }

        @Override
        public <T, Y> T call(Class<Y> clazz, Function<Y, T> testfun) {
            return testfun.apply(stubGenerator.generateClient(clazz));
        }

        @Override
        public <Y> void callVoid(Class<Y> clazz, Consumer<Y> testfun) {
            testfun.accept(stubGenerator.generateClient(clazz));
        }

        @Override
        public void callVoid(Consumer<WebTarget> testfun) {
            testfun.accept(targetGenerator.generate());
        }

        @Override
        public ResourceConfig getResourceConfig() {
            return serviceRunner.jerseyConfig.resourceConfig;
        }
    }


    public TestServiceRunnerJetty.Runtime start() {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        ServiceConfig serviceConfigwithProps = serviceConfig.addPropertiesAndApplyToBindings(propertyMap);
        ServiceRunner serviceRunner = new ServiceRunner(serviceConfigwithProps, propertyMap);
        ServiceRunner runningServiceRunner = serviceRunner.start();

        URI uri = runningServiceRunner.jettyServer.server.getURI();
        uri = UriBuilder.fromUri(uri).host("localhost").build();

        ClientGenerator clientGenerator = clientConfigurator.apply(
                ClientGenerator.defaults(serviceConfigwithProps.serviceDefinition)
        );
        Client client = clientGenerator.generate();
        StubGenerator stubGenerator = stubConfigurator.apply(StubGenerator.defaults(client, UriBuilder.fromUri(uri).build()));

        TargetGenerator targetGenerator = targetConfigurator.apply(TargetGenerator.defaults(client, uri));

        return new Runtime(runningServiceRunner, uri, stubGenerator, clientGenerator, targetGenerator);
    }


    @Override
    public TestServiceRunnerJetty withStartedRuntime() {
        return this.withRuntime(start());
    }

    public TestChain chain() {
        return new TestChain(this);
    }

    public <T, Y> T oneShot(Class<Y> clazz, Function<Y, T> testfun) {
        Runtime runner = start();
        try {
            return testfun.apply(runner.stubGenerator.generateClient(clazz));
        } finally {
            runner.stop();
        }
    }

    public <T> T oneShot(Function<WebTarget, T> testfun) {
        Runtime runner = start();
        try {
            return testfun.apply(runner.targetGenerator.generate());
        } finally {
            runner.stop();
        }
    }


    public TestServiceRunnerJetty property(String key, String value) {
        return propertyMap(this.propertyMap.put(key, value));
    }


    public TestServiceRunnerJetty clientConfigurator(Function<ClientGenerator, ClientGenerator> clientConfigurator) {
        return withClientConfigurator(clientConfigurator);
    }

    public TestServiceRunnerJetty stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {
        return withStubConfigurator(stubConfigurator);
    }

    public TestServiceRunnerJetty targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {
        return withTargetConfigurator(targetConfigurator);
    }

    public TestServiceRunnerJetty propertyMap(PropertyMap propertyMap) {
        return withPropertyMap(propertyMap);
    }

    public TestServiceRunnerJetty propertyFile(String filePath) {
        Properties properties = loadProperties(filePath);
        return withPropertyMap(propertyMap.putAllProperties(properties));
    }

    private Properties loadProperties(String file) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(file)) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestServiceRunnerJetty serviceConfig(ServiceConfig serviceConfig) {
        return this.withServiceConfig(serviceConfig);
    }
}

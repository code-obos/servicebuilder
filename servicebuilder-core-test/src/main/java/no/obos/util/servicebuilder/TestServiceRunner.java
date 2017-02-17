package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
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
    public final ServiceConfig serviceConfig;
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;
    public final Runtime runtime;

    public static TestServiceRunner defaults(ServiceConfig serviceConfig) {
        return new TestServiceRunner(serviceConfig, identity(), identity(), identity(), null);
    }


    @AllArgsConstructor
    public static class Runtime {
        public final JerseyConfig jerseyConfig;
        public final TestContainer testContainer;
        public final ClientConfig clientConfig;
        public final URI uri;
        public final Client client;
        public final Function<StubGenerator, StubGenerator> stubConfigurator;
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
            StubGenerator stubGenerator = stubConfigurator.apply(StubGenerator.defaults(client, uri));
            return testfun.apply(stubGenerator.generateClient(clazz));
        }

        public ResourceConfig getResourceConfig() {
            return jerseyConfig.getResourceConfig();
        }

        public Runtime stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {return this.stubConfigurator == stubConfigurator ? this : new Runtime(this.jerseyConfig, this.testContainer, this.clientConfig, this.uri, this.client, stubConfigurator, this.targetConfigurator);}

        public Runtime targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {return this.targetConfigurator == targetConfigurator ? this : new Runtime(this.jerseyConfig, this.testContainer, this.clientConfig, this.uri, this.client, this.stubConfigurator, targetConfigurator);}
    }


    public TestServiceRunner start() {
        ServiceConfig serviceConfigWithContext = ServiceConfigInitializer.finalize(serviceConfig);
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
        return new TestServiceRunner(serviceConfigWithContext, this.clientConfigurator, this.stubConfigurator, targetConfigurator, runtime);
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


    public TestServiceRunner clientConfigurator(Function<ClientGenerator, ClientGenerator> clientConfigurator) {return this.clientConfigurator == clientConfigurator ? this : new TestServiceRunner(this.serviceConfig, clientConfigurator, this.stubConfigurator, this.targetConfigurator, null);}

    public TestServiceRunner stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {return this.stubConfigurator == stubConfigurator ? this : new TestServiceRunner(this.serviceConfig, this.clientConfigurator, stubConfigurator, this.targetConfigurator, null);}

    public TestServiceRunner targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {return this.targetConfigurator == targetConfigurator ? this : new TestServiceRunner(this.serviceConfig, this.clientConfigurator, this.stubConfigurator, targetConfigurator, null);}


}

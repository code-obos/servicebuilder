package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
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

@Builder(toBuilder = true)
public class TestServiceRunner {
    public final ServiceConfig serviceConfig;
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;


    @AllArgsConstructor
    public static class Runtime {
        public final JerseyConfig jerseyConfig;
        public final TestContainer testContainer;
        public final ClientConfig clientConfig;
        public final URI uri;
        public final StubGenerator stubGenerator;
        public final ClientGenerator clientGenerator;
        public final TargetGenerator targetGenerator;

        public void stop() {
            testContainer.stop();
        }

        public <T> T call(BiFunction<ClientConfig, URI, T> testfun) {
            return testfun.apply(clientConfig, uri);
        }

        public <T> T call(Function<WebTarget, T> testfun) {
            return testfun.apply(targetGenerator.generate());
        }

        public <T, Y> T call(Class<Y> clazz, Function<Y, T> testfun) {
            return testfun.apply(stubGenerator.generateClient(clazz));
        }

        public ResourceConfig getResourceConfig() {
            return jerseyConfig.getResourceConfig();
        }

    }


    public static class TestServiceRunnerBuilder {
        Function<ClientGenerator, ClientGenerator> clientConfigurator = (cfg -> cfg);
        Function<StubGenerator, StubGenerator> stubConfigurator = (cfg -> cfg);
        Function<TargetGenerator, TargetGenerator> targetConfigurator = (cfg -> cfg);
    }


    public TestServiceRunner.Runtime start() {
        ServiceConfig serviceConfigWithContext = ServiceConfigInitializer.addContext(serviceConfig);
        JerseyConfig jerseyConfig = new JerseyConfig(serviceConfigWithContext.serviceDefinition)
                .addRegistrators(serviceConfig.registrators)
                .addBinders(serviceConfig.binders);
        serviceConfig.addons.forEach(it -> it.addToJerseyConfig(jerseyConfig));

        DeploymentContext context = DeploymentContext.builder(jerseyConfig.getResourceConfig()).build();
        URI uri = UriBuilder.fromUri("http://localhost/").port(0).build();
        TestContainer testContainer = new InMemoryTestContainerFactory().create(uri, context);
        testContainer.start();
        ClientConfig clientConfig = testContainer.getClientConfig();
        ClientGenerator clientGenerator = clientConfigurator.apply(ClientGenerator.defaults
                .clientConfigBase(clientConfig)
                .jsonConfig(serviceConfig.serviceDefinition.getJsonConfig())
        );
        Client client = clientGenerator.generate();
        StubGenerator stubGenerator = stubConfigurator.apply(StubGenerator.defaults(client,uri));

        TargetGenerator targetGenerator = targetConfigurator.apply(TargetGenerator.defaults(client, uri));

        return new Runtime(jerseyConfig, testContainer, clientConfig, uri, stubGenerator, clientGenerator, targetGenerator);
    }

    public <T> T oneShot(BiFunction<ClientConfig, URI, T> testfun) {
        Runtime runner = start();
        try {
            return testfun.apply(runner.clientConfig, runner.uri);
        } finally {
            runner.stop();
        }
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



    //    public void start() {
    //
    //        try {
    //            return testfun.apply(clientConfig1, uri);
    //        } finally {
    //        }
    //    }
}

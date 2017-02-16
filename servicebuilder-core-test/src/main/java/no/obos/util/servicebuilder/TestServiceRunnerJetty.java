package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.config.PropertyMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.function.Function;

import static java.util.function.Function.identity;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestServiceRunnerJetty {
    public static final int DEFAULT_PORT = 38373;
    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_CONTEXTPATH = "/test/v" + DEFAULT_VERSION;

    public final ServiceConfig serviceConfig;
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;
    public final PropertyMap propertyMap;

    public static TestServiceRunnerJetty defaults(ServiceConfig serviceConfig) {
        return new TestServiceRunnerJetty(serviceConfig, identity(), identity(), identity(), PropertyMap.empty
                .put("server.port", String.valueOf(DEFAULT_PORT))
                .put("service.version", String.valueOf(DEFAULT_VERSION))
                .put("server.contextPath", DEFAULT_CONTEXTPATH));
    }


    @AllArgsConstructor
    public static class Runtime {
        public final ServiceRunner serviceRunner;
        public final URI uri;
        public final StubGenerator stubGenerator;
        public final ClientGenerator clientGenerator;
        public final TargetGenerator targetGenerator;

        public void stop() {
            serviceRunner.stop();
        }

        public void join() {
            serviceRunner.join();
        }

        public <T> T call(Function<WebTarget, T> testfun) {
            return testfun.apply(targetGenerator.generate());
        }

        public <T, Y> T call(Class<Y> clazz, Function<Y, T> testfun) {
            return testfun.apply(stubGenerator.generateClient(clazz));
        }
    }


    public static class TestServiceRunnerBuilder {
        Function<ClientGenerator, ClientGenerator> clientConfigurator = (cfg -> cfg);
        Function<StubGenerator, StubGenerator> stubConfigurator = (cfg -> cfg);
        Function<TargetGenerator, TargetGenerator> targetConfigurator = (cfg -> cfg);
    }


    public TestServiceRunnerJetty.Runtime start() {
        ServiceRunner serviceRunner = new ServiceRunner(serviceConfig, propertyMap);
        serviceRunner.start();

        URI uri = URI.create("http://localhost:" + DEFAULT_PORT + DEFAULT_CONTEXTPATH + "/api");

        ClientGenerator clientGenerator = clientConfigurator.apply(ClientGenerator.defaults
                .jsonConfig(serviceConfig.serviceDefinition.getJsonConfig())
        );
        Client client = clientGenerator.generate();
        StubGenerator stubGenerator = stubConfigurator.apply(StubGenerator.defaults(client, uri));

        TargetGenerator targetGenerator = targetConfigurator.apply(TargetGenerator.defaults(client, uri));

        return new Runtime(serviceRunner, uri, stubGenerator, clientGenerator, targetGenerator);
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


    public TestServiceRunnerJetty clientConfigurator(Function<ClientGenerator, ClientGenerator> clientConfigurator) {return this.clientConfigurator == clientConfigurator ? this : new TestServiceRunnerJetty(this.serviceConfig, clientConfigurator, this.stubConfigurator, this.targetConfigurator, propertyMap);}

    public TestServiceRunnerJetty stubConfigurator(Function<StubGenerator, StubGenerator> stubConfigurator) {return this.stubConfigurator == stubConfigurator ? this : new TestServiceRunnerJetty(this.serviceConfig, this.clientConfigurator, stubConfigurator, this.targetConfigurator, propertyMap);}

    public TestServiceRunnerJetty targetConfigurator(Function<TargetGenerator, TargetGenerator> targetConfigurator) {return this.targetConfigurator == targetConfigurator ? this : new TestServiceRunnerJetty(this.serviceConfig, this.clientConfigurator, this.stubConfigurator, targetConfigurator, propertyMap);}

    public TestServiceRunnerJetty property(String key, String value) {return new TestServiceRunnerJetty(this.serviceConfig, this.clientConfigurator, this.stubConfigurator, targetConfigurator, this.propertyMap.put(key, value));}

}

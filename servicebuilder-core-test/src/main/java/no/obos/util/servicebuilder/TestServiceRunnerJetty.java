package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.client.TargetGenerator;
import no.obos.util.servicebuilder.config.PropertyMap;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.function.Function;

import static java.util.function.Function.identity;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestServiceRunnerJetty {
    public static final int DEFAULT_PORT = 0;
    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_CONTEXTPATH = "/test/v" + DEFAULT_VERSION;

    public final ServiceConfig serviceConfig;
    @Wither
    public final Function<ClientGenerator, ClientGenerator> clientConfigurator;
    @Wither
    public final Function<StubGenerator, StubGenerator> stubConfigurator;
    @Wither
    public final Function<TargetGenerator, TargetGenerator> targetConfigurator;
    @Wither
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
            serviceRunner.join();
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


    public TestServiceRunnerJetty.Runtime start() {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        ServiceConfig serviceConfigwithProps = serviceConfig.withProperties(propertyMap);
        ServiceRunner serviceRunner = new ServiceRunner(serviceConfigwithProps, propertyMap);
        serviceRunner.start();

        URI uri = serviceRunner.jettyServer.server.getURI();

        ClientGenerator clientGenerator = clientConfigurator.apply(
                ClientGenerator.defaults(serviceConfigwithProps.serviceDefinition)
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


    public TestServiceRunnerJetty property(String key, String value) {return withPropertyMap(this.propertyMap.put(key, value));}

}

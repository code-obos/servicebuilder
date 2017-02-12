package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.function.BiFunction;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TestServiceRunner {
    public final ServiceConfig serviceConfig;
    public final JerseyConfig jerseyConfig;
    public final TestContainer testContainer;
    public final ClientConfig clientConfig;
    public final URI uri;


    public static TestServiceRunner start(ServiceConfig serviceConfig) {
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
        return new TestServiceRunner(serviceConfigWithContext, jerseyConfig, testContainer, clientConfig, uri);
    }

    public <T> T call(BiFunction<ClientConfig, URI, T> testfun) {
        return testfun.apply(clientConfig, uri);
    }

    public static <T> T oneShot(ServiceConfig serviceConfig, BiFunction<ClientConfig, URI, T> testfun) {
        TestServiceRunner serviceRunner = start(serviceConfig);
        try {
            return testfun.apply(serviceRunner.clientConfig, serviceRunner.uri);
        } finally {
            serviceRunner.stop();
        }
    }

    public ResourceConfig getResourceConfig() {
        return jerseyConfig.getResourceConfig();
    }



    public void stop() {
        testContainer.stop();
    }

    //    public void start() {
    //
    //        try {
    //            return testfun.apply(clientConfig1, uri);
    //        } finally {
    //        }
    //    }
}

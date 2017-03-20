package no.obos.util.servicebuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.function.BiFunction;

/**
 * Util class for running tests against embedded jersey server
 */
public class EmbeddedJerseyServer {

    public static <T> T run(Application application, BiFunction<ClientConfig, URI, T> testfun) {

        DeploymentContext context = DeploymentContext.builder(application).build();
        URI uri = UriBuilder.fromUri("http://localhost/").port(0).build();
        final TestContainer testContainer = new InMemoryTestContainerFactory().create(uri, context);

        testContainer.start();

        ClientConfig clientConfig1 = testContainer.getClientConfig();
        try {
            return testfun.apply(clientConfig1, uri);
        } finally {
            testContainer.stop();
        }
    }
}


package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;
import java.util.Objects;

@SuppressWarnings("squid:S00112")
public class JettyServer {
    public static final String CONFIG_KEY_SERVER_CONTEXT_PATH = "server.contextPath";
    public static final String CONFIG_KEY_SERVER_PORT = "server.port";
    private static final String DEFAULT_API_PATH_SPEC = "/api/*";
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    @Getter
    public final Server server;
    @Getter
    public final ServletContextHandler servletContext;
    @Getter
    @Setter
    public WebAppContext webAppContext = null;
    @Getter
    public final JerseyConfig resourceConfig;
    @Getter
    public final Configuration configuration;

    public JettyServer(Configuration configuration, JerseyConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
        this.configuration = configuration;
        server = new Server(InetSocketAddress.createUnresolved(configuration.bindAddress, configuration.bindPort));
        servletContext = new ServletContextHandler(server, configuration.contextPath);
    }

    public JettyServer start() {
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig.getResourceConfig()));
        servletContext.addServlet(servletHolder, configuration.apiPathSpec);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        Handler[] handlers = Lists.newArrayList(servletContext, webAppContext)
                .stream().filter(Objects::nonNull).toArray(Handler[]::new);
        contexts.setHandlers(handlers);

        server.setHandler(contexts);
        try {
            server.start();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public void join() {
        try {
            server.join();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void stop() {
        try {
            server.stop();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public static Configurator defaults() {
        return cfg -> cfg;
    }

    public void addAppContext(WebAppContext webAppContext) {
        this.webAppContext = webAppContext;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Configuration {
        public final String apiPathSpec;
        public final String bindAddress;
        public final String contextPath;
        public final int bindPort;


        public static class ConfigurationBuilder {
            String apiPathSpec = DEFAULT_API_PATH_SPEC;
            String bindAddress = DEFAULT_BIND_ADDRESS;
        }

        public static ConfigurationBuilder defaultBuilder() {
            return Configuration.builder();
        }

        public static ConfigurationBuilder fromProperties(PropertyProvider properties) {
            properties.failIfNotPresent(CONFIG_KEY_SERVER_PORT, CONFIG_KEY_SERVER_CONTEXT_PATH);
            return defaultBuilder()
                    .contextPath(properties.get(CONFIG_KEY_SERVER_CONTEXT_PATH))
                    .bindPort(Integer.parseInt(properties.get(CONFIG_KEY_SERVER_PORT)));
        }
    }


    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder cfg);
    }
}

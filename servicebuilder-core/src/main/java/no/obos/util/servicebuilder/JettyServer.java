package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.obos.util.config.AppConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import java.net.InetSocketAddress;

@SuppressWarnings("squid:S00112")
public final class JettyServer {
    public static final String CONFIG_KEY_SERVER_CONTEXT_PATH = "server.contextPath";
    public static final String CONFIG_KEY_SERVER_PORT = "server.port";
    private static final String DEFAULT_API_PATH_SPEC = "/api/*";
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";

    @Getter
    final Server server;
    @Getter
    final ServletContextHandler servletContext;
    @Getter
    final JerseyConfig resourceConfig;
    @Getter
    final Config config;

    JettyServer(Config config, JerseyConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
        this.config = config;
        server = new Server(InetSocketAddress.createUnresolved(config.bindAddress, config.bindPort));
        servletContext = new ServletContextHandler(server, config.contextPath);
    }

    public JettyServer start() throws Exception {
        ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig.getResourceConfig()));
        servletContext.addServlet(servletHolder, config.apiPathSpec);
        server.start();
        return this;
    }

    /**
     * Fletter tråder slik at main metoden ikke avsluttes med en gang
     *
     * @throws Exception hvis tråden allerede er opptatt
     */
    public void join() throws Exception {
        server.join();
    }

    public void stop() throws Exception {
        server.stop();

    }

    public static Configurator defaults() {
        return cfg -> cfg;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class Config {
        final String apiPathSpec;
        final String bindAddress;
        final String contextPath;
        final int bindPort;

        public static ConfigBuilder defaultBuilder() {
            return Config.builder()
                    .apiPathSpec(DEFAULT_API_PATH_SPEC)
                    .bindAddress(DEFAULT_BIND_ADDRESS);
        }

        public static ConfigBuilder fromAppConfig(AppConfig appConfig) {
            appConfig.failIfNotPresent(CONFIG_KEY_SERVER_PORT, CONFIG_KEY_SERVER_CONTEXT_PATH);
            return defaultBuilder()
                    .contextPath(appConfig.get(CONFIG_KEY_SERVER_CONTEXT_PATH))
                    .bindPort(Integer.parseInt(appConfig.get(CONFIG_KEY_SERVER_PORT)));
        }
    }

    public interface Configurator {
        Config.ConfigBuilder apply(Config.ConfigBuilder cfg);
    }
}

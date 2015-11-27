package no.obos.util.servicebuilder;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.obos.util.config.AppConfig;
import no.obos.util.config.AppConfigLoader;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ServiceBuilder {
    public static final String APPCONFIG_KEY = "SERVICE_CONFIG";
    public static final String CONFIG_KEY_PROXY = "proxy";
    public static final boolean DEFAULT_MONITOR_INTEGRATION = true;
    public static final boolean DEFAULT_READ_PROXY_FROM_CONFIG = false;
    public static final boolean DEFAULT_APPCONFIG_FROM_JVM_ARG = true;

    boolean julLoggingIntegration = DEFAULT_MONITOR_INTEGRATION;
    boolean readProxyFromConfig = DEFAULT_READ_PROXY_FROM_CONFIG;


    @Getter
    final AppConfig appConfig;

    @Getter
    JettyServer jettyServer;

    @Getter
    JerseyConfig jerseyConfig;

    @Getter
    public final Config config;

    ServiceBuilder(AppConfig appConfig, Config config) {
        this.appConfig = appConfig;
        this.config = config;
    }

    public static ServiceBuilder config(Configurator customConfiguration) {
        Config.ConfigBuilder builder = Config.defaultBuilder();
        Config config = customConfiguration.apply(builder).build();
        AppConfig appConfig = null;
        if (config.appConfigFromJvmArg) {
            appConfig = new AppConfigLoader().load(APPCONFIG_KEY);
        }
        return new ServiceBuilder(appConfig, config);
    }

    public static ServiceBuilder defaults() {
        Config config = Config.defaultBuilder().build();
        AppConfig appConfig = null;
        if (config.appConfigFromJvmArg) {
            appConfig = new AppConfigLoader().load(APPCONFIG_KEY);
        }
        return new ServiceBuilder(appConfig, config);
    }

    public ServiceBuilder configJersey(JerseyConfig.Configurator configurator) {
        jerseyConfig = configurator.apply(new JerseyConfig());
        if (appConfig != null) {
            jerseyConfig.addBinder(binder -> binder.bind(appConfig).to(AppConfig.class));
        }
        return this;
    }


    public ServiceBuilder configJettyServer(JettyServer.Configurator configurator) {
        Preconditions.checkNotNull(jerseyConfig);
        JettyServer.Config jettyConfig;
        if (appConfig != null) {
            jettyConfig = configurator.apply(JettyServer.Config.fromAppConfig(appConfig)).build();
        } else {
            jettyConfig = configurator.apply(JettyServer.Config.defaultBuilder()).build();
        }
        this.jettyServer = new JettyServer(jettyConfig, jerseyConfig);
        return this;
    }


    public ServiceBuilder with(ServiceAddonConfig<?> addonConfig) {
        addonConfig.addAppConfig(appConfig);
        addonConfig.addContext(this);
        ServiceAddon addon = addonConfig.init();

        addon.addToJerseyConfig(jerseyConfig);
        addon.addToJettyServer(jettyServer);
        return this;
    }

    public ServiceBuilder with(ServiceAddon addon) {
        addon.addToJerseyConfig(jerseyConfig);
        addon.addToJettyServer(jettyServer);
        return this;
    }

    public <Addon extends ServiceAddon> Addon newAddon(ServiceAddonConfig<Addon> addonConfig) {
        addonConfig.addAppConfig(appConfig);
        addonConfig.addContext(this);
        return addonConfig.init();
    }

    public ServiceBuilder start() {
        if (julLoggingIntegration) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }


        if (readProxyFromConfig) {
            appConfig.failIfNotPresent(CONFIG_KEY_PROXY);
            if ("true".equals(appConfig.get("proxy"))) {
                System.getProperties().put("https.proxyHost", "obosproxy.obos.no");
                System.getProperties().put("https.proxyPort", "8080");
                System.getProperties().put("https.proxyUser", "utvadm");
                System.getProperties().put("https.proxyPassword", "UtvAdm123");
                System.setProperty("http.nonProxyHosts", "*.obos.no|localhost|127.0.0.1");
            }
        }

        try {
            jettyServer.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public void join() {
        try {
            jettyServer.join();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void stop() {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Builder
    @AllArgsConstructor
    public static class Config {
        final boolean julLoggingIntegration;
        final boolean readProxyFromConfig;
        final boolean appConfigFromJvmArg;

        public static ConfigBuilder defaultBuilder() {
            return Config.builder()
                    .julLoggingIntegration(DEFAULT_MONITOR_INTEGRATION)
                    .readProxyFromConfig(DEFAULT_READ_PROXY_FROM_CONFIG)
                    .appConfigFromJvmArg(DEFAULT_APPCONFIG_FROM_JVM_ARG);
        }
    }


    public interface Configurator {
        Config.ConfigBuilder apply(Config.ConfigBuilder cfg);
    }

}

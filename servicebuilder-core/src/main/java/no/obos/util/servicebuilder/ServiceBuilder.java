package no.obos.util.servicebuilder;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.obos.util.config.AppConfig;
import no.obos.util.config.AppConfigLoader;
import no.obos.util.version.Version;
import no.obos.util.version.VersionUtil;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class ServiceBuilder {
    public static final String APPCONFIG_KEY = "SERVICE_CONFIG";
    public static final String CONFIG_KEY_PROXY = "proxy";
    public static final boolean DEFAULT_JUL_LOGGING_INTEGRATION = true;
    public static final boolean DEFAULT_READ_PROXY_FROM_CONFIG = false;
    public static final boolean DEFAULT_APPCONFIG_FROM_JVM_ARG = true;
    public static final String CONFIG_KEY_SERVICE_VERSION = "service.version";

    @Getter
    final AppConfig appConfig;

    @Getter
    JettyServer jettyServer;

    @Getter
    JerseyConfig jerseyConfig;

    @Getter
    public final Configuration configuration;

    ServiceBuilder(AppConfig appConfig, Configuration configuration) {
        this.appConfig = appConfig;
        this.configuration = configuration;
    }

    public static ServiceBuilder configure(Class classOnLocalClassPath, Configurator customConfiguration) {
        Configuration.ConfigurationBuilder builder = Configuration.defaultBuilder();
        Configuration configuration = customConfiguration.apply(builder).build();
        AppConfig appConfig = null;
        if (configuration.appConfigFromJvmArg) {
            appConfig = new AppConfigLoader().load(APPCONFIG_KEY);
            setServiceVersion(classOnLocalClassPath, appConfig);
        }
        return new ServiceBuilder(appConfig, configuration);
    }

    public static ServiceBuilder defaults(Class classOnLocalClassPath) {
        Configuration configuration = Configuration.defaultBuilder().build();
        AppConfig appConfig = null;
        if (configuration.appConfigFromJvmArg) {
            appConfig = new AppConfigLoader().load(APPCONFIG_KEY);
            setServiceVersion(classOnLocalClassPath, appConfig);
        }
        return new ServiceBuilder(appConfig, configuration);
    }

    public ServiceBuilder configJersey(JerseyConfig.Configurator configurator) {
        jerseyConfig = configurator.apply(new JerseyConfig(this));
        if (appConfig != null) {
            jerseyConfig.addBinder(binder -> binder.bind(appConfig).to(AppConfig.class));
        }
        return this;
    }


    public ServiceBuilder configJettyServer(JettyServer.Configurator configurator) {
        Preconditions.checkNotNull(jerseyConfig);
        JettyServer.Configuration jettyConfiguration;
        if (appConfig != null) {
            jettyConfiguration = configurator.apply(JettyServer.Configuration.fromAppConfig(appConfig)).build();
        } else {
            jettyConfiguration = configurator.apply(JettyServer.Configuration.defaultBuilder()).build();
        }
        this.jettyServer = new JettyServer(jettyConfiguration, jerseyConfig);
        return this;
    }


    public ServiceBuilder with(ServiceAddonConfig<?> addonConfig) {
        if(appConfig != null) {
            addonConfig.addAppConfig(appConfig);
        }
        addonConfig.addContext(this);
        ServiceAddon addon = addonConfig.init();

        addon.addToJerseyConfig(jerseyConfig);
        addon.addToJettyServer(jettyServer);
        return this;
    }

    public <T extends ServiceAddon> T with2(ServiceAddonConfig<T> addonConfig) {
        if(appConfig != null) {
            addonConfig.addAppConfig(appConfig);
        }
        addonConfig.addContext(this);
        T addon = addonConfig.init();

        addon.addToJerseyConfig(jerseyConfig);
        addon.addToJettyServer(jettyServer);
        return addon;
    }


    public ServiceBuilder with(ServiceAddon addon) {
        addon.addToJerseyConfig(jerseyConfig);
        addon.addToJettyServer(jettyServer);
        return this;
    }

    public <Addon extends ServiceAddon> Addon newAddon(ServiceAddonConfig<Addon> addonConfig) {
        if(appConfig != null) {
            addonConfig.addAppConfig(appConfig);
        }
        addonConfig.addContext(this);
        return addonConfig.init();
    }

    public ServiceBuilder start() {
        if (configuration.julLoggingIntegration) {
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
        }


        if (configuration.readProxyFromConfig) {
            appConfig.failIfNotPresent(CONFIG_KEY_PROXY);
            if ("true".equals(appConfig.get("proxy"))) {
                System.getProperties().put("http.proxyHost", "obosproxy.obos.no");
                System.getProperties().put("http.proxyPort", "8080");
                System.getProperties().put("http.proxyUser", "utvadm");
                System.getProperties().put("http.proxyPassword", "UtvAdm123");
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
    public static class Configuration {
        final boolean julLoggingIntegration;
        final boolean readProxyFromConfig;
        final boolean appConfigFromJvmArg;

        public static ConfigurationBuilder defaultBuilder() {
            return Configuration.builder()
                    .julLoggingIntegration(DEFAULT_JUL_LOGGING_INTEGRATION)
                    .readProxyFromConfig(DEFAULT_READ_PROXY_FROM_CONFIG)
                    .appConfigFromJvmArg(DEFAULT_APPCONFIG_FROM_JVM_ARG);
        }
    }


    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder cfg);
    }

    private static void setServiceVersion(Class classOnLocalClassPath, AppConfig appConfig) {
        final Version version = new VersionUtil(classOnLocalClassPath).getVersion();
        appConfig.put(CONFIG_KEY_SERVICE_VERSION, version == null ? "" : version.getMajor() + "." + version.getMinor());
    }
}

package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosMetricsServlet;
import no.obos.util.config.AppConfig;
import org.eclipse.jetty.servlet.ServletHolder;

@AllArgsConstructor
public class MetricsAddon extends ServiceAddonEmptyDefaults {

    private static final String DEFAULT_PATH_SPEC = "/metrics/*";

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder()
                .pathSpec(DEFAULT_PATH_SPEC);
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder metricsServletHolder = new ServletHolder(new ObosMetricsServlet(jettyServer.getClass()));
        jettyServer.getServletContext().addServlet(metricsServletHolder, configuration.pathSpec);
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String pathSpec;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<MetricsAddon> {
        Configurator options;
        Configuration.ConfigurationBuilder configBuilder;

        @Override
        public void addAppConfig(AppConfig appConfig) {
            configFromAppConfig(appConfig, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public MetricsAddon init() {
            configBuilder = options.apply(configBuilder);
            return new MetricsAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfigurationuration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfigurationuration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

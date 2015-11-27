package no.obos.util.servicebuilder;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.config.JerseyJaxrsConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import org.eclipse.jetty.servlet.ServletHolder;

@AllArgsConstructor
public class SwaggerAddon extends ServiceAddonEmptyDefaults {

    public static final String DEFAULT_PATH_SPEC = "/swagger";
    public static final String DEFAULT_API_VERSION = "1.0";
    public static final String CONFIG_KEY_API_BASEURL = "api.baseurl";

    public final Config config;

    public static Config.ConfigBuilder defaultConfig() {
        return Config.builder()
                .pathSpec(DEFAULT_PATH_SPEC)
                .apiVersion(DEFAULT_API_VERSION);
    }

    public static void configFromAppConfig(AppConfig appConfig, Config.ConfigBuilder configBuilder) {
        appConfig.failIfNotPresent(CONFIG_KEY_API_BASEURL);
        configBuilder.apiBasePath(appConfig.get(CONFIG_KEY_API_BASEURL));
    }

    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator
                .register(ApiListingResource.class)
                .register(SwaggerSerializers.class)
        );
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder apiDocServletHolder = new ServletHolder(new JerseyJaxrsConfig());
        apiDocServletHolder.setInitParameter("api.version", config.apiVersion);
        apiDocServletHolder.setInitParameter("swagger.api.basepath", config.apiBasePath);
        apiDocServletHolder.setInitOrder(2); //NOSONAR
        jettyServer.getServletContext().addServlet(apiDocServletHolder, config.pathSpec);
    }

    @Builder
    @AllArgsConstructor
    public static class Config {

        public final String apiBasePath;
        public final String pathSpec;
        public final String apiVersion;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<SwaggerAddon> {
        Configurator options;
        Config.ConfigBuilder configBuilder;

        @Override
        public void addAppConfig(AppConfig appConfig) {
            configFromAppConfig(appConfig, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public SwaggerAddon init() {
            configBuilder = options.apply(configBuilder);
            return new SwaggerAddon(configBuilder.build());
        }
    }

    public static AddonBuilder config(Configurator options) {
        return new AddonBuilder(options, defaultConfig());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfig());
    }

    public interface Configurator {
        Config.ConfigBuilder apply(Config.ConfigBuilder configBuilder);
    }
}

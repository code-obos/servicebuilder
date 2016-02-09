package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.cors.ResponseCorsFilter;

@AllArgsConstructor
public class CorsFilterAddon extends ServiceAddonEmptyDefaults {


    public static final ImmutableList<String> DEFAULT_ALLOW_ORIGIN = ImmutableList.<String>builder()
            .add("*")
            .build();

    public static final ImmutableList<String> DEFAULT_ALLOW_METHODS = ImmutableList.<String>builder()
            .add("GET")
            .add("POST")
            .add("OPTIONS")
            .add("DELETE")
            .add("PUT")
            .build();
    public static final ImmutableList<String> DEFAULT_ALLOW_HEADERS = ImmutableList.<String>builder()
            .add("X-PINGOTHER")
            .add("Origin")
            .add("X-Requested-With")
            .add("Content-Type")
            .add("Accept")
            .add("X-Codingpedia")
            .add("X-OBOS-USERTOKENID")
            .add("X-OBOS-APPTOKENID")
            .add("api_key")
            .add("Authorization")
            .build();

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder()
                .allowOrigin(DEFAULT_ALLOW_ORIGIN)
                .allowMethods(DEFAULT_ALLOW_METHODS)
                .allowHeaders(DEFAULT_ALLOW_HEADERS);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bind(configuration).to(Configuration.class));
        jerseyConfig.addRegistations(registrator -> registrator.register(ResponseCorsFilter.class));
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final ImmutableList<String> allowOrigin;
        public final ImmutableList<String> allowMethods;
        public final ImmutableList<String> allowHeaders;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<CorsFilterAddon> {
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
        public CorsFilterAddon init() {
            configBuilder = options.apply(configBuilder);
            return new CorsFilterAddon(configBuilder.build());
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

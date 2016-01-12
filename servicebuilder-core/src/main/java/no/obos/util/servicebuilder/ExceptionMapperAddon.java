package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.exception.FieldLevelExceptionMapper;
import no.obos.util.servicebuilder.exception.JsonProcessingExceptionMapper;
import no.obos.util.servicebuilder.exception.RuntimeExceptionMapper;
import no.obos.util.servicebuilder.exception.ValidationExceptionMapper;
import no.obos.util.servicebuilder.exception.WebApplicationExceptionMapper;

@AllArgsConstructor
public class ExceptionMapperAddon extends ServiceAddonEmptyDefaults {

    private static final boolean DEFAULT_MAP_FIELD_LEVEL_VALIDATION = true;
    private static final boolean DEFAULT_MAP_JSON_PROCESSING = true;
    private static final boolean DEFAULT_MAP_RUNTIME = true;
    private static final boolean DEFAULT_MAP_VALIDATION = true;
    private static final boolean DEFAULT_MAP_WEB_APPLICATION = true;

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder()
                .mapFieldLevelValidation(DEFAULT_MAP_FIELD_LEVEL_VALIDATION)
                .mapJsonProcessing(DEFAULT_MAP_JSON_PROCESSING)
                .mapRuntime(DEFAULT_MAP_RUNTIME)
                .mapValidation(DEFAULT_MAP_VALIDATION)
                .mapWebApplication(DEFAULT_MAP_WEB_APPLICATION);

    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> {

            if (configuration.mapFieldLevelValidation) {
                registrator.register(FieldLevelExceptionMapper.class);
            }
            if (configuration.mapJsonProcessing) {
                registrator.register(JsonProcessingExceptionMapper.class);
            }
            if (configuration.mapRuntime) {
                registrator.register(RuntimeExceptionMapper.class);
            }
            if (configuration.mapValidation) {
                registrator.register(ValidationExceptionMapper.class);
            }
            if (configuration.mapWebApplication) {
                registrator.register(WebApplicationExceptionMapper.class);
            }
        });
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        private final boolean mapFieldLevelValidation;
        private final boolean mapJsonProcessing;
        private final boolean mapRuntime;
        private final boolean mapValidation;
        private final boolean mapWebApplication;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ExceptionMapperAddon> {
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
        public ExceptionMapperAddon init() {
            configBuilder = options.apply(configBuilder);
            return new ExceptionMapperAddon(configBuilder.build());
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

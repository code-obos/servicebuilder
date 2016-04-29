package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.exception.FieldLevelExceptionMapper;
import no.obos.util.servicebuilder.exception.JsonProcessingExceptionMapper;
import no.obos.util.servicebuilder.exception.RuntimeExceptionMapper;
import no.obos.util.servicebuilder.exception.ValidationExceptionMapper;
import no.obos.util.servicebuilder.exception.WebApplicationExceptionMapper;

import javax.ws.rs.NotFoundException;

/**
 * Legger til et sett med standard exceptionmappere for Jersey som mapper til problem response.
 * Logger stacktrace for de fleste exceptions, med unntak av exceptions og underexceptions satt til false i config.stacktraceConfig.
 * Config.logAllStackTraces er ment for debug-bruk.
 */
@AllArgsConstructor
public class ExceptionMapperAddon extends ServiceAddonEmptyDefaults {

    public static final boolean DEFAULT_MAP_FIELD_LEVEL_VALIDATION = true;
    public static final boolean DEFAULT_MAP_JSON_PROCESSING = true;
    public static final boolean DEFAULT_MAP_RUNTIME = true;
    public static final boolean DEFAULT_MAP_VALIDATION = true;
    public static final boolean DEFAULT_MAP_WEB_APPLICATION = true;
    public static final boolean DEFAULT_LOG_ALL_STACK_TRACES = false;
    public static final ImmutableMap<Class<?>, Boolean> DEFAULT_STACKTRACE_CONFIG = ImmutableMap.<Class<?>, Boolean>builder()
            .put(Throwable.class, true)
            .put(NotFoundException.class, false)
            .build();

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder()
                .mapFieldLevelValidation(DEFAULT_MAP_FIELD_LEVEL_VALIDATION)
                .mapJsonProcessing(DEFAULT_MAP_JSON_PROCESSING)
                .mapRuntime(DEFAULT_MAP_RUNTIME)
                .mapValidation(DEFAULT_MAP_VALIDATION)
                .mapWebApplication(DEFAULT_MAP_WEB_APPLICATION)
                .stacktraceConfig(DEFAULT_STACKTRACE_CONFIG)
                .logAllStacktraces(DEFAULT_LOG_ALL_STACK_TRACES)
                ;

    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> {

            if (configuration.mapFieldLevelValidation) {
                registrator.register(new FieldLevelExceptionMapper(configuration.stacktraceConfig));
            }
            if (configuration.mapJsonProcessing) {
                registrator.register(new JsonProcessingExceptionMapper(configuration.stacktraceConfig));
            }
            if (configuration.mapRuntime) {
                registrator.register(new RuntimeExceptionMapper(configuration.stacktraceConfig));
            }
            if (configuration.mapValidation) {
                registrator.register(new ValidationExceptionMapper(configuration.stacktraceConfig));
            }
            if (configuration.mapWebApplication) {
                registrator.register(new WebApplicationExceptionMapper(configuration.stacktraceConfig));
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
        private final boolean logAllStacktraces;
        private final ImmutableMap<Class<?>, Boolean> stacktraceConfig;
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
            if(configBuilder.build().logAllStacktraces) {
                configBuilder = configBuilder.stacktraceConfig(ImmutableMap.<Class<?>, Boolean>builder()
                        .put(Throwable.class, true)
                        .build());
            }
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

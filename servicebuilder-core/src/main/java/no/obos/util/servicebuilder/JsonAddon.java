package no.obos.util.servicebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.cors.ResponseCorsFilter;
import org.glassfish.jersey.jackson.JacksonFeature;

import java.util.function.Supplier;

@AllArgsConstructor
public class JsonAddon extends ServiceAddonEmptyDefaults {


    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder();
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        ObjectMapper mapper = configuration.objectMapperSupplier.get();
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);
        jerseyConfig.addRegistations(registrator -> registrator
                .register(JacksonFeature.class)
                .register(provider)
        );
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final Supplier<ObjectMapper> objectMapperSupplier;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<JsonAddon> {
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
        public JsonAddon init() {
            configBuilder = options.apply(configBuilder);
            return new JsonAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Supplier<ObjectMapper> objectMapperSupplier, Configurator options) {
        return new AddonBuilder(options, defaultConfigurationuration().objectMapperSupplier(objectMapperSupplier));
    }

    public static AddonBuilder defaults(Supplier<ObjectMapper> objectMapperSupplier) {
        return new AddonBuilder(cfg -> cfg, defaultConfigurationuration().objectMapperSupplier(objectMapperSupplier));
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

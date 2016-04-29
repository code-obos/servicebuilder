package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.applicationtoken.ApplicationTokenFilter;

import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Legger inn applikasjonsfilter. Avhenger av at TokenServiceAddon er lagt til.
 */
@AllArgsConstructor
public class ApplicationTokenFilterAddon extends ServiceAddonEmptyDefaults {
    public static final String ACCEPTED_APP_IDS = "apptoken.accepted.app.ids";
    public static final Predicate<ContainerRequestContext> DEFAULT_FASTTRACK_FILTER = it -> false;
    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder()
                .fasttrackFilter(DEFAULT_FASTTRACK_FILTER);
    }

    public static void configFromAppConfig(AppConfig appConfig, ApplicationTokenFilterAddon.Configuration.ConfigurationBuilder configBuilder) {
        appConfig.failIfNotPresent(ACCEPTED_APP_IDS);
        ArrayList<String> acceptedIdStrings = Lists.newArrayList(appConfig.get(ACCEPTED_APP_IDS).split(","));
        List<Integer> acceptedIds = acceptedIdStrings.stream()
                .map(Integer::valueOf)
                .collect(toList());
        configBuilder.acceptedAppIds(ImmutableList.copyOf(acceptedIds));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        super.addToJerseyConfig(jerseyConfig);
        jerseyConfig.addRegistations(registrator -> registrator.register(ApplicationTokenFilter.class));
        jerseyConfig.addBinder(binder -> {
            binder.bindFactory(ApplicationTokenAccessValidatorFactory.class).to(ApplicationTokenAccessValidator.class);
            binder.bind(configuration).to(Configuration.class);
        });
    }

    private static class ApplicationTokenAccessValidatorFactory implements Factory<ApplicationTokenAccessValidator> {
        @Inject
        private TokenServiceClient tokenServiceClient;

        @Inject
        private Configuration configuration;

        @Override
        public ApplicationTokenAccessValidator provide() {
            ApplicationTokenAccessValidator applicationTokenAccessValidator = new ApplicationTokenAccessValidator();
            applicationTokenAccessValidator.setTokenServiceClient(tokenServiceClient);
            applicationTokenAccessValidator.setAcceptedAppIds(configuration.acceptedAppIds.stream().map(Object::toString).collect(Collectors.toList()));
            return applicationTokenAccessValidator;
        }

        @Override
        public void dispose(ApplicationTokenAccessValidator instance) {

        }
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final ImmutableList<Integer> acceptedAppIds;
        public final Predicate<ContainerRequestContext> fasttrackFilter;
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ApplicationTokenFilterAddon> {
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
        public ApplicationTokenFilterAddon init() {
            configBuilder = options.apply(configBuilder);
            return new ApplicationTokenFilterAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfiguration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

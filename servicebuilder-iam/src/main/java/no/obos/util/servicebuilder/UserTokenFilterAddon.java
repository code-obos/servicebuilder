package no.obos.util.servicebuilder;

import java.util.function.Predicate;

import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.usertoken.UibBrukerProvider;
import no.obos.util.servicebuilder.usertoken.UserTokenFilter;

/*
    Referanseimplementasjon: Aarsregnskapsplanlegging
 */

@AllArgsConstructor
public class UserTokenFilterAddon extends ServiceAddonEmptyDefaults {

    public static final String USERTOKENID_HEADER = "X-OBOS-USERTOKENID";

    public static final boolean DEFAULT_REQUIRE_USERTOKEN = true;
    
    public static final Predicate<ContainerRequestContext> DEFAULT_FASTTRACK_FILTER = it -> false;

    public final Configuration configuration;

    /**
     * Benytter interfacet UibBruker som tester om bruker kan bruke mellom rolle-annotasjoner (@RolesAllowed).
     * Standardimplementasjonen er BasicUibBruker, som tar en liste med rolleannotasjoner og regler for hvilke
     * uib-roller som oppfyller dissed. Sjekk implementasjon i Aarsregnskapsplanlegging.
     */
    public static Configuration.ConfigurationBuilder defaultConfiguration(UibBrukerProvider uibBrukerProvider) {
        return Configuration.builder()
                .requireUserToken(DEFAULT_REQUIRE_USERTOKEN)
                .uibBrukerProvider(uibBrukerProvider)
                .fasttrackFilter(DEFAULT_FASTTRACK_FILTER);
    }

    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    binder.bind(configuration).to(Configuration.class);
                }
        );
        jerseyConfig.addRegistations(registrator -> registrator
                .register(RolesAllowedDynamicFeature.class)
                .register(UserTokenFilter.class)
        );
    }


    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final boolean requireUserToken;
        public final UibBrukerProvider uibBrukerProvider;
        public final Predicate<ContainerRequestContext> fasttrackFilter;
    }



    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<UserTokenFilterAddon> {
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
        public UserTokenFilterAddon init() {
            configBuilder = options.apply(configBuilder);
            return new UserTokenFilterAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(UibBrukerProvider uibBrukerProvider, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration(uibBrukerProvider));
    }

    public static AddonBuilder defaults(UibBrukerProvider uibBrukerProvider) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration(uibBrukerProvider));
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

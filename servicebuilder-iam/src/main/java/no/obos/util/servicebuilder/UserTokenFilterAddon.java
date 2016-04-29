package no.obos.util.servicebuilder;

import java.util.function.Predicate;

import javax.ws.rs.container.ContainerRequestContext;

import no.obos.util.servicebuilder.usertoken.BasicUibBruker;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.usertoken.UibBrukerProvider;
import no.obos.util.servicebuilder.usertoken.UserTokenFilter;

/**
 * Konfigurerer UserTokenFilter.
 * Leser UserToken og knytter opp UibBruker med brukerinformasjon.
 * Denne kan hentes ut med
 *
 * @Context SecurityContext context
 * BasicUibBruker userPrincipal = (BasicUibBruker) context.getUserPrincipal();
 *
 * Kan også sette opp filtrering på javax-roller.
 * Sjekk implementasjon av dette i aarsregnskapsplanlegging
 */
@AllArgsConstructor
public class UserTokenFilterAddon extends ServiceAddonEmptyDefaults {

    public static final String USERTOKENID_HEADER = "X-OBOS-USERTOKENID";

    public static final boolean DEFAULT_REQUIRE_USERTOKEN = true;

    public static final UibBrukerProvider DEFAULT_UIB_BRUKER_PROVIDER = BasicUibBruker.provider();

    public static final Predicate<ContainerRequestContext> DEFAULT_FASTTRACK_FILTER = it -> false;

    public final Configuration configuration;

    /**
     * Benytter interfacet UibBruker som tester om bruker kan bruke mellom rolle-annotasjoner (@RolesAllowed).
     * Standardimplementasjonen er BasicUibBruker, som tar en liste med rolleannotasjoner og regler for hvilke
     * uib-roller som oppfyller dissed. Sjekk implementasjon i Aarsregnskapsplanlegging.
     */
    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder()
                .requireUserToken(DEFAULT_REQUIRE_USERTOKEN)
                .uibBrukerProvider(DEFAULT_UIB_BRUKER_PROVIDER)
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

    @Deprecated //Default uib bruker without roles
    public static AddonBuilder configure(UibBrukerProvider uibBrukerProvider, Configurator options) {
        Configuration.ConfigurationBuilder configurationBuilder = defaultConfiguration();
        configurationBuilder.uibBrukerProvider(uibBrukerProvider);
        return new AddonBuilder(options, configurationBuilder);
    }

    @Deprecated //Default uib bruker without roles
    public static AddonBuilder defaults(UibBrukerProvider uibBrukerProvider) {
        Configuration.ConfigurationBuilder configurationBuilder = defaultConfiguration();
        configurationBuilder.uibBrukerProvider(uibBrukerProvider);
        return new AddonBuilder(cfg -> cfg, configurationBuilder);
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

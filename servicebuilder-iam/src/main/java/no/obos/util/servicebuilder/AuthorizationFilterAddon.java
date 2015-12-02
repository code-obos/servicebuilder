package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import no.obos.util.config.AppConfig;
import no.obos.util.servicebuilder.authorization.AuthorizationFilter;
import no.obos.util.servicebuilder.authorization.UibBrukerProvider;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

@AllArgsConstructor
public class AuthorizationFilterAddon extends ServiceAddonEmptyDefaults {

    public static final String BIND_NAME_WHITELIST = "whitelist";
    public static final String BIND_NAME_DEFAULT_REQUIRE_USERTOKEN = "requireUserToken";

    public static final String USERTOKENID_HEADER = "X-OBOS-USERTOKENID";

    public static final ImmutableList<String> DEFAULT_WHITELIST = ImmutableList.of("/swagger.json");
    public static final boolean DEFAULT_REQUIRE_USERTOKEN = true;

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfiguration(UibBrukerProvider uibBrukerProvider) {
        return Configuration.builder()
                .whitelist(DEFAULT_WHITELIST)
                .requireUserToken(DEFAULT_REQUIRE_USERTOKEN)
                .uibBrukerProvider(uibBrukerProvider);
    }

    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        String[] whitelistArray = configuration.whitelist.toArray(new String[] {});

        jerseyConfig.addBinder(binder -> {
                    binder.bind(whitelistArray).to(String[].class).named(BIND_NAME_WHITELIST);
                    binder.bind(configuration.requireUserToken).to(Boolean.class).named(BIND_NAME_DEFAULT_REQUIRE_USERTOKEN);
                    binder.bind(configuration.uibBrukerProvider).to(UibBrukerProvider.class);
                }
        );
        jerseyConfig.addRegistations(registrator -> registrator
                .register(RolesAllowedDynamicFeature.class)
                .register(AuthorizationFilter.class)
        );
    }


    @Builder
    @AllArgsConstructor
    public static class Configuration {
        @Singular("whitelisted")
        public final ImmutableList<String> whitelist;
        public final boolean requireUserToken;
        public final UibBrukerProvider uibBrukerProvider;
    }



    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<AuthorizationFilterAddon> {
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
        public AuthorizationFilterAddon init() {
            configBuilder = options.apply(configBuilder);
            return new AuthorizationFilterAddon(configBuilder.build());
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

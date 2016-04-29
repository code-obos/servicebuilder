package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.iam.jersey.client.WebClientImpl;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceHttpClient;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;

/**
 * Konfigurerer klient til TokenService.
 */
public class TokenServiceAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_TOKENSERVICE_URL = "tokenservice.url";
    public static final String CONFIG_KEY_APP_ID = "tokenservice.app.id";
    public static final String CONFIG_KEY_APP_SECRET = "tokenservice.app.secret";
    public final Configuration configuration;
    public final TokenServiceClient tokenServiceClient;


    public TokenServiceAddon(Configuration configuration) {
        this.configuration = configuration;
        tokenServiceClient = new TokenServiceHttpClient(new WebClientImpl(configuration.url), configuration.appId, configuration.appSecret);
    }

    public static void configFromAppConfig(AppConfig appConfig, Configuration.ConfigurationBuilder configBuilder) {
        appConfig.failIfNotPresent(CONFIG_KEY_TOKENSERVICE_URL, CONFIG_KEY_APP_ID, CONFIG_KEY_APP_SECRET);
        configBuilder
                .url(appConfig.get(CONFIG_KEY_TOKENSERVICE_URL))
                .appId(appConfig.get(CONFIG_KEY_APP_ID))
                .appSecret(appConfig.get(CONFIG_KEY_APP_SECRET));
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder();
    }

    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bind(tokenServiceClient).to(TokenServiceClient.class));
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck("Tokenservice: " + configuration.url, configuration.url);
    }


    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String url;
        public final String appId;
        public final String appSecret;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<TokenServiceAddon> {
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
        public TokenServiceAddon init() {
            configBuilder = options.apply(configBuilder);
            return new TokenServiceAddon(configBuilder.build());
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

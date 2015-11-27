package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.iam.jersey.client.WebClientImpl;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceHttpClient;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;

public class TokenServiceAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_TOKENSERVICE_URL = "tokenservice.url";
    public static final String CONFIG_KEY_APP_ID = "app.id";
    public static final String CONFIG_KEY_APP_SECRET = "app.secret";
    public final Config config;
    public final TokenServiceClient tokenServiceClient;


    public TokenServiceAddon(Config config) {
        this.config = config;
        tokenServiceClient = new TokenServiceHttpClient(new WebClientImpl(config.url), config.appId, config.appSecret);
    }

    public static void configFromAppConfig(AppConfig appConfig, Config.ConfigBuilder configBuilder) {
        configBuilder
                .url(appConfig.get(CONFIG_KEY_TOKENSERVICE_URL))
                .appId(appConfig.get(CONFIG_KEY_APP_ID))
                .appSecret(appConfig.get(CONFIG_KEY_APP_SECRET));
    }

    public static Config.ConfigBuilder defaultConfig() {
        return Config.builder();
    }

    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bind(tokenServiceClient).to(TokenServiceClient.class));
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck("Tokenservice: " + config.url, config.url);
    }


    @Builder
    @AllArgsConstructor
    public static class Config {
        public final String url;
        public final String appId;
        public final String appSecret;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<TokenServiceAddon> {
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
        public TokenServiceAddon init() {
            configBuilder = options.apply(configBuilder);
            return new TokenServiceAddon(configBuilder.build());
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

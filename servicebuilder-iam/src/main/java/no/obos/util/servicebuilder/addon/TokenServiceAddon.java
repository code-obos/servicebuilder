package no.obos.util.servicebuilder.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.iam.jersey.client.WebClientImpl;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceHttpClient;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.interfaces.ApplicationTokenIdAddon;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;

import javax.inject.Inject;
import java.util.function.Supplier;

/**
 * Konfigurerer klient til TokenService.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenServiceAddon implements ApplicationTokenIdAddon, Addon {

    public static final String CONFIG_KEY_TOKENSERVICE_URL = "tokenservice.url";
    public static final String CONFIG_KEY_APP_ID = "tokenservice.app.id";
    public static final String CONFIG_KEY_APP_SECRET = "tokenservice.app.secret";

    @Wither(AccessLevel.PRIVATE)
    public final String url;
    @Wither(AccessLevel.PRIVATE)
    public final String appId;
    @Wither(AccessLevel.PRIVATE)
    public final String appSecret;
    @Wither(AccessLevel.PRIVATE)
    public final TokenServiceClient tokenServiceClient;
    @Wither(AccessLevel.PRIVATE)
    public final Runtime runtime;

    public static TokenServiceAddon defaults = new TokenServiceAddon(null, null, null, null, null);

    @Inject
    public Addon initialize(ServiceConfig serviceConfig) {
        TokenServiceClient tokenServiceClient =
                this.tokenServiceClient == null
                        ? new TokenServiceHttpClient(new WebClientImpl(url), appId, appSecret)
                        : this.tokenServiceClient;
        return this
                .withRuntime(new Runtime(
                        tokenServiceClient
                ));
    }

    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_TOKENSERVICE_URL, CONFIG_KEY_APP_ID, CONFIG_KEY_APP_SECRET);
        return this
                .url(properties.get(CONFIG_KEY_TOKENSERVICE_URL))
                .appId(properties.get(CONFIG_KEY_APP_ID))
                .appSecret(properties.get(CONFIG_KEY_APP_SECRET));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bind(runtime.tokenServiceClient).to(TokenServiceClient.class));
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck("Tokenservice: " + url, url);
    }

    @Override
    public Supplier<String> getApptokenIdSupplier() {
        return () -> runtime.tokenServiceClient.getApplicationToken().getApplicationTokenId();
    }

    public TokenServiceAddon url(String url) {return withUrl(url);}

    public TokenServiceAddon appId(String appId) {return withAppId(appId);}

    public TokenServiceAddon appSecret(String appSecret) {return withAppSecret(appSecret);}

    public TokenServiceAddon tokenServiceClient(TokenServiceClient tokenServiceClient) {return withTokenServiceClient(tokenServiceClient);}

    @AllArgsConstructor
    public static class Runtime {
        public final TokenServiceClient tokenServiceClient;
    }
}

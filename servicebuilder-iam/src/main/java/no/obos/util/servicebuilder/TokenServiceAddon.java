package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.iam.jersey.client.WebClientImpl;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceHttpClient;
import no.obos.metrics.ObosHealthCheckRegistry;

import javax.inject.Inject;
import java.util.Objects;

/**
 * Konfigurerer klient til TokenService.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenServiceAddon implements Addon {

    public static final String CONFIG_KEY_TOKENSERVICE_URL = "tokenservice.url";
    public static final String CONFIG_KEY_APP_ID = "tokenservice.app.id";
    public static final String CONFIG_KEY_APP_SECRET = "tokenservice.app.secret";

    @Wither
    public final String url;
    @Wither
    public final String appId;
    @Wither
    public final String appSecret;
    @Wither
    public final TokenServiceClient tokenServiceClient;

    public static TokenServiceAddon defaults = new TokenServiceAddon(null, null, null, null);

    @Inject
    public Addon finalize(ServiceConfig serviceConfig) {
        return this
                .withTokenServiceClient(
                        new TokenServiceHttpClient(new WebClientImpl(url), appId, appSecret)
                );
    }

    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_TOKENSERVICE_URL, CONFIG_KEY_APP_ID, CONFIG_KEY_APP_SECRET);
        return this
                .withUrl(properties.get(CONFIG_KEY_TOKENSERVICE_URL))
                .withAppId(properties.get(CONFIG_KEY_APP_ID))
                .withAppSecret(properties.get(CONFIG_KEY_APP_SECRET));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> binder.bind(tokenServiceClient).to(TokenServiceClient.class));
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck("Tokenservice: " + url, url);
    }
}

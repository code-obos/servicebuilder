package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.iam.jersey.client.WebClientImpl;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceHttpClient;
import no.obos.metrics.ObosHealthCheckRegistry;

import javax.inject.Inject;

/**
 * Konfigurerer klient til TokenService.
 */
@Builder(toBuilder = true)
public class TokenServiceAddon implements Addon {

    public static final String CONFIG_KEY_TOKENSERVICE_URL = "tokenservice.url";
    public static final String CONFIG_KEY_APP_ID = "tokenservice.app.id";
    public static final String CONFIG_KEY_APP_SECRET = "tokenservice.app.secret";
    public final TokenServiceClient tokenServiceClient;

    public final String url;
    public final String appId;
    public final String appSecret;

    @Inject
    public Addon withDependencies(ServiceConfig serviceConfig) {
        return this.toBuilder()
                .tokenServiceClient(
                        new TokenServiceHttpClient(new WebClientImpl(url), appId, appSecret)
                ).build();
    }

    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_TOKENSERVICE_URL, CONFIG_KEY_APP_ID, CONFIG_KEY_APP_SECRET);
        return toBuilder()
                .url(properties.get(CONFIG_KEY_TOKENSERVICE_URL))
                .appId(properties.get(CONFIG_KEY_APP_ID))
                .appSecret(properties.get(CONFIG_KEY_APP_SECRET))
                .build();
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

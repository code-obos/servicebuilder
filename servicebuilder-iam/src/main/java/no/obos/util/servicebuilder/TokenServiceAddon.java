package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

    public final String url;
    public final String appId;
    public final String appSecret;

    public final TokenServiceClient tokenServiceClient;

    public static TokenServiceAddon defaults = new TokenServiceAddon(null, null, null, null);

    @Inject
    public Addon withDependencies(ServiceConfig serviceConfig) {
        return this
                .tokenServiceClient(
                        new TokenServiceHttpClient(new WebClientImpl(url), appId, appSecret)
                );
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
        jerseyConfig.addBinder(binder -> binder.bind(tokenServiceClient).to(TokenServiceClient.class));
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerPingCheck("Tokenservice: " + url, url);
    }


    public TokenServiceAddon url(String url) {return Objects.equals(this.url, url) ? this : new TokenServiceAddon(url, this.appId, this.appSecret, this.tokenServiceClient);}

    public TokenServiceAddon appId(String appId) {return Objects.equals(this.appId, appId) ? this : new TokenServiceAddon(this.url, appId, this.appSecret, this.tokenServiceClient);}

    public TokenServiceAddon appSecret(String appSecret) {return Objects.equals(this.appSecret, appSecret) ? this : new TokenServiceAddon(this.url, this.appId, appSecret, this.tokenServiceClient);}

    public TokenServiceAddon tokenServiceClient(TokenServiceClient tokenServiceClient) {return this.tokenServiceClient == tokenServiceClient ? this : new TokenServiceAddon(this.url, this.appId, this.appSecret, tokenServiceClient);}
}

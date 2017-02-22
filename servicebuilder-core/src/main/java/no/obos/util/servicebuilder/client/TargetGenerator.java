package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TargetGenerator {
    final Client client;
    final URI uri;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableMap<String, String> headers;
    @Wither
    final boolean throwExceptionForErrors;
    @Wither
    final boolean logging;

    public static TargetGenerator defaults(Client client, URI uri) {
        return new TargetGenerator(client, uri, ImmutableMap.of(), false, true);
    }

    public WebTarget generate() {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        WebTarget target = clientToUse.target(uri);

        if (! headers.isEmpty()) {
            target.register(new AbstractBinder() {
                @Override
                protected void configure() {
                    this.bind(headers).to(new TypeLiteral<Map<String, String>>() {}).named(WebTargetRequestHeaderFilter.MAP_NAME);
                }
            });

            target.register(WebTargetRequestHeaderFilter.class);
        }

        if (throwExceptionForErrors) {
            target.register(ClientErrorResponseFilter.class);
        }
        target.register(RequestIdClientFilter.class);
        if(logging) {
            target.register(ClientLogFilter.class);
        }

        return target;
    }

    public TargetGenerator plusHeader(String key, String value) {return withHeaders(GuavaHelper.plus(headers, key, value));}
}

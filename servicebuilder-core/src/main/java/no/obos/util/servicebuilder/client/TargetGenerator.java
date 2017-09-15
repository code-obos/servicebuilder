package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.util.GuavaHelper;

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
    @Wither(AccessLevel.PRIVATE)
    final boolean throwExceptionForErrors;
    @Wither(AccessLevel.PRIVATE)
    final boolean logging;

    public static TargetGenerator defaults(Client client, URI uri) {
        return new TargetGenerator(client, uri, ImmutableMap.of(), false, true);
    }

    public WebTarget generate() {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        WebTarget target = clientToUse.target(uri);

        final Map<String, String> headersToUse = Maps.newHashMap(headers);


        if (! headersToUse.isEmpty()) {
            target.register(new WebTargetRequestHeaderFilter(ImmutableMap.copyOf(headersToUse)));
        }

        if (throwExceptionForErrors) {
            target.register(ClientErrorResponseFilter.class);
        }
        target.register(RequestIdClientFilter.class);
        target.register(ClientNameFilter.class);
        if (logging) {
            target.register(ClientLogFilter.class);
        }

        return target;
    }

    public TargetGenerator header(String key, String value) {
        return withHeaders(GuavaHelper.plus(headers, key, value));
    }

    public TargetGenerator throwExceptionForErrors(boolean throwExceptionForErrors) {
        return withThrowExceptionForErrors(throwExceptionForErrors);
    }

    public TargetGenerator logging(boolean logging) {
        return withLogging(logging);
    }
}

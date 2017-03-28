package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.util.GuavaHelper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Map;
import java.util.function.Supplier;

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
    @Wither(AccessLevel.PRIVATE)
    final Supplier<String> appTokenSupplier;

    public static TargetGenerator defaults(Client client, URI uri) {
        return new TargetGenerator(client, uri, ImmutableMap.of(), false, true, null);
    }

    public WebTarget generate() {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        WebTarget target = clientToUse.target(uri);

        final Map<String, String> headersToUse = Maps.newHashMap(headers);

        if (appTokenSupplier != null && ! headers.containsKey(Constants.APPTOKENID_HEADER)) {
            headersToUse.put(Constants.APPTOKENID_HEADER, appTokenSupplier.get());
        }


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

    public TargetGenerator header(String key, String value) {return withHeaders(GuavaHelper.plus(headers, key, value));}

    public TargetGenerator throwExceptionForErrors(boolean throwExceptionForErrors) {return withThrowExceptionForErrors(throwExceptionForErrors);}

    public TargetGenerator logging(boolean logging) {return withLogging(logging);}

    public TargetGenerator appTokenSupplier(Supplier<String> appTokenSupplier) {return withAppTokenSupplier(appTokenSupplier);}
}

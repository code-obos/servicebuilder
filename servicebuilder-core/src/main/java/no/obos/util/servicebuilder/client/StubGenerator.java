package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StubGenerator {
    //    final String appToken;
    final Client client;
    final URI uri;
    @Wither(AccessLevel.PRIVATE)
    final boolean logging;
    @Wither(AccessLevel.PRIVATE)
    final boolean throwExceptionForErrors;
    @Wither(AccessLevel.PRIVATE)
    final String apiPath;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableList<Cookie> cookies;
    @Wither(AccessLevel.PRIVATE)
    final ImmutableMap<String, String> headers;


    public static StubGenerator defaults(Client client, URI uri) {
        return new StubGenerator(client, uri, true, true, "api", ImmutableList.of(), ImmutableMap.of());
    }

    public <T> T generateClient(Class<T> resource) {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        MultivaluedMap<String, Object> headerArg = new MultivaluedHashMap<>(headers);

        WebTarget webTarget = clientToUse.target(uri);
        if (apiPath != null) {
            webTarget = webTarget.path(apiPath);
        }
        if(throwExceptionForErrors) {
            webTarget.register(ClientErrorResponseFilter.class);
        }
        webTarget.register(RequestIdClientFilter.class);
        webTarget.register(ClientNameFilter.class);
        if (logging) {
            webTarget.register(ClientLogFilter.class);
        }

        return WebResourceFactory.newResource(resource, webTarget, false, headerArg, cookies, new Form());
    }

    public StubGenerator header(String key, String value) {
        return withHeaders(GuavaHelper.plus(headers, key, value));
    }


    public StubGenerator throwExceptionForErrors(boolean throwExceptionForErrors) {
        return withThrowExceptionForErrors(throwExceptionForErrors);
    }

    public StubGenerator cookie(Cookie cookie) {
        return withCookies(GuavaHelper.plus(cookies, cookie));
    }

    public StubGenerator logging(boolean logging) {
        return withLogging(logging);
    }

    public StubGenerator apiPath(String apiPath) {
        return withApiPath(apiPath);
    }
}

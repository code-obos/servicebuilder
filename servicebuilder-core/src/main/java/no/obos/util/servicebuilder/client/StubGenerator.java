package no.obos.util.servicebuilder.client;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Singular;
import no.obos.util.servicebuilder.Constants;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StubGenerator {
    //    final String appToken;
    final Client client;
    final URI uri;
    final String userToken;
    @Singular
    final ImmutableList<Cookie> cookies;
    @Singular
    final ImmutableMap<String, String> headers;

    public static StubGenerator defaults(Client client, URI uri) {
        return new StubGenerator(client, uri, null, ImmutableList.of(), ImmutableMap.of());
    }

    public <T> T generateClient(Class<T> resource) {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        MultivaluedMap<String, Object> headerArg = new MultivaluedHashMap<>(headers);
        if (! Strings.isNullOrEmpty(userToken)) {
            headerArg.putSingle(Constants.USERTOKENID_HEADER, userToken);
        }

        WebTarget webTarget = clientToUse.target(uri);

        return WebResourceFactory.newResource(resource, webTarget, false, headerArg, cookies, new Form());
    }

    public StubGenerator userToken(String userToken) {return Objects.equals(this.userToken, userToken) ? this : new StubGenerator(this.client, this.uri, userToken, this.cookies, this.headers);}

    public StubGenerator cookies(ImmutableList<Cookie> cookies) {return this.cookies == cookies ? this : new StubGenerator(this.client, this.uri, this.userToken, cookies, this.headers);}

    public StubGenerator headers(ImmutableMap<String, String> headers) {return this.headers == headers ? this : new StubGenerator(this.client, this.uri, this.userToken, this.cookies, headers);}
}

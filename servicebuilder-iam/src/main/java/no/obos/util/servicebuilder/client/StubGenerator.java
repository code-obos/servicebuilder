package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Singular;
import no.obos.util.servicebuilder.Constants;
import org.elasticsearch.common.Strings;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;

@Builder
public class StubGenerator {
    //    final String appToken;
    final URI uri;
    final Client client;
    final String userToken;
    @Singular
    final ImmutableList<Cookie> cookies;
    @Singular
    final ImmutableMap<String, String> headers;

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
}

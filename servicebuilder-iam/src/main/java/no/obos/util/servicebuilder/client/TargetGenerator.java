package no.obos.util.servicebuilder.client;

import com.google.common.collect.Maps;
import lombok.Builder;
import no.obos.util.servicebuilder.Constants;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.util.Map;

@Builder
public class TargetGenerator {
    final URI uri;
    final Client client;
    final String userToken;

    public WebTarget generate() {
        Client clientToUse = client != null
                ? client
                : ClientBuilder.newClient();

        Map<String, String> headers = Maps.newHashMap();

        if (userToken != null) {
            headers.put(Constants.USERTOKENID_HEADER, userToken);
        }


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

        return target;
    }
}

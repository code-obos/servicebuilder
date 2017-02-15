package no.obos.util.servicebuilder.client;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Map;

public class WebTargetRequestHeaderFilter implements ClientRequestFilter {
    public final static String MAP_NAME = "headers";

    final Map<String, String> headersToSet;

    @Inject
    public WebTargetRequestHeaderFilter(@Named(MAP_NAME) Map<String, String> headersToSet) {this.headersToSet = headersToSet;}


    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> requestHeaders = requestContext.getHeaders();
        this.headersToSet.entrySet().forEach(entry -> requestHeaders.putSingle(entry.getKey(), entry.getValue()));
    }
}

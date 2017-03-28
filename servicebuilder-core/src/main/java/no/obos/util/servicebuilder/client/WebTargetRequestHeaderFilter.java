package no.obos.util.servicebuilder.client;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

@AllArgsConstructor
public class WebTargetRequestHeaderFilter implements ClientRequestFilter {
    final ImmutableMap<String, String> headersToSet;


    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> requestHeaders = requestContext.getHeaders();
        this.headersToSet.entrySet().forEach(entry -> requestHeaders.putSingle(entry.getKey(), entry.getValue()));
    }
}

package no.obos.util.servicebuilder.client;

import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

@Slf4j
public class ClientLogFilter implements ClientRequestFilter, ClientResponseFilter{
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
      log.debug(requestContext.getUri().toString());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        log.debug(requestContext.getUri().toString() + " response: " + responseContext.getStatus());
    }
}

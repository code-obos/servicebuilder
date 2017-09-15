package no.obos.util.servicebuilder.client;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;

@Priority(Priorities.USER)
@Slf4j
public class ClientLogFilter implements ClientRequestFilter, ClientResponseFilter {
    public static final String PROPERTYNAME = "ClientLogFilter.startTime";

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.setProperty(PROPERTYNAME, System.nanoTime());
        log.info(getCallSignature(requestContext));
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        Long totalMillis = null;

        Long startNanos = (Long) requestContext.getProperty(PROPERTYNAME);
        if (startNanos != null) {
            long totalNanos = System.nanoTime() - startNanos;
            totalMillis = totalNanos / 1_000_000;
        }
        log.info(getCallSignature(requestContext) + " response: " + responseContext.getStatus() + ", millis: " + totalMillis);
    }

    private static String getCallSignature(ClientRequestContext requestContext) {
        return requestContext.getMethod() + " " + requestContext.getUri().toString();
    }
}

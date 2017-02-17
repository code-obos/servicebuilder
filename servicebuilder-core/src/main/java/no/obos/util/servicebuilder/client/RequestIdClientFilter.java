package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.Constants;
import org.slf4j.MDC;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class RequestIdClientFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String requestId = MDC.get(Constants.X_OBOS_REQUEST_ID);
        if (requestId != null) {
            requestContext.getHeaders().putSingle(Constants.X_OBOS_REQUEST_ID, requestId);
        }
    }
}

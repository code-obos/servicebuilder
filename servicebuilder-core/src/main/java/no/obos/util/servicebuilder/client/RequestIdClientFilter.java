package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.model.Constants;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Priority(Priorities.HEADER_DECORATOR)
public class RequestIdClientFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) {
        String requestId = MDC.get(Constants.X_REQUEST_ID);
        if (requestId != null) {
            requestContext.getHeaders().putSingle(Constants.X_REQUEST_ID, requestId);
        }
    }
}

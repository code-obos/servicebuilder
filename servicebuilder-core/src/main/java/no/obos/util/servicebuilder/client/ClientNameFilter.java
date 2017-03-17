package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.model.Constants;
import org.jvnet.hk2.annotations.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

@Priority(Priorities.HEADER_DECORATOR)
public class ClientNameFilter implements ClientRequestFilter {
    public static final String CLIENT_APPNAME = "client-appname";
    private final String clientAppName;

    @Inject
    public ClientNameFilter(@Named(CLIENT_APPNAME) @Optional String clientAppName) {
        this.clientAppName = clientAppName;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (clientAppName != null) {
            requestContext.getHeaders().putSingle(Constants.CLIENT_APPNAME_HEADER, clientAppName);
        }
    }
}

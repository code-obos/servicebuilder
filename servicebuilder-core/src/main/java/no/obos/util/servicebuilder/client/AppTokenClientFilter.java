package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.model.Constants;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.util.function.Supplier;

@Priority(Priorities.HEADER_DECORATOR)
public class AppTokenClientFilter implements ClientRequestFilter {
    public static final String APP_TOKEN_SUPPLIER_BIND_NAME = "appTokenSupplier";

    final Supplier<String> appTokenSupplier;

    @Inject
    public AppTokenClientFilter(@Named(APP_TOKEN_SUPPLIER_BIND_NAME) Supplier<String> appTokenSupplier) {
        this.appTokenSupplier = appTokenSupplier;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String appTokenId = appTokenSupplier.get();
        if (appTokenId != null) {
            requestContext.getHeaders().putSingle(Constants.APPTOKENID_HEADER, appTokenId);
        }
    }

}

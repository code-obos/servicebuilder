package no.obos.util.servicebuilder.usertoken;

import javax.ws.rs.container.ContainerRequestContext;

public interface UserTokenAuthenticatedHandler {
    void handle(ContainerRequestContext requestContext);
}

package no.obos.util.servicebuilder.usertoken;

import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.UserTokenRequired;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION + 1)
public class UserTokenBlockingFilter implements ContainerRequestFilter {

    private final ResourceInfo resourceInfo;
    private final UserTokenFilterAddon configuration;
    private final SecurityContext securityContext;

    @Inject
    public UserTokenBlockingFilter(@Context ResourceInfo resourceInfo, UserTokenFilterAddon configuration, SecurityContext securityContext) {
        this.resourceInfo = resourceInfo;
        this.configuration = configuration;
        this.securityContext = securityContext;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();

        if (aboslutePath.contains("swagger.json") || "OPTIONS".equals(requestMethod)) {
            return;
        }

        if (requireUserToken() && securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("UsertokenId required");
        }
    }

    private boolean requireUserToken() {
        UserTokenRequired methodAnnotation = resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getAnnotation(UserTokenRequired.class)
                : null;
        UserTokenRequired classAnnotation = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(UserTokenRequired.class)
                : null;

        if (methodAnnotation != null) {
            return methodAnnotation.value();
        }
        if (classAnnotation != null) {
            return classAnnotation.value();
        }

        return configuration.requireUserTokenByDefault;
    }
}

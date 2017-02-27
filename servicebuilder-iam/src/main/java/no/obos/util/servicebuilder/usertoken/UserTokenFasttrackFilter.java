package no.obos.util.servicebuilder.usertoken;

import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.UserTokenRequired;

import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;

public class UserTokenFasttrackFilter implements ContainerRequestFilter {

    final private ResourceInfo resourceInfo;

    final UserTokenFilterAddon configuration;

    final SecurityContext securityContext;

    @Inject
    public UserTokenFasttrackFilter(@Context ResourceInfo resourceInfo, UserTokenFilterAddon configuration, SecurityContext securityContext) {
        this.resourceInfo = resourceInfo;
        this.configuration = configuration;
        this.securityContext = securityContext;
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Vi slipper gjennom CORS OPTIONS, etc...
        if (allwaysAccept(requestContext)) {
            return;
        }

        if (configuration.requireUserToken && securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("Usertoken required");
        }

    }


    public boolean allwaysAccept(ContainerRequestContext requestContext) {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();
        UserTokenRequired methodAnnotation = resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getAnnotation(UserTokenRequired.class)
                : null;
        UserTokenRequired classAnnotation = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(UserTokenRequired.class)
                : null;
        boolean annotationFasttrack = false;
        if (methodAnnotation != null) {
            annotationFasttrack = ! methodAnnotation.value();
        } else if (classAnnotation != null) {
            annotationFasttrack = ! classAnnotation.value();
        }

        return aboslutePath.contains("swagger")
                || "OPTIONS".equals(requestMethod)
                || annotationFasttrack;
    }

}

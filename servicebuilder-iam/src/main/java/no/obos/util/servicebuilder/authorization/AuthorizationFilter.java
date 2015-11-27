package no.obos.util.servicebuilder.authorization;

import com.google.common.base.Strings;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceClientException;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.AuthorizationFilterAddon;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

@PreMatching
public class AuthorizationFilter implements ContainerRequestFilter {


    @Inject
    private TokenServiceClient tokenServiceClient;

    @Inject
    @Named(AuthorizationFilterAddon.BIND_NAME_WHITELIST)
    private String[] whitelistUris;

    @Inject
    UibBrukerProvider uibBrukerProvider;

    @Inject
    @Named(AuthorizationFilterAddon.BIND_NAME_DEFAULT_REQUIRE_USERTOKEN)
    Boolean requireUserToken;

    @Override
    @SuppressWarnings("squid:S1166")
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String usertokenId = requestContext.getHeaderString(AuthorizationFilterAddon.USERTOKENID_HEADER);

        for (String whitelistUri : whitelistUris) {
            if (requestContext.getUriInfo().getPath().contains(whitelistUri)) {
                return;
            }
        }

        // Vi slipper gjennom CORS OPTIONS
        if ("OPTIONS".equals(requestContext.getMethod())) {
            return;
        }


        if (requireUserToken && Strings.isNullOrEmpty(usertokenId)) {
            throw new NotAuthorizedException("Usertoken required");
        }

        if (! Strings.isNullOrEmpty(usertokenId)) {
            UserToken userToken;
            try {
                userToken = tokenServiceClient.getUserTokenById(usertokenId);
            } catch (TokenServiceClientException e) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not valid");
            }
            if (requireUserToken && userToken == null) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not authorized");
            } else if (userToken != null) {
                UibBruker bruker = uibBrukerProvider.newUibBruker(userToken);
                if (bruker == null) {
                    throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not authorized");
                }
                requestContext.setSecurityContext(new AutentiseringsContext(bruker));
            }
        }
    }

    public static class AutentiseringsContext implements SecurityContext {

        private final UibBruker bruker;

        public AutentiseringsContext(UibBruker uibBruker) {
            this.bruker = uibBruker;
        }

        @Override
        public Principal getUserPrincipal() {
            return bruker;
        }

        @Override
        public boolean isUserInRole(String role) {
            return bruker.isUserInRole(role);
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }

    }

}

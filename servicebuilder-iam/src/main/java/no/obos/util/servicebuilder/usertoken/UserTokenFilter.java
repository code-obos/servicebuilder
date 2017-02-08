package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceClientException;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.UserTokenFilterAddon;

import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

@PreMatching
public class UserTokenFilter implements ContainerRequestFilter {

    final private TokenServiceClient tokenServiceClient;

    final UserTokenFilterAddon configuration;

    @Inject
    public UserTokenFilter(TokenServiceClient tokenServiceClient, UserTokenFilterAddon configuration) {
        this.tokenServiceClient = tokenServiceClient;
        this.configuration = configuration;
    }

    @Override
    @SuppressWarnings("squid:S1166")
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String usertokenId = requestContext.getHeaderString(Constants.USERTOKENID_HEADER);

        // Vi slipper gjennom CORS OPTIONS, etc...
        if (allwaysAccept(requestContext)) {
            return;
        }

        if (configuration.requireUserToken && Strings.isNullOrEmpty(usertokenId)) {
            throw new NotAuthorizedException("Usertoken required");
        }

        if (! Strings.isNullOrEmpty(usertokenId)) {
            UserToken userToken;
            try {
                userToken = tokenServiceClient.getUserTokenById(usertokenId);
            } catch (TokenServiceClientException e) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not valid", e);
            }
            if (configuration.requireUserToken && userToken == null) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not authorized");
            } else if (userToken != null) {
                UibBruker bruker = configuration.uibBrukerProvider.newUibBruker(userToken);
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

    public boolean allwaysAccept(ContainerRequestContext requestContext) {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();

        return aboslutePath.contains("swagger") ||
                "OPTIONS".equals(requestMethod) ||
                configuration.fasttrackFilter.test(requestContext);
    }

}

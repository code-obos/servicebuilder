package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.TokenServiceClientException;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.model.Constants;

import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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


        if (! Strings.isNullOrEmpty(usertokenId)) {
            UserToken userToken;
            try {
                userToken = tokenServiceClient.getUserTokenById(usertokenId);
            } catch (TokenServiceClientException e) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not valid", e);
            }
            if (configuration.requireUserTokenByDefault && userToken == null) {
                throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not authorized");
            } else if (userToken != null) {
                UibBruker bruker = UibBruker.ofUserToken(userToken);
                if (bruker == null) {
                    throw new NotAuthorizedException("UsertokenId: '" + usertokenId + "' not authorized");
                }
                List<String> tilgangerList = Lists.newArrayList();
                tilgangerList.addAll(configuration.userTokenTilganger.apply(userToken));
                tilgangerList.addAll(configuration.uibBrukerTilganger.apply(bruker));
                tilgangerList.addAll(bruker.roller.stream()
                        .map(rolle ->
                                configuration.uibRolleTilganger.stream()
                                        .map(it -> it.apply(rolle))
                                        .filter(Objects::nonNull)
                        ).flatMap(Function.identity())
                        .collect(Collectors.toSet())
                );
                configuration.uibBrukerTilganger.apply(bruker);
                ImmutableSet<String> tilganger = ImmutableSet.copyOf(tilgangerList.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .collect(Collectors.toList())
                );

                requestContext.setSecurityContext(new AutentiseringsContext(bruker, tilganger));
            }
        }
    }

    @AllArgsConstructor
    public static class AutentiseringsContext implements SecurityContext {

        private final UibBruker bruker;
        private final ImmutableSet<String> tilganger;


        @Override
        public Principal getUserPrincipal() {
            return bruker;
        }

        @Override
        public boolean isUserInRole(String role) {
            return tilganger.contains(role.trim().toUpperCase());
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

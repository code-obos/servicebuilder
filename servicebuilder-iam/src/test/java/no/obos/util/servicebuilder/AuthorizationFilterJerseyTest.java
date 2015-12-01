package no.obos.util.servicebuilder;

import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.authorization.BasicUibBruker;
import no.obos.util.servicebuilder.authorization.UibToJavaxRole;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterJerseyTest extends JerseyTest {

    @Mock
    private TokenServiceClient tokenServiceClient;

    static final String javaxRole = "Mr. Tilgang";
    static final String uibRoleNameValid = "superbruker";
    static final String uibRoleNameInvalid = "middelmådigbruker";

    @Override
    protected Application configure() {
        return new JerseyConfig()
                .addRegistations(context -> context
                        .register(Resource.class)
                        .register(JacksonFeature.class)

                ).addBinder(binder -> {
                    binder.bind(tokenServiceClient).to(TokenServiceClient.class);
                })
                .with(AuthorizationFilterAddon.defaults(BasicUibBruker.provider(UIB_TO_JAVAX_ROLE)))
                .getResourceConfig();
    }

    @Test
    public void validUserTokenIsAccepted() {
        String usertoken = "valid-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameValid));

        Response response = target(Resource.PATH).path(Resource.RESOURCE_PATH).request()
                .header(AuthorizationFilterAddon.USERTOKENID_HEADER, usertoken)
                .get();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void inValidUserTokenIsRejected() {
        String usertoken = "invalid-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameInvalid));

        Response response = target(Resource.PATH).path(Resource.RESOURCE_PATH).request()
                .header(AuthorizationFilterAddon.USERTOKENID_HEADER, usertoken)
                .get();
        assertEquals(403, response.getStatus());
    }

    public static final UibToJavaxRole UIB_TO_JAVAX_ROLE = new UibToJavaxRole() {

        @Override public String getJavaxRoleName() {
            return javaxRole;
        }

        @Override public boolean tilgangForUibRolle(UserRole role) {
            return uibRoleNameValid.equals(role.getIdentity().getName());
        }
    };

    private UserToken getUserToken(String uibRoleName) {
        UserToken expectedUserToken = new UserToken();

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(getUserRole(uibRoleName));

        expectedUserToken.setRoles(userRoles);
        return expectedUserToken;
    }


    private UserRole getUserRole(String roleName) {
        UserRole userRole = new UserRole();
        userRole.setIdentity(new UserRole.Identity(roleName, "201", "999"));
        userRole.setValue("");
        return userRole;
    }

    @Path(Resource.PATH)
    public static class Resource {
        public static final String PATH = "yeah";
        public static final String RESOURCE_PATH = "brille";

        @GET
        @Produces("text/plain")
        @RolesAllowed(javaxRole)
        @Path(RESOURCE_PATH)
        public String getProtectedResource() {
            return "Very secret";
        }
    }
}

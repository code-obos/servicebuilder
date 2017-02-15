package no.obos.util.servicebuilder;

import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.usertoken.BasicUibBruker;
import no.obos.util.servicebuilder.usertoken.UibToJavaxRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterJerseyTest {

    private static TokenServiceClient tokenServiceClient = Mockito.mock(TokenServiceClient.class);

    static final String javaxRole = "Mr. Tilgang";
    static final String uibRoleNameValid = "superbruker";
    static final String uibRoleNameInvalid = "middelmådigbruker";
    ServiceConfig serviceConfig = ServiceConfig.builder()
            .serviceDefinition(ServiceDefinition.simple(Resource.class))
            //                .register(Resource.class)
            .bind(tokenServiceClient, TokenServiceClient.class)
            .addon(UserTokenFilterAddon.builder()
                    .uibBrukerProvider(BasicUibBruker.provider(UIB_TO_JAVAX_ROLE))
                    .build()
            ).build();

    TestServiceRunner testServiceRunner = TestServiceRunner.builder()
            .serviceConfig(serviceConfig)
            .clientConfigurator(cfg -> cfg.exceptionMapping(false))
            .build();



    @Test
    public void validUserTokenIsAccepted() {
        String usertoken = "valid-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameValid));

        Response response = testServiceRunner.oneShot(((clientConfig, uri) ->
                ClientBuilder.newClient(clientConfig).target(uri)
                        .path(Resource.PATH)
                        .path(Resource.RESOURCE_PATH).request()
                        .header(Constants.USERTOKENID_HEADER, usertoken)
                        .get()
        ));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void inValidUserTokenIsRejected() {
        String usertoken = "invalid-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameInvalid));

        Response response = testServiceRunner.oneShot(((clientConfig, uri) ->
                ClientBuilder.newClient(clientConfig).target(uri)
                        .path(Resource.PATH)
                        .path(Resource.RESOURCE_PATH).request()
                        .header(Constants.USERTOKENID_HEADER, usertoken)
                        .get()
        ));
        assertEquals(403, response.getStatus());
    }

    public static final UibToJavaxRole UIB_TO_JAVAX_ROLE = new UibToJavaxRole() {

        @Override
        public String getJavaxRoleName() {
            return javaxRole;
        }

        @Override
        public boolean tilgangForUibRolle(UserRole role) {
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

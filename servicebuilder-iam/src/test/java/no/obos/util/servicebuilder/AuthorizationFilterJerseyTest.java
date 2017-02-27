package no.obos.util.servicebuilder;

import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.addon.ExceptionMapperAddon;
import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.UserTokenRequired;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.usertoken.UibBruker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hk2.annotations.Optional;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterJerseyTest {

    private static TokenServiceClient tokenServiceClient = Mockito.mock(TokenServiceClient.class);

    static final String javaxRole = "Mr. Tilgang";
    static final String uibRoleNamePrioritized = "superbruker";
    static final String uibRoleNameUnprioritized = "middelmådigbruker";
    ServiceConfig serviceConfig = ServiceConfig.defaults(ServiceDefinition.simple(Resource.class))
            //                .register(Resource.class)
            .bind(tokenServiceClient, TokenServiceClient.class)
            .bind(ResourceImpl.class, Resource.class)
            .addon(ExceptionMapperAddon.defaults)
            .addon(UserTokenFilterAddon.defaults
                    .plusUibRolleTilgang(uibRolle -> uibRoleNamePrioritized.equalsIgnoreCase(uibRolle.navn) ? javaxRole : null)
            );

    TestServiceRunner testServiceRunner = TestServiceRunner.defaults(serviceConfig);


    @Test
    public void validUserTokenIsAccepted() {
        String usertoken = "invalid-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameUnprioritized));

        String actual = testServiceRunner
                .withStubConfigurator(cfg -> cfg.withUserToken(usertoken))
                .oneShot(Resource.class, Resource::getProtectedResource);

        Assert.assertEquals("adolf", actual);
    }

    @Test
    public void validUserTokenIsAcceptedWithRole() {
        String usertoken = "prioritized-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNamePrioritized));

        String actual = testServiceRunner
                .withStubConfigurator(cfg -> cfg.withUserToken(usertoken))
                .oneShot(Resource.class, Resource::getProtectedWithRoleResource);

        Assert.assertEquals("adolf", actual);
    }

    @Test
    public void inValidUserTokenIsRejected() {
        String usertoken = "lowly-usertoken";

        Mockito.when(tokenServiceClient.getUserTokenById(usertoken)).thenReturn(getUserToken(uibRoleNameUnprioritized));


        try {
            testServiceRunner
                    .withStubConfigurator(cfg -> cfg.withUserToken(usertoken))
                    .oneShot(Resource.class, Resource::getProtectedWithRoleResource);
            Assert.fail();
        } catch (ExternalResourceException ex) {
            assertEquals(Integer.valueOf(403), ex.getMetaData().httpStatus);
        }
    }

    @Test
    public void noUserTokenIsRejected() {
        try {
            testServiceRunner
                    .oneShot(Resource.class, Resource::getProtectedResource);
            Assert.fail();
        } catch (ExternalResourceException ex) {
            assertEquals(Integer.valueOf(401), ex.getMetaData().httpStatus);
        }
    }

    @Test
    public void noUserTokenIsAcceptedWhenAnnotated() {
        String actual = testServiceRunner
                .oneShot(Resource.class, Resource::getUnProtectedResource);
        assertEquals("eple", actual);
    }

    private UserToken getUserToken(String uibRoleName) {
        UserToken expectedUserToken = new UserToken();

        List<UserRole> userRoles = new ArrayList<>();
        userRoles.add(getUserRole(uibRoleName));

        expectedUserToken.setRoles(userRoles);
        expectedUserToken.setEtternavn("adolf");
        return expectedUserToken;
    }


    private UserRole getUserRole(String roleName) {
        UserRole userRole = new UserRole();
        userRole.setIdentity(new UserRole.Identity(roleName, "201", "999"));
        userRole.setValue("");
        return userRole;
    }

    @Path("")
    public interface Resource {

        @GET
        @RolesAllowed(javaxRole)
        @Path("protectedWithRole")
        String getProtectedWithRoleResource();

        @GET
        @Path("protected")
        String getProtectedResource();

        @GET
        @Path("unprotected")
        @UserTokenRequired(false)
        String getUnProtectedResource();
    }


    public static class ResourceImpl implements Resource {
        @Inject
        @Optional
        UibBruker uibBruker;

        @Override
        public String getProtectedWithRoleResource() {
            return uibBruker.etternavn;
        }

        @Override
        public String getProtectedResource() {
            return uibBruker.etternavn;
        }

        @Override
        public String getUnProtectedResource() {
            return "eple";
        }
    }
}

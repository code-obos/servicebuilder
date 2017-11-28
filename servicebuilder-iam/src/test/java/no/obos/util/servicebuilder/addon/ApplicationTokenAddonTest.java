package no.obos.util.servicebuilder.addon;

import io.swagger.annotations.Api;
import no.obos.iam.tokenservice.ApplicationToken;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.annotations.AppIdWhiteList;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.model.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.eclipse.jetty.http.HttpStatus.UNAUTHORIZED_401;
import static org.mockito.Mockito.mock;

public class ApplicationTokenAddonTest {

    private static final String AUTHORIZED = "1";
    private static final String AUTHORIZED2 = "2";
    private static final String AUTHORIZED3 = "3";
    private static final int AUTHORIZED2_INT = 2;
    private static final int AUTHORIZED3_INT = 3;
    private static final String UNAUTHORIZED = "42";

    private static final String INVALID = "BANAN";
    private static final String VALID = "GOOD-APP-TOKEN-ID";
    private static final TokenServiceClient tokenServiceClient = mock(TokenServiceClient.class);


    @Api
    public @Path("balle") interface ResourceNoAnnotation {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        void get();
    }

    @Test
    public void should_allow_calls_with_valid_and_authorized_apptoken() {
        appTokenMock(VALID, AUTHORIZED);

        getTestServiceRunner(VALID, ResourceNoAnnotation.class, mock(ResourceNoAnnotation.class))
                .oneShotVoid(ResourceNoAnnotation.class, ResourceNoAnnotation::get);
    }

    @Test
    public void authorizes_calling_application_from_allowed_application_ids() {
        appTokenMock(VALID, UNAUTHORIZED);

        try {
            getTestServiceRunner(VALID, ResourceNoAnnotation.class, mock(ResourceNoAnnotation.class))
                    .oneShotVoid(ResourceNoAnnotation.class, ResourceNoAnnotation::get);
        } catch (ExternalResourceException e) {

            checkError(e, UNAUTHORIZED_401, "Apptokenid '" + VALID + "' is UNAUTHORIZED");

        }
    }

    @Test
    public void checks_validity_of_apptoken() {
        Mockito.when(tokenServiceClient.getApptokenById(INVALID)).thenReturn(null);
        try {
            getTestServiceRunner(INVALID, ResourceNoAnnotation.class, mock(ResourceNoAnnotation.class))
                    .oneShotVoid(ResourceNoAnnotation.class, ResourceNoAnnotation::get);
        } catch (ExternalResourceException e) {
            checkError(e, UNAUTHORIZED_401, "Apptokenid '" + INVALID + "' is UNAUTHORIZED");
        }
    }


    @Api
    @Path("kaka")
    @AppIdWhiteList({AUTHORIZED2_INT, AUTHORIZED3_INT})
    public interface ResourceFineGrained {
        @GET
        @AppIdWhiteList({AUTHORIZED2_INT})
        @Produces(MediaType.APPLICATION_JSON)
        void get();

        @GET
        @Path("eple")
        @Produces(MediaType.APPLICATION_JSON)
        void get2();
    }


    @Test
    public void annotationsAllowFineGrainedAccessToEndpointsAmongAuthorizedApplications_expectUnauthorizedAppToken() {
        appTokenMock(VALID, AUTHORIZED);

        try {
            getTestServiceRunner(VALID, ResourceFineGrained.class, mock(ResourceFineGrained.class))
                    .oneShotVoid(ResourceFineGrained.class, ResourceFineGrained::get);
        } catch (ExternalResourceException e) {
            checkError(e, UNAUTHORIZED_401, "Apptokenid '" + VALID + "' is UNAUTHORIZED for this endpoint");
        }
    }

    @Test
    public void annotationsAllowFineGrainedAccessToEndpointsAmongAuthorizedApplications_expectSuccessAppToken2() {
        appTokenMock(VALID, AUTHORIZED2);

        getTestServiceRunner(VALID, ResourceFineGrained.class, mock(ResourceFineGrained.class))
                .oneShotVoid(ResourceFineGrained.class, ResourceFineGrained::get);
    }

    @Test
    public void annotationsAllowFineGrainedAccessToEndpointsAmongAuthorizedApplications_expectUnauthorizedAppToken3() {
        appTokenMock(VALID, AUTHORIZED3);

        try{
            getTestServiceRunner(VALID, ResourceFineGrained.class, mock(ResourceFineGrained.class))
                    .oneShotVoid(ResourceFineGrained.class, ResourceFineGrained::get);
        } catch (ExternalResourceException e) {
            checkError(e, UNAUTHORIZED_401, "Apptokenid '" + VALID + "' is UNAUTHORIZED for this endpoint");
        }
    }

    @Test
    public void annotationsAllowFineGrainedAccessToEndpointsAmongAuthorizedApplications_expectSuccessAppToken3() {
        appTokenMock(VALID, AUTHORIZED3);

        getTestServiceRunner(VALID, ResourceFineGrained.class, mock(ResourceFineGrained.class))
                .oneShotVoid(ResourceFineGrained.class, ResourceFineGrained::get2);
    }

    @Test
    public void annotationsAllowFineGrainedAccessToSpesificEndpointsAmongAuthorizedApplications_expectSuccessAppToken2() {
        appTokenMock(VALID, AUTHORIZED2);

        getTestServiceRunner(VALID, ResourceFineGrained.class, mock(ResourceFineGrained.class))
                .oneShotVoid(ResourceFineGrained.class, ResourceFineGrained::get2);
    }

    @Api
    @Path("mele")
    public interface ResourceSuperFineGrained {
        @GET
        @AppIdWhiteList({AUTHORIZED2_INT})
        @Produces(MediaType.APPLICATION_JSON)
        void get();

        @GET
        @Path("dele")
        @Produces(MediaType.APPLICATION_JSON)
        void get2();
    }

    @Test
    public void annotationSetOnlyOnSpecificEndpoint() {
        appTokenMock(VALID, AUTHORIZED2);

        getTestServiceRunner(VALID, ResourceSuperFineGrained.class, mock(ResourceSuperFineGrained.class))
                .oneShotVoid(ResourceSuperFineGrained.class, ResourceSuperFineGrained::get);
    }

    @Test
    public void annotationSetOnlyOnSpecificEndpoint2() {
        appTokenMock(VALID, AUTHORIZED2);

        getTestServiceRunner(VALID, ResourceSuperFineGrained.class, mock(ResourceSuperFineGrained.class))
                .oneShotVoid(ResourceSuperFineGrained.class, ResourceSuperFineGrained::get2);
    }

    @Test
    public void annotationSetOnlyOnSpecificEndpoint_getAUTHORIZED3_expectUnauthorized() {
        appTokenMock(VALID, AUTHORIZED3);

        try {
            getTestServiceRunner(VALID, ResourceSuperFineGrained.class, mock(ResourceSuperFineGrained.class))
                    .oneShotVoid(ResourceSuperFineGrained.class, ResourceSuperFineGrained::get);
        } catch (ExternalResourceException e) {
            checkError(e, UNAUTHORIZED_401, "Apptokenid '" + VALID + "' is UNAUTHORIZED for this endpoint");
        }
    }

    @Test
    public void annotationSetOnlyOnSpecificEndpoint_get2AUTHORIZED3() {
        appTokenMock(VALID, AUTHORIZED3);

        getTestServiceRunner(VALID, ResourceSuperFineGrained.class, mock(ResourceSuperFineGrained.class))
                .oneShotVoid(ResourceSuperFineGrained.class, ResourceSuperFineGrained::get2);
    }

    private <T> TestServiceRunner getTestServiceRunner(String apptokenHeader, Class<T> resource, T resourceImpl) {
        return TestServiceRunner.defaults(
                ServiceConfig.defaults(ServiceDefinitionUtil.simple(resource))
                        .addon(ExceptionMapperAddon.defaults)
                        .addon(ApplicationTokenFilterAddon.defaults.acceptedAppIds(AUTHORIZED + "," + AUTHORIZED2 + "," + AUTHORIZED3).swaggerImplicitHeaders(false))
                        .addon(TokenServiceAddon.defaults.tokenServiceClient(tokenServiceClient))
                        .bind(resourceImpl, resource)
        ).stubConfigurator(s -> s.header(Constants.APPTOKENID_HEADER, apptokenHeader));
    }

    private void appTokenMock(String apptokenID, String appId) {
        ApplicationToken applicationToken = new ApplicationToken();
        applicationToken.setApplicationId(appId);
        Mockito.when(tokenServiceClient.getApptokenById(apptokenID)).thenReturn(applicationToken);
    }

    private void checkError(ExternalResourceException e, int expectedStatus, String expectedMessage) {
        Assert.assertEquals(expectedMessage, e.getMetaData().httpResponseMetaData.problemResponse.detail);
        Assert.assertEquals(expectedStatus, e.getMetaData().httpResponseMetaData.status);
    }
}

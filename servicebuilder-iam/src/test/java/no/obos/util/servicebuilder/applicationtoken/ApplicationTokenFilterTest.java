package no.obos.util.servicebuilder.applicationtoken;

import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.access.TokenCheckResult;
import no.obos.iam.tokenservice.ApplicationToken;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.servicebuilder.addon.ApplicationTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.AppIdWhitelist;
import no.obos.util.servicebuilder.model.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTokenFilterTest {

    private static final String APPTOKEN_ID = "abc-123";
    private static final int APP_ID_WHITELISTED = 123456789;
    private static final int APP_ID_NOT_WHITELISTED = 987654321;

    @Mock
    private ApplicationTokenAccessValidator applicationTokenAccessValidator;
    @Mock
    private TokenServiceClient tokenServiceClient;
    @Mock
    private ResourceInfo resourceInfo;
    @Mock
    private Supplier<Boolean> fasttrack;

    private ContainerRequestFilter requestFilter;


    @SuppressWarnings("WeakerAccess")
    static class Resource {

        public static void unannotated() {
        }

        @AppIdWhitelist(APP_ID_WHITELISTED)
        public static void annotatedExclusive() {
        }

        @AppIdWhitelist(value = APP_ID_WHITELISTED, exclusive = false)
        public static void annotatedNonExclusive() {
        }
    }

    @Before
    public void setUp() throws Exception {
        requestFilter = new ApplicationTokenFilter(
                applicationTokenAccessValidator,
                ApplicationTokenFilterAddon.defaults.fasttrackFilter(it -> fasttrack.get()),
                tokenServiceClient,
                resourceInfo);

        when(fasttrack.get()).thenReturn(false);
        when(resourceInfo.getResourceMethod()).thenReturn(Resource.class.getMethod("unannotated"));
    }

    @Test
    public void filter_alwaysAccept() throws Exception {
        when(fasttrack.get()).thenReturn(true);

        ContainerRequestContext requestContext = createRequestContextMock();
        requestFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void filter_noApptokenId_null() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        requestFilter.filter(requestContext);

        verify(requestContext).abortWith(any());
    }

    @Test
    public void filter_noApptokenId_empty() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn("");

        requestFilter.filter(requestContext);

        verify(requestContext).abortWith(any());
    }

    @Test
    public void filter_apptokenId_authorized() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_WHITELISTED));
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.AUTHORIZED);

        requestFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void filter_apptokenId_unauthorized() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_WHITELISTED));
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.UNAUTHORIZED);

        requestFilter.filter(requestContext);

        verify(requestContext).abortWith(any());
    }

    @Test
    public void filter_apptokenId_authorized_whitelisted() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.AUTHORIZED);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_WHITELISTED));
        when(resourceInfo.getResourceMethod()).thenReturn(Resource.class.getMethod("annotatedExclusive"));

        requestFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void filter_apptokenId_unauthorized_whitelisted() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.UNAUTHORIZED);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_WHITELISTED));
        when(resourceInfo.getResourceMethod()).thenReturn(Resource.class.getMethod("annotatedExclusive"));

        requestFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    @Test
    public void filter_apptokenId_authorized_notWhitelisted_exclusive() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.AUTHORIZED);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_NOT_WHITELISTED));
        when(resourceInfo.getResourceMethod()).thenReturn(Resource.class.getMethod("annotatedExclusive"));

        requestFilter.filter(requestContext);

        verify(requestContext).abortWith(any());
    }

    @Test
    public void filter_apptokenId_authorized_notWhitelisted_nonExclusive() throws Exception {
        ContainerRequestContext requestContext = createRequestContextMock();
        when(requestContext.getHeaderString(Constants.APPTOKENID_HEADER)).thenReturn(APPTOKEN_ID);
        when(applicationTokenAccessValidator.checkApplicationTokenId(APPTOKEN_ID))
                .thenReturn(TokenCheckResult.AUTHORIZED);
        when(tokenServiceClient.getApptokenById(APPTOKEN_ID)).thenReturn(createApplicationToken(APP_ID_NOT_WHITELISTED));
        when(resourceInfo.getResourceMethod()).thenReturn(Resource.class.getMethod("annotatedNonExclusive"));

        requestFilter.filter(requestContext);

        verify(requestContext, never()).abortWith(any());
    }

    private static ApplicationToken createApplicationToken(int applicationId) {
        ApplicationToken token = new ApplicationToken();
        token.setApplicationId(String.valueOf(applicationId));
        return token;
    }

    private static ContainerRequestContext createRequestContextMock() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(new URI("http://domain.tld/path"));
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(requestContext.getMethod()).thenReturn("GET");
        return requestContext;
    }

}
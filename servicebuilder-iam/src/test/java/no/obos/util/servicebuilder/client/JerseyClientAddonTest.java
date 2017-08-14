package no.obos.util.servicebuilder.client;

import no.obos.iam.tokenservice.ApplicationToken;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestServiceFull;
import no.obos.util.servicebuilder.TestServiceFull.Controller;
import no.obos.util.servicebuilder.TestServiceFull.ResourceFull;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.JerseyClientAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.addon.TokenServiceAddon;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import org.jboss.logging.MDC;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JerseyClientAddonTest {

    private Controller nestedController = mock(Controller.class);

    private TestServiceRunner nestedService = TestServiceRunner.defaults(
            TestServiceFull.config
                    .addon(ObosLogFilterAddon.defaults)
                    .bind(nestedController, Controller.class)
    );

    @Test
    public void can_call_with_injected_stub() {
        //Given
        TestServiceFull.Call expected = getCall();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .apptoken(false)
                        .apiPrefix(null)
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_stub);

        //then
        verify(nestedController).isCallValid(argThat(new VersionAgnosticCall(expected)));
        nestedRuntime.stop();
    }

    @Test
    public void can_call_with_injected_stub_gets_apptoken() {
        //Given
        TestServiceFull.Call expected = getCall().toBuilder()
                .header(Constants.APPTOKENID_HEADER, "something")
                .build();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        TokenServiceClient tokenServiceClient = mock(TokenServiceClient.class);
        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(TokenServiceAddon.defaults.tokenServiceClient(tokenServiceClient))
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .apiPrefix(null)
                        .uri(nestedRuntime.uri)
                );

        //when
        when(tokenServiceClient.getApplicationToken()).thenReturn(new ApplicationToken() {
            public String getApplicationTokenId() {
                return "something";
            }
        });
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_stub);

        //then
        verify(nestedController).isCallValid(argThat(new VersionAgnosticCall(expected)));
        nestedRuntime.stop();
    }

    @Test
    public void can_call_with_injected_target_gets_apptoken() {
        //Given
        TestServiceFull.Call expected = getCall().toBuilder()
                .header(Constants.APPTOKENID_HEADER, "something")
                .build();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        TokenServiceClient tokenServiceClient = mock(TokenServiceClient.class);
        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(TokenServiceAddon.defaults.tokenServiceClient(tokenServiceClient))
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .uri(nestedRuntime.uri)
                );

        //when
        when(tokenServiceClient.getApplicationToken()).thenReturn(new ApplicationToken() {
            public String getApplicationTokenId() {
                return "something";
            }
        });
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_target);

        //then
        verify(nestedController).isCallValid(argThat(new VersionAgnosticCall(expected)));
        nestedRuntime.stop();
    }

    @Test
    public void can_call_with_injected_target() {
        //Given
        TestServiceFull.Call expected = getCall();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .apptoken(false)
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_target);

        //then
        verify(nestedController).isCallValid(argThat(new VersionAgnosticCall(expected)));
        nestedRuntime.stop();
    }

    @Test
    public void request_id_gets_carried_over() {
        MDC.put(Constants.X_OBOS_REQUEST_ID, "Banana");
        //Given
        TestServiceFull.Call expected = getCall()
                .toBuilder()
                .header(Constants.X_OBOS_REQUEST_ID, "Banana")
                .build();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .apptoken(false)
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_target);

        //then
        verify(nestedController).isCallValid(argThat(new VersionAgnosticCall(expected)));
        nestedRuntime.stop();
        MDC.remove(Constants.X_OBOS_REQUEST_ID);
    }

    @Path("service")
    public interface Api {
        @GET
        boolean call_with_stub();

        @GET
        @Path("target")
        boolean call_with_target();
    }


    static class ApiImpl implements Api {
        @Inject
        ResourceFull nested;

        @Inject
        @Named(TestServiceFull.NAME)
        WebTarget target;

        @Override
        public boolean call_with_stub() {
            return nested.get();
        }

        @Override
        public boolean call_with_target() {
            Response response = target.path(TestServiceFull.PATH)
                    .request()
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get();

            return response.readEntity(Boolean.class);
        }
    }


    private final ServiceDefinition serviceDefinition = ServiceDefinitionUtil.simple(Api.class);

    private TestServiceFull.Call getCall() {
        return TestServiceFull.Call.builder()
                .header("Accept", "application/json")
                .header("User-Agent", "^Jersey/\\d+\\.\\d+\\.\\d+ \\(Jersey InMemory Connector\\)$")
                .header(Constants.CLIENT_APPNAME_HEADER, "^anonymous_service:\\d+\\.\\d+$")
                .build();
    }

    private class VersionAgnosticCall extends ArgumentMatcher<TestServiceFull.Call> {

        private final TestServiceFull.Call expected;

        VersionAgnosticCall(TestServiceFull.Call expected) {
            this.expected = Objects.requireNonNull(expected);
        }

        @Override
        public boolean matches(Object argument) {
            return argument instanceof TestServiceFull.Call && matches((TestServiceFull.Call) argument);
        }

        private boolean matches(TestServiceFull.Call argument) {
            TestServiceFull.Call expected = this.expected;

            for (String headerKey : expected.headers.keySet()) {
                expected = exchangeExpectedHeaderWithArgumentHeaderIfMatches(expected, argument, headerKey);
            }

            return argument.equals(expected);
        }

        private TestServiceFull.Call exchangeExpectedHeaderWithArgumentHeaderIfMatches(
                TestServiceFull.Call expected,
                TestServiceFull.Call argument,
                String headerKey)
        {
            String argumentHeader = argument.headers.get(headerKey);
            String expectedHeader = expected.headers.get(headerKey);

            if (argumentHeader != null
                    && expectedHeader != null
                    && expectedHeader.startsWith("^")
                    && expectedHeader.endsWith("$")
                    && argumentHeader.matches(expectedHeader))
            {
                return expected.toBuilder().header(headerKey, argumentHeader).build();
            }
            return expected;
        }
    }
}

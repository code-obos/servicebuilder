package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.JerseyClientAddon;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinition;
import no.obos.util.servicebuilder.TestServiceFull;
import no.obos.util.servicebuilder.TestServiceFull.Controller;
import no.obos.util.servicebuilder.TestServiceFull.ResourceFull;
import no.obos.util.servicebuilder.TestServiceRunner;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JerseyClientAddonTest {
    Controller nestedController = mock(Controller.class);

    TestServiceRunner nestedService = TestServiceRunner.defaults(
            TestServiceFull.config
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
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_stub);

        //then
        verify(nestedController).isCallValid(eq(expected));
        nestedRuntime.stop();
    }

    @Test
    public void can_call_with_injected_stub_transferred_usertoken() {
        //Given
        TestServiceFull.Call expected = getCall().toBuilder()
                .header(Constants.USERTOKENID_HEADER, "something")
                .build();
        TestServiceRunner.Runtime nestedRuntime = nestedService.start().runtime;

        ServiceConfig outerServiceConfig = ServiceConfig.defaults(serviceDefinition)
                .bind(ApiImpl.class, Api.class)
                .addon(JerseyClientAddon.defaults(TestServiceFull.instance)
                        .clientConfigBase(nestedRuntime.clientConfig)
                        .forwardUsertoken(true)
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .stubConfigurator(it -> it.header(Constants.USERTOKENID_HEADER, "something"))
                .oneShot(Api.class, Api::call_with_stub);

        //then
        verify(nestedController).isCallValid(eq(expected));
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
                        .uri(nestedRuntime.uri)
                );

        //when
        TestServiceRunner.defaults(outerServiceConfig)
                .oneShot(Api.class, Api::call_with_target);

        //then
        verify(nestedController).isCallValid(eq(expected));
        nestedRuntime.stop();
    }

    @Path("service") public interface Api {
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


    final ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);

    TestServiceFull.Call getCall() {
        return TestServiceFull.Call.builder()
                .header("Accept", "application/json")
                .header("User-Agent", "Jersey/2.25.1 (Jersey InMemory Connector)")
                .build();
    }
}

package no.obos.util.servicebuilder.addon;

import io.swagger.annotations.Api;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestService.Resource;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.interfaces.ApplicationTokenIdAddon;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.LocalDate;

import static no.obos.util.servicebuilder.TestService.Payload;
import static no.obos.util.servicebuilder.TestService.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class JerseyClientAddonTest {
    Resource nestedResourceMock = Mockito.mock(Resource.class);


    @Test
    public void injection_of_client_works() {
        Payload expected = new Payload("eple", LocalDate.now().minusYears(1));

        when(nestedResourceMock.get()).thenReturn(expected);

        Payload actual =
                nestedTestService.oneShot((clientconfig, uri) -> TestServiceRunner.defaults(
                        outerServiceConfig
                                .addon(JerseyClientAddon.defaults(TestService.instance)
                                        .clientConfigBase(clientconfig)
                                        .apiPrefix(null)
                                        .apptoken(false)
                                        .uri(uri)
                                )
                ).oneShot(OuterResource.class, OuterResource::get));
        assertThat(actual).isEqualTo(expected);
    }


    TestServiceRunner nestedTestService = TestServiceRunner.defaults(
            ServiceConfig.defaults(instance)
                    .bind(nestedResourceMock, Resource.class)
    );

    ServiceConfig outerServiceConfig = ServiceConfig.defaults(ServiceDefinition.simple("outer", "1.0", OuterResource.class))
            .addon(ExceptionMapperAddon.defaults)
            .bind(OuterResourceImpl.class, OuterResource.class);


    @Api
    @Path("kake")
    public interface OuterResource {
        @GET
        @Produces("application/json")
        Payload get();
    }


    public static class OuterResourceImpl implements OuterResource {
        @Inject
        Resource nestedResource;

        @Override
        public Payload get() {
            return nestedResource.get();
        }
    }


    @Test
    public void dependent_on_tokenserviceaddon_if_apptoken_specified() {

        ServiceConfig serviceConfig = TestService.config
                .addon(JerseyClientAddon.defaults(TestService.instance));
        try {
            TestServiceRunner
                    .defaults(serviceConfig)
                    .oneShot(TestService.Resource.class, TestService.Resource::get);
        } catch (DependenceException ex) {
            assertThat(ex.independent).isEqualTo(ApplicationTokenIdAddon.class);
            assertThat(ex.dependent).isEqualTo(JerseyClientAddon.class);
        }
    }
}


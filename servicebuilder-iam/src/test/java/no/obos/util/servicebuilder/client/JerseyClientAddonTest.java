package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.ExceptionMapperAddon;
import no.obos.util.servicebuilder.JerseyClientAddon;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinition;
import no.obos.util.servicebuilder.TestServiceRunner;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;

public class JerseyClientAddonTest {

    static class NestedApiImpl implements NestedApi {
        @Context
        HttpHeaders headers;

        @Override
        public boolean shouldUpdate(LocalDate payload) {
            return "eple".equals(headers.getHeaderString(Constants.USERTOKENID_HEADER));
        }
    }


    static class ApiImpl implements Api {
        @Inject
        NestedApi resource;

        @Inject
        @Named(ServiceDefinition.ANONYMOUS_SERVICE_NAME)
        WebTarget target;

        @Override
        public LocalDate call(LocalDate payload) {
            boolean shouldUpdateResource = resource.shouldUpdate(payload);
            boolean shouldUpdateTarget = target.path("service").request().post(Entity.entity(payload, MediaType.APPLICATION_JSON_TYPE)).readEntity(Boolean.class);
            if (shouldUpdateResource != shouldUpdateTarget) {
                return null;
            }
            return shouldUpdateResource ? payload.plusYears(100) : payload;
        }
    }



    @Path("service") public interface NestedApi {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        boolean shouldUpdate(LocalDate payload);
    }


    @Path("service") public interface Api {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        LocalDate call(LocalDate payload);
    }


    final ServiceDefinition nestedServiceDefinition = ServiceDefinition.simple(NestedApi.class);

    @Test
    public void can_call_with_injected() {
        //Given
        LocalDate payloadIn = LocalDate.now();
        LocalDate payloadOut = LocalDate.now().plusYears(100);


        ServiceConfig nestedServiceConfig = ServiceConfig.builder()
                .serviceDefinition(nestedServiceDefinition)
                .bind(NestedApiImpl.class, NestedApi.class)
                .addon(ExceptionMapperAddon.builder().build())
                .build();

        TestServiceRunner.oneShot(nestedServiceConfig, (nestedClientConfig, nestedUri) -> {
            ServiceConfig serviceConfig = ServiceConfig.builder()
                    .serviceDefinition(serviceDefinition)
                    .bind(ApiImpl.class, Api.class)
                    .addon(ExceptionMapperAddon.builder().build())
                    .addon(JerseyClientAddon.builder()
                            .serviceDefinition(nestedServiceDefinition)
                            .clientConfigBase(nestedClientConfig)
                            .usertoken(true)
                            .uri(nestedUri)
                            .build()
                    )
                    .build();
            return TestServiceRunner.oneShot(serviceConfig, (clientConfig, uri) -> {


                //when
                Client client = ClientGenerator.builder()
                        .clientConfigBase(clientConfig)
                        .exceptionMapping(true)
                        .jsonConfig(serviceDefinition.getJsonConfig())
                        .build().generate();
                Api apiEple = StubGenerator.builder()
                        .client(client)
                        .userToken("eple")
                        .uri(uri)
                        .build()
                        .generateClient(Api.class);
                LocalDate actualWithUpdate = apiEple.call(payloadIn);

                Api apiBanan = StubGenerator.builder()
                        .client(client)
                        .userToken("banan")
                        .uri(uri)
                        .build()
                        .generateClient(Api.class);
                LocalDate actualNoUpdate = apiBanan.call(payloadIn);

                //then
                Assert.assertEquals(actualNoUpdate, payloadIn);
                Assert.assertEquals(actualWithUpdate, payloadOut);
                return "";

            });
        });
    }


    final ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);

}

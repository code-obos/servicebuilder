package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.ExceptionMapperAddon;
import no.obos.util.servicebuilder.JerseyClientAddon;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceDefinition;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
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
        NestedApi nestedApi;

        @Override
        public LocalDate call(LocalDate payload) {
            return (nestedApi.shouldUpdate(payload)) ? payload.plusYears(100) : payload;
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
    public void can_call() {
        //Given
        LocalDate payloadIn = LocalDate.now();
        LocalDate payloadOut = LocalDate.now().plusYears(100);


        final ResourceConfig nestedResourceConfig = new JerseyConfig(nestedServiceDefinition)
                .addBinder(binder -> binder.bind(NestedApiImpl.class).to(NestedApi.class))
                .with(ExceptionMapperAddon.defaults())
                .getResourceConfig();


        LocalDate actual = EmbeddedJerseyServer.run(nestedResourceConfig, (nestedClientConfig, nestedUri) -> {
            final ResourceConfig resourceConfig = new JerseyConfig(serviceDefinition)
                    .addBinder(binder -> binder.bind(ApiImpl.class).to(Api.class))
                    .with(ExceptionMapperAddon.defaults())
                    .with(JerseyClientAddon.configure(nestedServiceDefinition, cfg -> cfg
                            .clientConfigBase(nestedClientConfig)
                            .uri(nestedUri)
                    ))
                    .getResourceConfig();
            return EmbeddedJerseyServer.run(resourceConfig, (clientConfig, uri) -> {


                //when
                Client client = ClientGenerator.builder()
                        .clientConfigBase(clientConfig)
                        .exceptionMapping(true)
                        .jsonConfig(serviceDefinition.getJsonConfig())
                        .build().generate();
                Api api = StubGenerator.builder()
                        .client(client)
                        .userToken("eple")
                        .uri(uri)
                        .build()
                        .generateClient(Api.class);
                return api.call(payloadIn);

            });
        });
        //then
        Assert.assertEquals(actual, payloadOut);
    }


    final ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);

}

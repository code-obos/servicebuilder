package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.EmbeddedJerseyServer;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceDefinition;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDate;

public class StubGeneratorTest {



    @Path("service") public interface Api {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        LocalDate call(LocalDate payload);
    }


    static class ApiImpl implements Api {
        @Context
        HttpHeaders headers;

        @Override
        public LocalDate call(LocalDate payload) {
            if ("banan".equals(headers.getHeaderString(Constants.USERTOKENID_HEADER))) {
                return payload.plusYears(1);
            } else {
                return payload;
            }
        }
    }

    @Test
    public void can_call() {
        //Given
        LocalDate payloadIn = LocalDate.now();
        LocalDate payloadOut = LocalDate.now().plusYears(1);

        LocalDate actual = EmbeddedJerseyServer.run(resourceConfig, (clientConfig, uri) -> {

            //when
            Client client = ClientGenerator.builder()
                    .clientConfigBase(clientConfig)
                    .exceptionMapping(true)
                    .jsonConfig(serviceDefinition.getJsonConfig())
                    .build().generate();
            Api api = StubGenerator.builder()
                    .client(client)
                    .userToken("banan")
                    .uri(uri)
                    .build()
                    .generateClient(Api.class);
            return api.call(payloadIn);

        });
        //then
        Assert.assertEquals(actual, payloadOut);
    }


    final ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);

    final ResourceConfig resourceConfig = new JerseyConfig(serviceDefinition)
            .addBinder(binder -> binder.bind(ApiImpl.class).to(Api.class))
            .getResourceConfig();

}

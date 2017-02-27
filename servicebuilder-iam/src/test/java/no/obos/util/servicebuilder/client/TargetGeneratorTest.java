package no.obos.util.servicebuilder.client;

import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.EmbeddedJerseyServer;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Assert;
import org.junit.Test;

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

public class TargetGeneratorTest {



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
            String usertoken = headers.getHeaderString(Constants.USERTOKENID_HEADER);
            if ("banan".equals(usertoken)) {
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
        LocalDate payloadAfterUpdate = LocalDate.now().plusYears(1);

        EmbeddedJerseyServer.run(resourceConfig, (clientConfig, uri) -> {

            //when
            Client client = ClientGenerator.defaults(serviceDefinition)
                    .withClientConfigBase(clientConfig)
                    .generate();

            WebTarget targetShouldUpdate = TargetGenerator.defaults(client, uri)
                    .plusHeader(Constants.USERTOKENID_HEADER,"banan")
                    .generate();
            LocalDate actualShouldUpdate = targetShouldUpdate
                    .path("service")
                    .request()
                    .post(Entity.entity(payloadIn, MediaType.APPLICATION_JSON_TYPE))
                    .readEntity(LocalDate.class);

            WebTarget targetNoUpdate = TargetGenerator.defaults(client, uri)
                    .plusHeader(Constants.USERTOKENID_HEADER, "eple")
                    .generate();
            LocalDate actualNoUpdate = targetNoUpdate
                    .path("service")
                    .request()
                    .post(Entity.entity(payloadIn, MediaType.APPLICATION_JSON_TYPE))
                    .readEntity(LocalDate.class);

            //then
            Assert.assertEquals(actualShouldUpdate, payloadAfterUpdate);
            Assert.assertEquals(actualNoUpdate, payloadIn);
            return "";
        });
    }


    final ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);

    final ResourceConfig resourceConfig = new JerseyConfig(serviceDefinition)
            .addBinder(binder -> binder.bind(ApiImpl.class).to(Api.class))
            .getResourceConfig();

}

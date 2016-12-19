package no.obos.util.servicebuilder.client;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceClientAddon;
import no.obos.util.servicebuilder.ServiceDefinition;
import no.obos.util.servicebuilder.clientGenerator.ClientGenerator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientConfiguratorTest extends JerseyTest {

    @Mock
    Resource resource = mock(Resource.class);


    @Test
    public void can_call() {
        //Given
        Payload payloadIn = new Payload(LocalDate.now());
        Payload payloadOut = new Payload(LocalDate.now().plusYears(100));
        when(resource.call(payloadIn)).thenReturn(payloadOut);

        //when
        Resource client = ClientGenerator.createClient(Resource.class, target());
        Payload actual = client.call(payloadIn);

        //then
        Assert.assertEquals(actual, payloadOut);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Payload {
        LocalDate date;
    }


    @Path("service") public interface Resource {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        Payload call(Payload payload);
    }


    final static ServiceDefinition serviceDefinition = ServiceDefinition.simple("testservice", Resource.class);

    public Application configure() {
        ResourceConfig resourceConfig = new JerseyConfig(serviceDefinition)
                .addBinder(binder -> binder.bind(resource).to(Resource.class))
                .getResourceConfig();
        return resourceConfig;
    }

    @Override
    protected void configureClient(final ClientConfig config) {
        ServiceClientAddon.Configuration build = ServiceClientAddon.defaultConfiguration(serviceDefinition).build();
        ClientGenerator.addConfig(config, build);
    }

}

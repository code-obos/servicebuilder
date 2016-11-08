package no.obos.util.servicebuilder.client;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceDefinition;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.time.LocalDate;
import java.util.List;

public class ClientConfiguratorTest extends JerseyTest {

    @Mock
    Resource resource;


    @Test
    @Ignore
    public void can_call() {
        //Given
    }


    @Getter
    @Setter
    static class Payload {
        LocalDate date;
    }


    @Path("service") interface Resource {
        @POST
        Payload call(Payload payload);
    }


    public Application configure() {
        ServiceDefinition serviceDefinition = new ServiceDefinition() {
            public String getName() {
                return "testservice";
            }

            public List<Class> getResources() {
                return Lists.newArrayList(Resource.class);
            }

        };
        return new JerseyConfig(serviceDefinition)
                .addBinder(binder -> binder.bind(resource).to(Resource.class))
                .getResourceConfig();
    }


}

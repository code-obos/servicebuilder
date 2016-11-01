package no.obos.util.servicebuilder.client;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.ServiceDefinition;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public class TestService implements ServiceDefinition {

    @Path("service")
    interface Resource {
        @POST
        ClientConfiguratorTest.Payload call(ClientConfiguratorTest.Payload payload);
    }

    @Path("service2")
    interface Resource2 {
        @POST
        ClientConfiguratorTest.Payload call2(ClientConfiguratorTest.Payload payload);
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public List<Class> getResources() {
        return Lists.newArrayList(Resource.class, Resource2.class);
    }
}

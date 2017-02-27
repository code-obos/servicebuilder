package no.obos.util.servicebuilder.client;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.model.ServiceDefinition;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

public class TestService implements ServiceDefinition {

    public static class Payload {

    }


    @Path("service") interface Resource {
        @POST
        Payload call(Payload payload);
    }


    @Path("service2") interface Resource2 {
        @POST
        Payload call2(Payload payload);
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

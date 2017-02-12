package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.time.LocalDate;
import java.util.List;

public class TestService implements ServiceDefinition {

    public static final String PATH = "path";

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Payload {
        String string;
        LocalDate date;
    }


    @Api
    public @Path(PATH) interface Resource {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        Payload post(Payload payload);
        @GET
        @Produces("application/json")
        Payload get();
    }

    public static class Impl implements Resource {
        @Override
        public Payload post(Payload payload) {
            return new Payload(payload.string + "1", payload.date.plusYears(1));
        }

        @Override
        public Payload get() {
            return new Payload("string", LocalDate.now());
        }
    }



    @Override
    public String getName() {
        return "test";
    }

    @Override
    public List<Class> getResources() {
        return Lists.newArrayList(Resource.class);
    }

    public final static TestService instance = new TestService();


    public static ServiceConfig.ServiceConfigBuilder addToConfig(ServiceConfig.ServiceConfigBuilder config) {
        return config.build().toBuilder()
                .serviceDefinition(TestService.instance)
                .bind(TestService.Impl.class, TestService.Resource.class);
    }

}

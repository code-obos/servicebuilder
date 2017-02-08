package no.obos.util.servicebuilder.experimental;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.obos.util.servicebuilder.ServiceDefinition;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

public class Main2 {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Stuff {
        String name;
        int number;
    }


    @io.swagger.annotations.Api
    @Path("service") public interface Api {
        @POST
        @Produces("application/json")
        @Consumes("application/json")
        Stuff processStuff(Stuff payload);
        @GET
        @Produces("application/json")
        Stuff getStuff();

    }


    public static class ApiImpl implements Api {
        @Override
        public Stuff processStuff(Stuff payload) {
            return new Stuff(payload.getName(), Integer.valueOf(payload.getName()));
        }

        @Override
        public Stuff getStuff() {
            return new Stuff("Bjarte", 23);
        }
    }


    static ServiceDefinition serviceDefinition = ServiceDefinition.simple(Api.class);
    static ServiceConfig serviceConfig = ServiceConfig.builder()
            .serviceDefinition(serviceDefinition)
            .addon(SwaggerAddon.builder().build())
            .addon(CorsFilterAddon.builder().build())
            .bind(ApiImpl.class,Api.class)
            .build();

    public static void main(String[] args) {
        new ServiceRunner(serviceConfig).start();//.join();
    }

}

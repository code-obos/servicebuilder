package no.obos.util.servicebuilder;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.*;
import no.obos.util.servicebuilder.TestService.Payload;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.Version;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestServiceFull implements ServiceDefinition {

    public static final String PATH = "full";

    public static final String NAME = "test_service_full";

    @Getter
    final Version version = new Version(1, 0, 0);


    @Builder(toBuilder = true)
    @EqualsAndHashCode
    @ToString
    public static class Call {
        @Singular
        public final Map<String, String> headers;
        @Singular
        public final Map<String, String> queryParams;
        public final Payload payload;
        public final String method;
    }


    @Api
    public @Path(PATH)
    interface ResourceFull {
        @GET
        @Produces("application/json")
        boolean get();

        @POST
        @Produces("application/json")
        boolean post(Payload payload);

        @PUT
        @Produces("application/json")
        boolean put(Payload payload);

        @DELETE
        @Produces("application/json")
        boolean delete(Payload payload);

        @GET
        @Path(PATH)
        @Produces("application/json")
        boolean getExplicitContext(@HeaderParam("header1") String header1, @HeaderParam("header2") int header2, @QueryParam("qp1") String qp1, @QueryParam("qp2") int qp2);
    }


    public interface Controller {
        boolean isCallValid(Call context);
    }


    public static class ImplFull implements ResourceFull {
        final HttpHeaders headers;

        final UriInfo uriInfo;

        final Controller controller;

        @Inject
        public ImplFull(@Context HttpHeaders headers, @Context UriInfo uriInfo, Controller controller) {
            this.headers = headers;
            this.uriInfo = uriInfo;
            this.controller = controller;
        }

        @Override
        public boolean get() {
            Call call = getCall().build();
            return controller.isCallValid(call);
        }

        @Override
        public boolean post(Payload payload) {
            Call call = getCall()
                    .payload(payload)
                    .build();
            return controller.isCallValid(call);
        }

        @Override
        public boolean put(Payload payload) {
            Call call = getCall()
                    .payload(payload)
                    .build();
            return controller.isCallValid(call);
        }

        @Override
        public boolean delete(Payload payload) {
            Call call = getCall()
                    .payload(payload)
                    .build();
            return controller.isCallValid(call);
        }

        @Override
        public boolean getExplicitContext(String header1, int header2, String qp1, int qp2) {
            Call call = getCall()
                    .header("header1", header1)
                    .header("header2", String.valueOf(header2))
                    .queryParam("qp1", qp1)
                    .queryParam("qp2", String.valueOf(qp2))
                    .build();
            return controller.isCallValid(call);
        }

        private Call.CallBuilder getCall() {
            MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
            MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
            return Call.builder()
                    .headers(requestHeaders.keySet().stream().collect(Collectors.toMap(Function.identity(), requestHeaders::getFirst)))
                    .queryParams(queryParameters.keySet().stream().collect(Collectors.toMap(Function.identity(), queryParameters::getFirst)))
                    ;


        }
    }


    public static Payload defaultPayload = new Payload("string", LocalDate.now());


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<Class> getResources() {
        return Lists.newArrayList(ResourceFull.class);
    }

    public final static TestServiceFull instance = new TestServiceFull();
    public final static ServiceConfig config = ServiceConfig.defaults(instance)
            .bind(ImplFull.class, ResourceFull.class);
}

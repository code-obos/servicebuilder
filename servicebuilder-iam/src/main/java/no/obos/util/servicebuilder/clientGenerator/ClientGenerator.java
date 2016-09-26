package no.obos.util.servicebuilder.clientGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import no.obos.util.model.ProblemResponse;
import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.ServiceClientAddon;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class ClientGenerator {
    public static <T> T createClient(Class<T> resourceClass, ServiceClientAddon.Configuration configuration) {
        ObjectMapper mapper = configuration.serviceDefinition.getJsonConfig().get();
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);

        Configuration resourceConfig = new ResourceConfig()
                .register(ErrorResponseFilter.class)
                .register(UserTokenHeaderFilter.class)
                .register(JacksonFeature.class)
                .register(provider)
                .register(new AbstractBinder() {

                    @Override
                    protected void configure() {
                        bind(mapper).to(ObjectMapper.class);
                    }
                });



        Client client = ClientBuilder.newClient(resourceConfig);
        WebTarget webTarget = client.target(configuration.url);

        return WebResourceFactory.newResource(resourceClass, webTarget);
    }


    private static class UserTokenHeaderFilter implements ClientRequestFilter {

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.getHeaders().add(Constants.USERTOKENID_HEADER, "bf102f6b-3deb-4656-be17-bbe7b75af19f");
        }
    }


    private static class ErrorResponseFilter implements ClientResponseFilter {

        @Inject
        ObjectMapper mapper = new ObjectMapper();

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
                throws IOException
        {
            // for non-200 response, deal with the custom error messages
            if (! Response.Status.Family.SUCCESSFUL.equals(responseContext.getStatusInfo().getFamily())) {
                if (responseContext.hasEntity()) {
                    // get the "real" error message

                    ProblemResponse error = mapper.readValue(responseContext.getEntityStream(), ProblemResponse.class);
                    throw new ThirdPartyException(error, responseContext.getStatus());
                }
            }
        }
    }


    private static class ThirdPartyException extends WebApplicationException {
        final ProblemResponse problemResponse;

        private ThirdPartyException(ProblemResponse problemResponse, int status) {
            super(status);
            this.problemResponse = problemResponse;
        }

    }
}

package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.model.ProblemResponse;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class ClientErrorResponseFilter implements ClientResponseFilter {
    final ObjectMapper mapper;

    @Inject
    public ClientErrorResponseFilter(ObjectMapper mapper) {this.mapper = mapper;}

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException
    {
        // for non-200 response, deal with the custom error messages
        if (! Response.Status.Family.SUCCESSFUL.equals(responseContext.getStatusInfo().getFamily())) {
            if (responseContext.hasEntity()) {
                // setUp the "real" error message

                ProblemResponse error = mapper.readValue(responseContext.getEntityStream(), ProblemResponse.class);
                throw new RuntimeException("Error from server. Status " + responseContext.getStatus() + ": " + error );
            } else {
                throw new RuntimeException("Error from server. Status " + responseContext.getStatus() + ": Unknown" );
            }
        }
    }
}

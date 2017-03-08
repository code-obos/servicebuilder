package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.exception.ExternalResourceNotFoundException;
import no.obos.util.servicebuilder.model.ProblemResponse;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.exception.ExternalResourceException.MetaData;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Priority(Priorities.AUTHENTICATION)
public class ClientErrorResponseFilter implements ClientResponseFilter {
    final ObjectMapper mapper;
    final ServiceDefinition serviceDefinition;

    @Inject
    public ClientErrorResponseFilter(
            ObjectMapper mapper,
            @Named(ClientGenerator.SERVICE_DEFINITION_INJECTION) ServiceDefinition serviceDefinition)
    {
        this.mapper = mapper;
        this.serviceDefinition = serviceDefinition;
    }



    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException
    {
        // for non-200 response, deal with the custom error messages
        if (! Response.Status.Family.SUCCESSFUL.equals(responseContext.getStatusInfo().getFamily())) {
            MetaData.MetaDataBuilder metaData = MetaData.builder()
                    .httpStatus(responseContext.getStatus())
                    .gotAnswer(true)
                    .targetUrl(requestContext.getUri().toString())
                    .targetName(serviceDefinition.getName())
                    .context("response_headers", responseContext.getHeaders().toString());
            if (responseContext.hasEntity()) {
                // setUp the "real" error message
                String entity;
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(responseContext.getEntityStream()))) {
                    entity = buffer.lines().collect(Collectors.joining("\n"));
                }
                try {
                    ProblemResponse error = mapper.readValue(entity, ProblemResponse.class);
                    metaData.nestedProblemResponce(error)
                            .incidentReferenceId(error.incidentReferenceId);
                } catch (JsonParseException | JsonMappingException e) {
                    //ignore
                }
            }
            if(Response.Status.NOT_FOUND.getStatusCode() == responseContext.getStatus()) {
                throw new ExternalResourceNotFoundException(metaData.build());
            }
            throw new ExternalResourceException(metaData.build());
        }
    }
}

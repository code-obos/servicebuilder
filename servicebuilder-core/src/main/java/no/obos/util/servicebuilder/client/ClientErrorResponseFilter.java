package no.obos.util.servicebuilder.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.exception.ExternalResourceException.HttpResponseMetaData;
import no.obos.util.servicebuilder.exception.ExternalResourceException.MetaData;
import no.obos.util.servicebuilder.exception.ExternalResourceNotFoundException;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.Version;
import no.obos.util.servicebuilder.util.FormatUtil;
import org.jvnet.hk2.annotations.Optional;

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
import java.util.Map;
import java.util.stream.Collectors;

@Priority(Priorities.AUTHENTICATION)
public class ClientErrorResponseFilter implements ClientResponseFilter {
    final ObjectMapper mapper;
    final ServiceDefinition serviceDefinition;

    @Inject
    public ClientErrorResponseFilter(
            ObjectMapper mapper,
            @Named(ClientGenerator.SERVICE_DEFINITION_INJECTION) @Optional ServiceDefinition serviceDefinition)
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
            String targetName = serviceDefinition == null ? null : serviceDefinition.getName();
            Version targetVersion = serviceDefinition == null ? null : serviceDefinition.getVersion();
            MetaData metaData = MetaData.builder()
                    .httpRequestMetaData(getRequestMetaData(requestContext))
                    .httpResponseMetaData(getResponseMetaData(responseContext))
                    .gotAnswer(true)
                    .targetName(targetName)
                    .targetVersion(targetVersion)
                    .build();
            if (Response.Status.NOT_FOUND.getStatusCode() == responseContext.getStatus()) {
                throw new ExternalResourceNotFoundException(metaData);
            }
            throw new ExternalResourceException(metaData);
        }
    }

    private HttpResponseMetaData getResponseMetaData(ClientResponseContext responseContext) throws IOException {
        Map<String, String> headers = FormatUtil.MultiMapAsStringMap(responseContext.getHeaders());
        HttpResponseMetaData.HttpResponseMetaDataBuilder builder = HttpResponseMetaData.builder()
                .status(responseContext.getStatus())
                .headers(headers);

        if (responseContext.hasEntity()) {
            String body;
            // setUp the "real" error message
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(responseContext.getEntityStream(), "UTF-8"))) {
                body = buffer.lines().collect(Collectors.joining("\n"));
            }
            try {
                HttpProblem problem = mapper.readValue(body, HttpProblem.class);
                if (problemWasParsed(problem)) {
                    builder.httpProblem(problem)
                            .incidentReferenceId(problem.incidentReferenceId);
                }
            } catch (JsonParseException | JsonMappingException e) {
                //ignore
            }

            if (builder.build().httpProblem == null) {
                builder.response(body);
            }
        }

        return builder.build();
    }

    private boolean problemWasParsed(HttpProblem problem) {
        return problem != null
                && problem.incidentReferenceId != null;
    }

    private ExternalResourceException.HttpRequestMetaData getRequestMetaData(ClientRequestContext requestContext) {
        Map<String, String> headers = FormatUtil.MultiMapAsStringMap(requestContext.getStringHeaders());
        return ExternalResourceException.HttpRequestMetaData.builder()
                .url(requestContext.getUri().toString())
                .headers(headers)
                .build();
    }
}

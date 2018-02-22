package no.obos.util.servicebuilder.applicationtoken;

import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.access.TokenCheckResult;
import no.obos.util.model.ProblemResponse;
import no.obos.util.servicebuilder.ApplicationTokenFilterAddon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.UUID;

public class ApplicationTokenFilter implements ContainerRequestFilter {
    Logger log = LoggerFactory.getLogger(ApplicationTokenFilter.class);

    public static final String APPTOKENID_HEADER = "X-OBOS-APPTOKENID";

    @Inject
    private ApplicationTokenAccessValidator applicationTokenAccessValidator;

    @Inject
    ApplicationTokenFilterAddon.Configuration configuration;

    @Context
    private UriInfo uriInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.debug("Request headers: {}", requestContext.getHeaders());

        if (allwaysAccept(requestContext)) {
            return;
        }

        String apptokenid = requestContext.getHeaderString(APPTOKENID_HEADER);
        if (apptokenid == null || apptokenid.trim().isEmpty()) {
            String feilref = UUID.randomUUID().toString();
            String msg = "Header (" + APPTOKENID_HEADER + ") for application token ID is missing";
            log.warn(msg);
            ProblemResponse problemResponse = new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), feilref);
            Response response = Response.status(Status.UNAUTHORIZED).entity(problemResponse).build();
            requestContext.abortWith(response);
        } else {
            TokenCheckResult result = applicationTokenAccessValidator.checkApplicationTokenId(apptokenid);
            if (result != TokenCheckResult.AUTHORIZED) {
                String feilref = UUID.randomUUID().toString();
                String msg = "Apptokenid '" + apptokenid + "' is " + result;
                log.warn(msg);
                ProblemResponse problemResponse = new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), feilref);
                Response response = Response.status(Status.UNAUTHORIZED).entity(problemResponse).build();
                requestContext.abortWith(response);
            }
        }
    }

    public boolean allwaysAccept(ContainerRequestContext requestContext) {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();

        return aboslutePath.contains("swagger") ||
                "OPTIONS".equals(requestMethod) ||
                configuration.fasttrackFilter.test(requestContext);
    }
}


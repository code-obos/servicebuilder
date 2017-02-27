package no.obos.util.servicebuilder.applicationtoken;

import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.access.TokenCheckResult;
import no.obos.util.model.ProblemResponse;
import no.obos.util.servicebuilder.addon.ApplicationTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.AppTokenRequired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.UUID;

public class ApplicationTokenFilter implements ContainerRequestFilter {
    Logger log = LoggerFactory.getLogger(ApplicationTokenFilter.class);

    public static final String APPTOKENID_HEADER = "X-OBOS-APPTOKENID";

    final private ApplicationTokenAccessValidator applicationTokenAccessValidator;
    final ApplicationTokenFilterAddon configuration;

    final private ResourceInfo resourceInfo;

    @Inject
    public ApplicationTokenFilter(ApplicationTokenAccessValidator applicationTokenAccessValidator, ApplicationTokenFilterAddon configuration, ResourceInfo resourceInfo) {
        this.applicationTokenAccessValidator = applicationTokenAccessValidator;
        this.configuration = configuration;
        this.resourceInfo = resourceInfo;
    }

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

        AppTokenRequired methodAnnotation = resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getAnnotation(AppTokenRequired.class)
                : null;
        AppTokenRequired classAnnotation = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(AppTokenRequired.class)
                : null;
        boolean annotationFasttrack = false;
        if (methodAnnotation != null) {
            annotationFasttrack = ! methodAnnotation.value();
        } else if (classAnnotation != null) {
            annotationFasttrack = ! classAnnotation.value();
        }

        return aboslutePath.contains("swagger") ||
                "OPTIONS".equals(requestMethod) ||
                configuration.fasttrackFilter.test(requestContext) ||
                annotationFasttrack;
    }
}


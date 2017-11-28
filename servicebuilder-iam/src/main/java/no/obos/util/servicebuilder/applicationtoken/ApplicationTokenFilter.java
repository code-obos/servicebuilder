package no.obos.util.servicebuilder.applicationtoken;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.iam.access.ApplicationTokenAccessValidator;
import no.obos.iam.access.TokenCheckResult;
import no.obos.iam.tokenservice.ApplicationToken;
import no.obos.iam.tokenservice.TokenServiceClient;
import no.obos.util.model.ProblemResponse;
import no.obos.util.servicebuilder.addon.ApplicationTokenFilterAddon;
import no.obos.util.servicebuilder.annotations.AppIdWhiteList;
import no.obos.util.servicebuilder.annotations.AppTokenRequired;
import no.obos.util.servicebuilder.util.AnnotationUtil;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Priority(Priorities.AUTHENTICATION)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationTokenFilter implements ContainerRequestFilter {

    public static final String APPTOKENID_HEADER = "X-OBOS-APPTOKENID";

    private final ApplicationTokenAccessValidator applicationTokenAccessValidator;
    final ApplicationTokenFilterAddon configuration;
    private final TokenServiceClient tokenServiceClient;

    final private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (alwaysAccept(requestContext)) {
            return;
        }

        String apptokenid = requestContext.getHeaderString(APPTOKENID_HEADER);
        if (apptokenid == null || apptokenid.trim().isEmpty()) {
            handleErrorNoAppToken(requestContext);
        } else {
            ApplicationToken token = tokenServiceClient.getApptokenById(apptokenid);
            TokenCheckResult result = applicationTokenAccessValidator.checkApplicationToken(token);
            if (result != TokenCheckResult.AUTHORIZED) {
                handleErrorUnauthorized(requestContext, apptokenid, result);
            } else if (! isAllowedToCallEndpoint(token)) {
                handleErrorUnauthorizedForEndpoint(requestContext, apptokenid, result);
            }
        }
    }

    private boolean isAllowedToCallEndpoint(ApplicationToken token) {
        AppIdWhiteList annotation = AnnotationUtil.getAnnotation(AppIdWhiteList.class, resourceInfo.getResourceMethod());
        int app = Integer.parseInt(token.getApplicationId());

        return Optional.ofNullable(annotation)
                .map(AppIdWhiteList::value)
                .map(allowedIds ->
                        Arrays.stream(allowedIds)
                                .anyMatch(it -> app == it)
                )
                .orElse(true);
    }

    private void handleErrorUnauthorizedForEndpoint(ContainerRequestContext requestContext, String apptokenid, TokenCheckResult result) {
        String feilref = UUID.randomUUID().toString();
        String msg = "Apptokenid '" + apptokenid + "' is UNAUTHORIZED for this endpoint";
        log.warn(msg);
        ProblemResponse problemResponse = new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), feilref);
        Response response = Response.status(Status.UNAUTHORIZED).entity(problemResponse).build();
        requestContext.abortWith(response);
    }

    private void handleErrorUnauthorized(ContainerRequestContext requestContext, String apptokenid, TokenCheckResult result) {
        String feilref = UUID.randomUUID().toString();
        String msg = "Apptokenid '" + apptokenid + "' is " + result;
        log.warn(msg);
        ProblemResponse problemResponse = new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), feilref);
        Response response = Response.status(Status.UNAUTHORIZED).entity(problemResponse).build();
        requestContext.abortWith(response);
    }

    private void handleErrorNoAppToken(ContainerRequestContext requestContext) {
        String feilref = UUID.randomUUID().toString();
        String msg = "Header (" + APPTOKENID_HEADER + ") for application token ID is missing";
        log.warn(msg);
        ProblemResponse problemResponse = new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), feilref);
        Response response = Response.status(Status.UNAUTHORIZED).entity(problemResponse).build();
        requestContext.abortWith(response);
    }

    public boolean alwaysAccept(ContainerRequestContext requestContext) {
        String aboslutePath = requestContext.getUriInfo().getAbsolutePath().toString();
        String requestMethod = requestContext.getMethod();

        AppTokenRequired methodAnnotation = resourceInfo.getResourceMethod() != null
                ? resourceInfo.getResourceMethod().getAnnotation(AppTokenRequired.class)
                : null;
        AppTokenRequired classAnnotation = resourceInfo.getResourceClass() != null
                ? resourceInfo.getResourceClass().getAnnotation(AppTokenRequired.class)
                : null;
        boolean annotationFasttrack = ! configuration.requireAppTokenByDefault;
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


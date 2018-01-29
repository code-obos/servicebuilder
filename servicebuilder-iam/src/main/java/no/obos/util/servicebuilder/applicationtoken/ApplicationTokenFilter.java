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
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.util.AnnotationUtil;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static no.obos.iam.access.TokenCheckResult.AUTHORIZED;
import static no.obos.iam.access.TokenCheckResult.UNAUTHORIZED;

@Priority(Priorities.AUTHENTICATION)
@Slf4j
@AllArgsConstructor(onConstructor = @__({@Inject}))
public class ApplicationTokenFilter implements ContainerRequestFilter {

    @Deprecated
    public static final String APPTOKENID_HEADER = Constants.APPTOKENID_HEADER;

    private final ApplicationTokenAccessValidator applicationTokenAccessValidator;
    final ApplicationTokenFilterAddon configuration;
    private final TokenServiceClient tokenServiceClient;

    final private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (alwaysAccept(requestContext)) {
            return;
        }

        String apptokenid = requestContext.getHeaderString(Constants.APPTOKENID_HEADER);

        if (apptokenid == null || apptokenid.trim().isEmpty()) {
            handleErrorNoAppToken(requestContext);
        } else {
            TokenCheckResult result = applicationTokenAccessValidator.checkApplicationTokenId(apptokenid);

            if (result != AUTHORIZED && (result != UNAUTHORIZED || isNotInWhiteList(getApplicationId(apptokenid)))) {
                handleErrorUnauthorized(requestContext, apptokenid, result);
            } else if (isNotAllowedToCallEndpointIfOtherwiseAuthorized(getApplicationId(apptokenid))) {
                handleErrorUnauthorized(requestContext, apptokenid, UNAUTHORIZED);
            }
        }
    }

    private Integer getApplicationId(String apptokenid) {
        return Optional.ofNullable(tokenServiceClient.getApptokenById(apptokenid))
                .map(ApplicationToken::getApplicationId)
                .map(Integer::parseInt)
                .orElse(null);
    }

    private boolean isNotAllowedToCallEndpointIfOtherwiseAuthorized(Integer applicationId) {
        return whiteListIsPresentAndExclusive() && isNotInWhiteList(applicationId);
    }

    private Boolean whiteListIsPresentAndExclusive() {
        return Optional.ofNullable(getWhiteListAnnotation())
                .map(AppIdWhiteList::exclusive)
                .orElse(false);
    }

    private boolean isNotInWhiteList(Integer applicationId) {
        return applicationId == null
                || Optional.ofNullable(getWhiteListAnnotation())
                .map(AppIdWhiteList::value)
                .map(Arrays::stream)
                .map(whiteListAppIds ->
                        whiteListAppIds.noneMatch(whiteListedAppId -> applicationId == whiteListedAppId))
                .orElse(true);
    }

    private AppIdWhiteList getWhiteListAnnotation() {
        return AnnotationUtil.getAnnotation(AppIdWhiteList.class, resourceInfo.getResourceMethod());
    }

    private void handleErrorUnauthorized(ContainerRequestContext requestContext, String apptokenid, TokenCheckResult result) {
        handleUnauthorized(requestContext, "Apptokenid '" + apptokenid + "' is " + result);
    }

    private void handleErrorNoAppToken(ContainerRequestContext requestContext) {
        handleUnauthorized(requestContext, "Header (" + Constants.APPTOKENID_HEADER + ") for application token ID is missing");
    }

    private static void handleUnauthorized(ContainerRequestContext requestContext, String msg) {
        log.warn(msg);
        requestContext.abortWith(Response
                .status(Status.UNAUTHORIZED)
                .entity(new ProblemResponse("ERROR", msg, Status.UNAUTHORIZED.getStatusCode(), UUID.randomUUID().toString()))
                .build());
    }

    public boolean alwaysAccept(ContainerRequestContext requestContext) {
        String absolutePath = requestContext.getUriInfo().getAbsolutePath().toString();
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

        return absolutePath.contains("swagger") ||
                "OPTIONS".equals(requestMethod) ||
                configuration.fasttrackFilter.test(requestContext) ||
                annotationFasttrack;
    }
}


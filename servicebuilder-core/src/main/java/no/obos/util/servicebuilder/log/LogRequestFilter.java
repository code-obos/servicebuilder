package no.obos.util.servicebuilder.log;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.LogRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class LogRequestFilter implements ContainerRequestFilter {
    @Context
    ResourceInfo resourceInfo;

    @Inject
    RestLogger restLogger;


    @Context
    HttpServletRequest servletRequest;


    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        if (restLogger == null) {
            log.error("Missing log configuration. Please bind no.obos.util.restlogger.RestLogger.class");
            return;
        }

        Class<?> handlingClass = resourceInfo.getResourceClass();
        Method handlingMethod = resourceInfo.getResourceMethod();
        LogParams logParams = restLogger.LogParamsForCall(handlingClass, handlingMethod);

        if (! logParams.enableLogging) {
            return;
        }
        LogRequest.LogRequestBuilder logRequest = LogRequest.builder();

        logRequest.handlingClass(handlingClass);
        logRequest.handlingMethod(handlingMethod);

        if (logParams.considerHeaders()) {
            logRequest.headers(JerseyLogUtil.logHeaders(request.getHeaders(), logParams));
        }

        if (logParams.logUrl) {
            logRequest.uri(request.getUriInfo().getAbsolutePath());
        }

        if (servletRequest == null) {
            log.warn("No servlet request in restlogger");
        } else {
            if (logParams.logSender) {
                logRequest.remoteAddr(servletRequest.getRemoteAddr());
            }
            if (logParams.logUser) {
                String user = servletRequest.getUserPrincipal() != null
                        ? servletRequest.getUserPrincipal().toString()
                        : null;
                logRequest.user(user);
            }
        }

        if (logParams.logRequestEntity) {
            logRequest.entity(JerseyLogUtil.extractRequestEntity(request));
        }

        restLogger.handleRequest(logRequest.build(), logParams);
    }

}

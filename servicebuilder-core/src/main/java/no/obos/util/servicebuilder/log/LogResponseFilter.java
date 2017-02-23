package no.obos.util.servicebuilder.log;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.LogResponse;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class LogResponseFilter implements ContainerResponseFilter {


    @Context
    ResourceInfo resourceInfo;

    @Inject
    RestLogger restLogger;

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
            throws IOException
    {

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

        LogResponse.LogResponseBuilder logResponse = LogResponse.builder();

        if (logParams.logHeaders) {
            logResponse.headers(JerseyLogUtil.logHeaders(response.getStringHeaders(), logParams));
        }

        if (logParams.logResponseEntity) {
            logResponse.entityClass(response.getEntityClass());
            if (response.hasEntity()) {
                logResponse.entity(response.getEntity());
            }
        }

        restLogger.handleResponse(logResponse.build(), logParams);
    }
}

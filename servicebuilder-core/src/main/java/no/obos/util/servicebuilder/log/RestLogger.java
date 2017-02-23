package no.obos.util.servicebuilder.log;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.LogRequest;
import no.obos.util.servicebuilder.log.model.LogResponse;
import no.obos.util.servicebuilder.log.model.RestLogConfiguration;

import javax.inject.Inject;
import java.lang.reflect.Method;

@Slf4j
public class RestLogger {


    final RestLogConfiguration restLogConfiguration;

    @Inject
    public RestLogger(RestLogConfiguration restLogConfiguration) {this.restLogConfiguration = restLogConfiguration;}

    public LogParams LogParamsForCall(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            log.error("Missing clazz or method info for log call, logging disabled.");
            return LogParams.defaults.toBuilder().enableLogging(false).build();
        }
        LogParams.LogParamsBuilder ret = restLogConfiguration.defaultLogParams.toBuilder();

        if (restLogConfiguration.blacklistClasses.contains(clazz)) {
            ret.enableLogging(false);
        }

        ret.enableLogging(restLogConfiguration.enableDefault);

        if (method.isAnnotationPresent(Log.class)) {
            ret.enableLogging(method.getAnnotation(Log.class).value());
        }

        if (method.isAnnotationPresent(LogRequestEntity.class)) {
            ret.logRequestEntity(method.getAnnotation(LogRequestEntity.class).value());
        }
        if (method.isAnnotationPresent(LogResponseEntity.class)) {
            ret.logResponseEntity(method.getAnnotation(LogResponseEntity.class).value());
        }
        return ret.build();

    }

    public void handleRequest(LogRequest logRequest, LogParams logParams) {
        log.error(logRequest.toString());
    }

    public void handleResponse(LogResponse logResponse, LogParams logParams) {
        log.error(logResponse.toString());
    }
}

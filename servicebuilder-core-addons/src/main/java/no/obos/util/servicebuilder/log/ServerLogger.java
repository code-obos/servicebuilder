package no.obos.util.servicebuilder.log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.annotations.Log;
import no.obos.util.servicebuilder.annotations.LogRequestEntity;
import no.obos.util.servicebuilder.annotations.LogResponseEntity;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.LogRequest;
import no.obos.util.servicebuilder.log.model.LogResponse;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.util.AnnotationUtil;
import no.obos.util.servicebuilder.util.LogUtil;

import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class ServerLogger {

    public final ImmutableList<Predicate<ContainerRequestContext>> fastTrackFilters;
    public final LogParams logParams;


    public LogParams LogParamsForCall(Class<?> clazz, Method method) {
        if (clazz == null || method == null) {
            return logParams;
        }

        LogParams ret = logParams;

        Log enableLogging = AnnotationUtil.getAnnotation(Log.class, method);
        if (enableLogging != null) {
            ret = ret.enableLogging(enableLogging.value());
        }

        LogRequestEntity logRequestEntity = AnnotationUtil.getAnnotation(LogRequestEntity.class, method);
        if (logRequestEntity != null) {
            ret = ret.logRequestPayload(logRequestEntity.value());
        }

        LogResponseEntity logResponseEntity = AnnotationUtil.getAnnotation(LogResponseEntity.class, method);
        if (logResponseEntity != null) {
            ret = ret.logRequestPayload(logResponseEntity.value());
        }
        return ret;

    }

    public void handleRequest(LogRequest logRequest, LogParams logParams) {

        List<String> entries = Lists.newArrayList();
        entries.add(logRequest.uri);
        if (logRequest.clientApplication != null) {
            entries.add("Client: " + logRequest.clientApplication);
        }
        if (logRequest.user != null) {
            entries.add("User: " + logRequest.user);
        }
        if (logParams.logHeaders && logRequest.headers != null) {
            entries.add("Headers: " + getHeaders(logRequest.headers, logParams)
            );
        }
        if (logParams.logResponseEntity && ! Strings.isNullOrEmpty(logRequest.entity)) {
            List<String> lines = Splitter.on('\n').splitToList(logRequest.entity)
                    .stream()
                    .map(String::trim)
                    .collect(Collectors.toList());
            String compactedEntity = Joiner.on(' ').join(lines);
            entries.add("Entity: " + compactedEntity);
        }
        String logString = Joiner.on(", ").join(entries);
        LogUtil.doLog(logString, logParams.logLevel, log);
    }

    public void handleResponse(LogResponse logResponse, LogParams logParams) {
        List<String> entries = Lists.newArrayList();
        entries.add(logResponse.uri);
        entries.add("Status: " + logResponse.status);
        if (logResponse.totalMillis != null) {
            entries.add("Millis: " + logResponse.totalMillis);
        }
        if (logParams.logHeaders && logResponse.headers != null) {
            entries.add("Headers: " + getHeaders(logResponse.headers, logParams)
            );
        }
        if (logParams.logResponseEntity && logResponse.entity != null) {
            if (! (logResponse.entity instanceof HttpProblem)) {
                entries.add("Entity: " + logResponse.entity.toString());
            }
        }
        String logString = Joiner.on(", ").join(entries);
        LogUtil.doLog(logString, logParams.logLevel, log);
    }

    private List<String> getHeaders(Map<String, String> headers, LogParams logParams) {
        return headers.entrySet().stream()
                .filter(entry -> ! logParams.skipHeaders.contains(entry.getKey()))
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toList());
    }


}

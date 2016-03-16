package no.obos.util.servicebuilder.exception;

import no.obos.util.model.ProblemResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

final public class ExceptionUtil {

    public static final String DEFAULT_ERROR_TITLE = "ERROR";

    public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";
    public static final String APPLICATION_PROBLEM_XML = MediaType.APPLICATION_XML;

    public static String lagFeilreferanse() {
        return UUID.randomUUID().toString();
    }

    public static String lagLoggMelding(String msg, String feilreferanse) {
        return msg + ". Feilreferanse: " + feilreferanse;
    }

    public static Response buildDefaultProblemResponse(int status, String msg, String feilreferanse) {
        ProblemResponse problemResponse = new ProblemResponse(DEFAULT_ERROR_TITLE, msg, status, feilreferanse);
        return Response.status(status).type(ExceptionUtil.APPLICATION_PROBLEM_JSON).entity(problemResponse).build();
    }

    public static Response buildDefaultProblemResponse(int status, String msg, String mediaType, String feilreferanse) {
        ProblemResponse problemResponse = new ProblemResponse(DEFAULT_ERROR_TITLE, msg, status, feilreferanse);
        return Response.status(status).type(mediaType).entity(problemResponse).build();
    }

    public static Response buildProblemResponse(int status, String msg, String mediaType, String feilreferanse, String errorTitle) {
        ProblemResponse problemResponse = new ProblemResponse(errorTitle, msg, status, feilreferanse);
        return Response.status(status).type(mediaType).entity(problemResponse).build();
    }

    public static boolean shouldPrintStacktrace(Throwable throwable, Map<Class<?>, Boolean> config) {
        Class<?> clazz = throwable.getClass();
        while (clazz.getSuperclass() != null && ! Throwable.class.equals(clazz.getSuperclass())) {
            if (config.containsKey(clazz)) {
                return config.get(clazz) == Boolean.TRUE;
            }
            clazz = clazz.getSuperclass();
        }
        return true;
    }
}

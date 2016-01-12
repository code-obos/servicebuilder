package no.obos.util.servicebuilder.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);
    private final ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig;

    public JsonProcessingExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        this.shouldLogStacktraceConfig = shouldLogStacktraceConfig;
    }

    @Override
    public Response toResponse(JsonProcessingException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Feil under jsonprosessenring: " + exception.getMessage();

        String loggmelding = ExceptionUtil.lagLoggMelding(msg, feilreferanse);

        if (ExceptionUtil.shouldPrintStacktrace(exception, shouldLogStacktraceConfig)) {
            LOG.error(loggmelding, exception);
        } else {
            LOG.error(loggmelding);
        }
        Response.Status status = Response.Status.BAD_REQUEST;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

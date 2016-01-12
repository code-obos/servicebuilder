package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionMapper.class);
    private final ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig;

    public ValidationExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        this.shouldLogStacktraceConfig = shouldLogStacktraceConfig;
    }

    @Override public Response toResponse(ValidationException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Valideringfeil: " + exception.getMessage();

        String loggMelding = ExceptionUtil.lagLoggMelding(msg, feilreferanse);
        if (ExceptionUtil.shouldPrintStacktrace(exception, shouldLogStacktraceConfig)) {
            LOG.error(loggMelding, exception);
        } else {
            LOG.error(loggMelding);
        }
        Response.Status status = Response.Status.BAD_REQUEST;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

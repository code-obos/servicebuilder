package no.obos.util.servicebuilder.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override public Response toResponse(ValidationException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Valideringfeil: " + exception.getMessage();

        LOG.error(ExceptionUtil.lagLoggMelding(msg, feilreferanse), exception);
        Response.Status status = Response.Status.BAD_REQUEST;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

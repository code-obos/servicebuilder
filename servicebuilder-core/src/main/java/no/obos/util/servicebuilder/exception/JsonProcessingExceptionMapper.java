package no.obos.util.servicebuilder.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override
    public Response toResponse(JsonProcessingException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Feil under jsonprosessenring: " + exception.getMessage();

        LOG.error(ExceptionUtil.lagLoggMelding(msg, feilreferanse), exception);
        Response.Status status = Response.Status.BAD_REQUEST;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

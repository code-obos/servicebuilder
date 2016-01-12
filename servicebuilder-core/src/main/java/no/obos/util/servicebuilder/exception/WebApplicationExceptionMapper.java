package no.obos.util.servicebuilder.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override public Response toResponse(WebApplicationException exception) {
        WebApplicationException webApplicationException = exception;
        int status = webApplicationException.getResponse().getStatus();
        String msg = webApplicationException.getLocalizedMessage();
        String feilreferanse = ExceptionUtil.lagFeilreferanse();

        LOG.warn(ExceptionUtil.lagLoggMelding(msg, feilreferanse), exception);
        return ExceptionUtil.buildDefaultProblemResponse(status, msg, feilreferanse);
    }

}

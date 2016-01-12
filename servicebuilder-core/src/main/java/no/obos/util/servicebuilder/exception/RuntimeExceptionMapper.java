package no.obos.util.servicebuilder.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    @Override
    public Response toResponse(RuntimeException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Det har oppstått en intern feil";

        LOG.error(ExceptionUtil.lagLoggMelding(msg, feilreferanse), exception);
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

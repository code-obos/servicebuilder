package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);
    private final ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig;

    public RuntimeExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        this.shouldLogStacktraceConfig = shouldLogStacktraceConfig;
    }

    @Override
    public Response toResponse(RuntimeException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        String msg = "Det har oppstått en intern feil";

        String loggMelding = ExceptionUtil.lagLoggMelding(msg, feilreferanse);
        if (ExceptionUtil.shouldPrintStacktrace(exception, shouldLogStacktraceConfig)) {
            LOG.error(loggMelding, exception);
        } else {
            LOG.error(loggMelding);
        }
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

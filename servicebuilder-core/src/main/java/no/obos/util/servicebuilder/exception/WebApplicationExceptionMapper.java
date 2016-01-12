package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);
    private final ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig;

    public WebApplicationExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        this.shouldLogStacktraceConfig = shouldLogStacktraceConfig;
    }

    @Override public Response toResponse(WebApplicationException exception) {
        WebApplicationException webApplicationException = exception;
        int status = webApplicationException.getResponse().getStatus();
        String msg = webApplicationException.getLocalizedMessage();
        String feilreferanse = ExceptionUtil.lagFeilreferanse();

        String loggMelding = ExceptionUtil.lagLoggMelding(msg, feilreferanse);

        if (ExceptionUtil.shouldPrintStacktrace(exception, shouldLogStacktraceConfig)) {
            LOG.warn(loggMelding, exception);
        } else {
            LOG.warn(loggMelding);
        }
        return ExceptionUtil.buildDefaultProblemResponse(status, msg, feilreferanse);
    }

}

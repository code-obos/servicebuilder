package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.logging.Logger;

@Slf4j
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    final private ExceptionUtil exceptionUtil;

    @Inject
    public WebApplicationExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        LogLevel logLevel = exception.getResponse().getStatus() >= 500 ? LogLevel.ERROR : LogLevel.WARN;
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(exception.getResponse().getStatus())
                .logLevel(logLevel)
                .logger(log)
        );
    }
}

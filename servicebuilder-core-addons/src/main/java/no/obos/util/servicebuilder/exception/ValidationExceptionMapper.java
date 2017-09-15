package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Slf4j
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    final private ExceptionUtil exceptionUtil;

    @Inject
    public ValidationExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(ValidationException exception) {
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(BAD_REQUEST.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail("Valideringsfeil: " + exception.getLocalizedMessage())
                .logger(log)
        );
    }
}

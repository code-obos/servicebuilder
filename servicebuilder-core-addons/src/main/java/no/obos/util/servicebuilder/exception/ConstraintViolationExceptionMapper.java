package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;
import no.obos.util.servicebuilder.model.NoValidationLogging;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Payload;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Slf4j
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    final private ExceptionUtil exceptionUtil;

    @Inject
    public ConstraintViolationExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> errorSet = exception.getConstraintViolations();
        List<String> errors = errorSet.stream()
                .map(this::errorMessage)
                .collect(Collectors.toList());
        String msg = String.format("Validering av parametere feilet med: %s", errors.toString());

        return exceptionUtil.handle(exception, cfg -> cfg
                .status(BAD_REQUEST.getStatusCode())
                .logLevel(LogLevel.WARN)
                .detail(msg)
                .logger(log)
        );
    }

    private String errorMessage(ConstraintViolation<?> error) {
        Set<Class<? extends Payload>> payload = error.getConstraintDescriptor().getPayload();

        if (payload.contains(NoValidationLogging.class)) {
            return error.getPropertyPath().toString() + ", " + error.getMessage();
        } else {
            return error.getPropertyPath().toString() + ", " + error.getMessage() + ", was: " + error.getInvalidValue();
        }
    }

}

package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Slf4j
public class FieldLevelExceptionMapper implements ExceptionMapper<FieldLevelValidationException> {
    final private ExceptionUtil exceptionUtil;

    @Inject
    public FieldLevelExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(FieldLevelValidationException exception) {
        Map<String, List<String>> errorMap = exception.getErrorFields();
        List<String> errors = errorMap.keySet().stream()
                .map(error -> error + ", " + errorMap.get(error))
                .collect(Collectors.toList());
        String msg = String.format("Validering av objekt feilet med: %s", errors.toString());

        return exceptionUtil.handle(exception, cfg -> cfg
                .status(BAD_REQUEST.getStatusCode())
                .logLevel(LogLevel.WARN)
                .detail(msg)
                .logger(log)
        );
    }
}

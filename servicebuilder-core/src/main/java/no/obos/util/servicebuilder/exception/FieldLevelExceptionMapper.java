package no.obos.util.servicebuilder.exception;

import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.exception.FieldLevelValidationException;
import no.obos.util.servicebuilder.exception.domain.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Slf4j
public class FieldLevelExceptionMapper implements ExceptionMapper<FieldLevelValidationException> {
    @Inject
    private ExceptionUtil exceptionUtil;

    @Override
    public Response toResponse(FieldLevelValidationException exception) {
        Multimap<String, String> errorMap = exception.getErrorFields();
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

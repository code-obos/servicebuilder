package no.obos.util.servicebuilder.exception;

import com.google.common.collect.Multimap;
import no.obos.util.exception.FieldLevelValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.stream.Collectors;

public class FieldLevelExceptionMapper implements ExceptionMapper<FieldLevelValidationException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    @Override public Response toResponse(FieldLevelValidationException exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        Multimap<String, String> errorMap = ((FieldLevelValidationException) exception).getErrorFields();
        List<String> errors = errorMap.keySet().stream()
                .map(error -> error + ", " + errorMap.get(error))
                .collect(Collectors.toList());
        String msg = errors.toString();
        LOG.warn(ExceptionUtil.lagLoggMelding("Validering av objekt feilet med: " + msg, feilreferanse));

        Response.Status status = Response.Status.BAD_REQUEST;

        return ExceptionUtil.buildDefaultProblemResponse(status.getStatusCode(), msg, feilreferanse);
    }
}

package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import no.obos.util.exception.FieldLevelValidationException;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class FieldLevelExceptionMapper extends AbstractExceptionMapper<FieldLevelValidationException> {

    private static final Logger LOG = LoggerFactory.getLogger(FieldLevelExceptionMapper.class);

    public FieldLevelExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        super(shouldLogStacktraceConfig);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Level getLevel() {
        return Level.WARN;
    }

    @Override
    protected ProblemInformation getProblemInformation(FieldLevelValidationException exception) {
        Multimap<String, String> errorMap = exception.getErrorFields();
        List<String> errors = errorMap.keySet().stream()
                .map(error -> error + ", " + errorMap.get(error))
                .collect(Collectors.toList());
        String msg = String.format("Validering av objekt feilet med: %s", errors.toString());

        return new ProblemInformation(BAD_REQUEST.getStatusCode(), msg);
    }
}

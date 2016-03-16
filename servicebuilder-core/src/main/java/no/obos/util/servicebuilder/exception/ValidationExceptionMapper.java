package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class ValidationExceptionMapper extends AbstractExceptionMapper<ValidationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    public ValidationExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        super(shouldLogStacktraceConfig);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected Level getLevel() {
        return Level.ERROR;
    }

    @Override
    protected ProblemInformation getProblemInformation(ValidationException exception) {
        String msg = "Valideringfeil: " + exception.getMessage();
        return new ProblemInformation(BAD_REQUEST.getStatusCode(), msg);
    }
}

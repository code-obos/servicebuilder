package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class JsonProcessingExceptionMapper extends AbstractExceptionMapper<JsonProcessingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    public JsonProcessingExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
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
    protected ProblemInformation getProblemInformation(JsonProcessingException exception) {
        String msg = "Feil under jsonprosessenring: " + exception.getMessage();
        return new ProblemInformation(BAD_REQUEST.getStatusCode(), msg);
    }
}

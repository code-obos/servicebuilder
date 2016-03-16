package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;

public class WebApplicationExceptionMapper extends AbstractExceptionMapper<WebApplicationException> {

    private static final Logger LOG = LoggerFactory.getLogger(WebApplicationExceptionMapper.class);

    public WebApplicationExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
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
    protected ProblemInformation getProblemInformation(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        String msg = exception.getLocalizedMessage();
        return new ProblemInformation(status, msg);
    }

}

package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class RuntimeExceptionMapper extends AbstractExceptionMapper<RuntimeException> {
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionMapper.class);

    public RuntimeExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
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
    protected ProblemInformation getProblemInformation(RuntimeException exception) {
        return new ProblemInformation(INTERNAL_SERVER_ERROR.getStatusCode(), "Det har oppstått en intern feil");
    }
}

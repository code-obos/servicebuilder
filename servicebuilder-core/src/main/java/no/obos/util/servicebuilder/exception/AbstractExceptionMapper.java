package no.obos.util.servicebuilder.exception;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.domain.ProblemInformation;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;

public abstract class AbstractExceptionMapper<T extends Throwable> extends ContextAwareExceptionMapper<T> {
    private final ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig;

    public AbstractExceptionMapper(ImmutableMap<Class<?>, Boolean> shouldLogStacktraceConfig) {
        this.shouldLogStacktraceConfig = shouldLogStacktraceConfig;
    }

    @Override
    public Response toResponse(T exception) {
        String feilreferanse = ExceptionUtil.lagFeilreferanse();
        ProblemInformation problemInformation = getProblemInformation(exception);

        String loggMelding = ExceptionUtil.lagLoggMelding(problemInformation.getMessage(), feilreferanse);
        Logger logger = getLogger();
        Level level = getLevel();
        if (ExceptionUtil.shouldPrintStacktrace(exception, shouldLogStacktraceConfig)) {
            log(logger, level, loggMelding, exception);
        } else {
            log(logger, level, loggMelding);
        }

        return ExceptionUtil.buildDefaultProblemResponse(problemInformation.getStatus(), problemInformation.getMessage(), getMediaType(), feilreferanse);
    }

    protected abstract Logger getLogger();

    protected abstract Level getLevel();

    protected abstract ProblemInformation getProblemInformation(T exception);

    private void log(Logger logger, Level level, String loggMelding) {
        switch (level.levelInt) {
            case Level.TRACE_INT:
                logger.trace(loggMelding);
                break;
            case Level.WARN_INT:
                logger.warn(loggMelding);
                break;
            case Level.INFO_INT:
                logger.info(loggMelding);
                break;
            case Level.DEBUG_INT:
                logger.debug(loggMelding);
                break;
            case Level.ERROR_INT:
                logger.error(loggMelding);
                break;
        }
    }

    private void log(Logger logger, Level level, String loggMelding, T exception) {
        switch (level.levelInt) {
            case Level.TRACE_INT:
                logger.trace(loggMelding, exception);
                break;
            case Level.WARN_INT:
                logger.warn(loggMelding, exception);
                break;
            case Level.INFO_INT:
                logger.info(loggMelding, exception);
                break;
            case Level.DEBUG_INT:
                logger.debug(loggMelding, exception);
                break;
            case Level.ERROR_INT:
                logger.error(loggMelding, exception);
                break;
        }
    }
}

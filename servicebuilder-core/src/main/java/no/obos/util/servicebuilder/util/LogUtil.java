package no.obos.util.servicebuilder.util;

import no.obos.util.servicebuilder.model.LogLevel;
import org.slf4j.Logger;

public class LogUtil {
    public static void doLog(String s, LogLevel logLevel, Logger logger) {
        switch (logLevel) {
            case ERROR: {
                logger.error(s);
                break;
            }

            case WARN: {
                logger.warn(s);
                break;
            }

            case INFO: {
                logger.info(s);
                break;
            }

            case DEBUG: {
                logger.debug(s);
                break;
            }

            case TRACE: {
                logger.trace(s);
                break;
            }

            default: {
                logger.error(s);
                break;
            }
        }
    }

    public static void doLog(String s, Throwable exception, LogLevel logLevel, Logger logger) {
        switch (logLevel) {
            case ERROR: {
                logger.error(s, exception);
                break;
            }

            case WARN: {
                logger.warn(s, exception);
                break;
            }

            case INFO: {
                logger.info(s, exception);
                break;
            }

            case DEBUG: {
                logger.debug(s, exception);
                break;
            }

            case TRACE: {
                logger.trace(s, exception);
                break;
            }

            default: {
                logger.error(s, exception);
                break;
            }
        }
    }
}

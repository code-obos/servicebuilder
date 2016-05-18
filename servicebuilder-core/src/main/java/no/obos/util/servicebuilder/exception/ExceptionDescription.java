package no.obos.util.servicebuilder.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.servicebuilder.exception.domain.LogLevel;
import org.slf4j.Logger;

@Builder(toBuilder = true)
@AllArgsConstructor
public class ExceptionDescription {

    /**
     * The exception in question. Required.
     */
    public final Throwable exception;

    /**
     * Http status code of error. Defaults to 500.
     */
    public final Integer status;

    /**
     * A brief title for the error condition. Should be the same for every problem of the same type.
     * Defaults to the text description of the HTTP status code or "Unclassified error" for codes without standard description
     */
    public final String title;

    /**
     * Detailed description of error that is sent to the client. Default exceptions message (set to emptystring to clear).
     */
    public final String detail;

    /**
     * String identifying error for easy log searching. Default to randomly generated uuid.
     */
    public final String reference;

    /**
     * Level the exception should be logged on. Defaults to Error.
     */
    public final LogLevel logLevel;

    /**
     * Should stacktrace be logged? Default derived from configuration settings.
     */
    public final Boolean logStackTrace;

    /**
     * Additional detail that should be logged but not exposed externally.
     */
    public final String internalMessage;

    /**
     * Logger used for logging. Defaults to ExceptionUtil logger.
     */
    public final Logger logger;
}

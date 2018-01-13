package no.obos.util.servicebuilder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;

import java.util.Map;

@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
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
     * "type" (string) - A URI reference [RFC3986] that identifies the
     * problem type.  This specification encourages that, when
     * dereferenced, it provide human-readable documentation for the
     * problem type (e.g., using HTML [W3C.REC-html5-20141028]).  When
     * this member is not present, its value is assumed to be
     * "about:blank".
     */
    public final String type;

    /**
     * Additional fileds exposed in return to client
     */
    public final Map<String, String> context;

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
     * If true, detail is user-friendly and may be displayed in gui of calling application
     */
    public final boolean userMessageInDetail;

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

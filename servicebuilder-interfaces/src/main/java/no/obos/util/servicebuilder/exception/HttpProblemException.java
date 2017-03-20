package no.obos.util.servicebuilder.exception;

import lombok.Getter;
import no.obos.util.servicebuilder.model.LogLevel;
import no.obos.util.servicebuilder.model.ProblemResponse;

import javax.ws.rs.WebApplicationException;

public class HttpProblemException extends WebApplicationException{
    @Getter
    private final ProblemResponse problemResponse;

    @Getter
    private final LogLevel logLevel;

    @Getter
    private final boolean logStacktrace;

    public HttpProblemException(ProblemResponse problemResponse, LogLevel logLevel, boolean logStacktrace) {
        super(problemResponse.title, problemResponse.status);
        this.problemResponse = problemResponse;
        this.logLevel = logLevel;
        this.logStacktrace = logStacktrace;
    }

    public HttpProblemException(ProblemResponse problemResponse, Throwable cause, LogLevel logLevel, boolean logStacktrace) {
        super(problemResponse.title, cause, problemResponse.status);
        this.problemResponse = problemResponse;
        this.logLevel = logLevel;
        this.logStacktrace = logStacktrace;
    }
}

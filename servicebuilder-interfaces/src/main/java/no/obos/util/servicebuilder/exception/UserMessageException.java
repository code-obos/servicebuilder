package no.obos.util.servicebuilder.exception;

import lombok.Getter;

import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Getter
public class UserMessageException extends WebApplicationException {

    public UserMessageException(String detail) {
        super(detail, BAD_REQUEST);
    }

    public UserMessageException(String detail, Throwable cause) {
        super(detail, cause, BAD_REQUEST);
    }

    public UserMessageException(String detail, int status) {
        super(detail, status);
    }

    public UserMessageException(String detail, Throwable cause, int status) {
        super(detail, cause, status);
    }
}

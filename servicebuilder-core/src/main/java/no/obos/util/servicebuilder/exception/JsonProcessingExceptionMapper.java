package no.obos.util.servicebuilder.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.exception.domain.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Slf4j
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Inject ExceptionUtil exceptionUtil;

    @Override
    public Response toResponse(JsonProcessingException exception) {
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(BAD_REQUEST.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail("Det har oppstått en intern feil")
                .logger(log)
        );
    }
}

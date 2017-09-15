package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

@Slf4j
public class UserMessageExceptionMapper implements ExceptionMapper<UserMessageException> {
    final private ExceptionUtil exceptionUtil;

    @Inject
    public UserMessageExceptionMapper(ExceptionUtil exceptionUtil) {
        this.exceptionUtil = exceptionUtil;
    }

    @Override
    public Response toResponse(UserMessageException exception) {
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(exception.getResponse().getStatus())
                .logLevel(LogLevel.WARN)
                .detail(exception.getMessage())
                .userMessageInDetail(true)
                .logger(log)
        );
    }

}

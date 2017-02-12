package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.LogLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Slf4j
public class ExternalResourceExceptionMapper implements ExceptionMapper<ExternalResourceException> {
    final private ExceptionUtil exceptionUtil;

    @Inject
    public ExternalResourceExceptionMapper(ExceptionUtil exceptionUtil) {this.exceptionUtil = exceptionUtil;}

    @Override
    public Response toResponse(ExternalResourceException exception) {
        String detail;
        if(exception.metaData.gotAnswer) {
            detail = "Feil under kommunikasjon med " + exception.metaData.targetName;
        } else {
            detail = "Kan ikke nå " + exception.metaData.targetName;
        }
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(INTERNAL_SERVER_ERROR.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail("Det har oppstått en intern feil")
                .internalMessage(exception.metaData.toString())
                .logger(log)
        );
    }

}

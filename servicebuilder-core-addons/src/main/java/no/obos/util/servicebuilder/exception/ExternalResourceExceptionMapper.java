package no.obos.util.servicebuilder.exception;

import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.model.LogLevel;

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
        if (exception.getMetaData().gotAnswer) {
            detail = "Feil under kommunikasjon med " + exception.getMetaData().targetName;
        } else {
            detail = "Kan ikke nå " + exception.getMetaData().targetName;
        }
        return exceptionUtil.handle(exception, cfg -> cfg
                .status(INTERNAL_SERVER_ERROR.getStatusCode())
                .logLevel(LogLevel.ERROR)
                .detail(detail)
                .reference(exception.getMetaData().incidentReferenceId)
                .internalMessage(exception.getMetaData().toString())
                .logger(log)
        );
    }

}

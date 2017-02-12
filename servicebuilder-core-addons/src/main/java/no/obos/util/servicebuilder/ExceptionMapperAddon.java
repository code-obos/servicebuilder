package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.servicebuilder.exception.ConstraintViolationExceptionMapper;
import no.obos.util.servicebuilder.exception.ExceptionUtil;
import no.obos.util.servicebuilder.exception.ExternalResourceExceptionMapper;
import no.obos.util.servicebuilder.exception.FieldLevelExceptionMapper;
import no.obos.util.servicebuilder.exception.JsonProcessingExceptionMapper;
import no.obos.util.servicebuilder.exception.RuntimeExceptionMapper;
import no.obos.util.servicebuilder.exception.ValidationExceptionMapper;
import no.obos.util.servicebuilder.exception.WebApplicationExceptionMapper;

import javax.ws.rs.NotFoundException;

/**
 * Legger til et sett med standard exceptionmappere for Jersey som mapper til problem response.
 * Logger stacktrace for de fleste exceptions, med unntak av exceptions og underexceptions satt til false i config.stacktraceConfig.
 * Config.logAllStackTraces er ment for debug-bruk.
 */
@Builder(toBuilder = true)
public class ExceptionMapperAddon implements Addon {

    public final boolean mapFieldLevelValidation;
    public final boolean mapJsonProcessing;
    public final boolean mapRuntime;
    public final boolean mapValidation;
    public final boolean mapWebApplication;
    public final boolean logAllStacktraces;
    public final ImmutableMap<Class<?>, Boolean> stacktraceConfig;


    public static class ExceptionMapperAddonBuilder {
        boolean mapFieldLevelValidation = true;
        boolean mapJsonProcessing = true;
        boolean mapRuntime = true;
        boolean mapValidation = true;
        boolean mapWebApplication = true;
        boolean logAllStacktraces = false;
        ImmutableMap<Class<?>, Boolean> stacktraceConfig = ImmutableMap.<Class<?>, Boolean>builder()
                .put(Throwable.class, true)
                .put(NotFoundException.class, false)
                .build();

    }



    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> {
            if (mapFieldLevelValidation) {
                registrator.register(FieldLevelExceptionMapper.class);
            }
            if (mapJsonProcessing) {
                registrator.register(JsonProcessingExceptionMapper.class);
            }
            if (mapRuntime) {
                registrator.register(RuntimeExceptionMapper.class);
            }
            if (mapValidation) {
                registrator.register(ValidationExceptionMapper.class);
            }
            if (mapWebApplication) {
                registrator.register(WebApplicationExceptionMapper.class);
            }
            registrator.register(ConstraintViolationExceptionMapper.class);
            registrator.register(ExternalResourceExceptionMapper.class);
        });
        jerseyConfig.addBinder(binder -> {
            binder.bind(this).to(ExceptionMapperAddon.class);
            binder.bindAsContract(ExceptionUtil.class);
        });
    }
}

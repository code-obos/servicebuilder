package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.exception.ConstraintViolationExceptionMapper;
import no.obos.util.servicebuilder.exception.ExceptionUtil;
import no.obos.util.servicebuilder.exception.ExternalResourceExceptionMapper;
import no.obos.util.servicebuilder.exception.FieldLevelExceptionMapper;
import no.obos.util.servicebuilder.exception.HttpProblemExceptionMapper;
import no.obos.util.servicebuilder.exception.JsonProcessingExceptionMapper;
import no.obos.util.servicebuilder.exception.RuntimeExceptionMapper;
import no.obos.util.servicebuilder.exception.UserMessageExceptionMapper;
import no.obos.util.servicebuilder.exception.ValidationExceptionMapper;
import no.obos.util.servicebuilder.exception.WebApplicationExceptionMapper;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;

import javax.ws.rs.NotFoundException;

/**
 * Legger til et sett med standard exceptionmappere for Jersey som mapper til problem response.
 * Logger stacktrace for de fleste exceptions, med unntak av exceptions og underexceptions satt til false i config.stacktraceConfig.
 * Config.logAllStackTraces er ment for debug-bruk.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMapperAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    public final ImmutableMap<Class<?>, Boolean> stacktraceConfig;

    public static ExceptionMapperAddon exceptionMapperAddon = new ExceptionMapperAddon(
            ImmutableMap.<Class<?>, Boolean>builder()
                    .put(Throwable.class, true)
                    .put(NotFoundException.class, false)
                    .build()
    );


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> {
            registrator.register(FieldLevelExceptionMapper.class);
            registrator.register(JsonProcessingExceptionMapper.class);
            registrator.register(RuntimeExceptionMapper.class);
            registrator.register(ValidationExceptionMapper.class);
            registrator.register(WebApplicationExceptionMapper.class);
            registrator.register(ConstraintViolationExceptionMapper.class);
            registrator.register(ExternalResourceExceptionMapper.class);
            registrator.register(UserMessageExceptionMapper.class);
            registrator.register(HttpProblemExceptionMapper.class);
        });
        jerseyConfig.addBinder(binder -> {
            binder.bind(this).to(ExceptionMapperAddon.class);
            binder.bindAsContract(ExceptionUtil.class);
        });
    }

    public ExceptionMapperAddon stacktraceConfig(Class<?> key, boolean value) {
        return this.withStacktraceConfig(GuavaHelper.plus(stacktraceConfig, key, value));
    }
}

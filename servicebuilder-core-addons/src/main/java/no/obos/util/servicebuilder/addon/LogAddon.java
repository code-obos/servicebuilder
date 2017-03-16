package no.obos.util.servicebuilder.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.log.LogRequestFilter;
import no.obos.util.servicebuilder.log.LogResponseFilter;
import no.obos.util.servicebuilder.log.RestLogger;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.RestLogConfiguration;
import no.obos.util.servicebuilder.model.Addon;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    public final RestLogConfiguration restLogConfiguration;

    public final static LogAddon defaults = new LogAddon(
            RestLogConfiguration.builder()
                    .enableDefault(true)
                    .defaultLogParams(
                            LogParams.defaults
                    ).build()
    );

    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        serviceConfig.addBinder(binder -> {
            binder.bind(restLogConfiguration).to(RestLogConfiguration.class);
            binder.bindAsContract(RestLogger.class);
        });
        serviceConfig.addRegistations(registrator ->
                registrator
                        .register(LogRequestFilter.class)
                        .register(LogResponseFilter.class)
        );
    }

    public LogAddon restLogConfiguration(RestLogConfiguration restLogConfiguration) {return withRestLogConfiguration(restLogConfiguration);}
}

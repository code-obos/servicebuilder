package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.log.LogRequestFilter;
import no.obos.util.servicebuilder.log.LogResponseFilter;
import no.obos.util.servicebuilder.log.RestLogger;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.RestLogConfiguration;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogAddon implements Addon {

    @Wither
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
            binder.bind(RestLogger.class).to(RestLogger.class);
        });
        serviceConfig.addRegistations(registrator ->
                registrator
                        .register(LogRequestFilter.class)
                        .register(LogResponseFilter.class)
        );
    }
}

package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.log.ServerLogFilter;
import no.obos.util.servicebuilder.log.ServerLogger;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.model.Addon;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ServerLogAddon implements Addon {

    public final ImmutableList<Predicate<ContainerRequestContext>> fastTrackFilters =
            ImmutableList.of(
                    request -> "OPTIONS".equals(request.getMethod()),
                    request -> request.getUriInfo() != null
                            && request.getUriInfo().getAbsolutePath().toString().contains("swagger.json"),
                    request -> request.getUriInfo() != null
                            && request.getUriInfo().getAbsolutePath().toString().contains("metrics")
            );

    @Wither(AccessLevel.PRIVATE)
    public final LogParams logParams;

    public static final ServerLogAddon serverLogAddon = new ServerLogAddon(LogParams.defaults);



    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        ServerLogger serverLogger = new ServerLogger(fastTrackFilters, logParams);
        serviceConfig.addBinder(binder -> binder.bind(serverLogger).to(ServerLogger.class));
        serviceConfig.addRegistations(registrator ->
                registrator
                        .register(ServerLogFilter.class)
        );
    }

    public ServerLogAddon logParams(LogParams logParams) {
        return withLogParams(logParams);
    }
}

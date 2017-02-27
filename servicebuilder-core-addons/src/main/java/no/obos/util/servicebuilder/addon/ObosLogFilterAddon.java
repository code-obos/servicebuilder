package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.log.ObosLogFilter;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Legger til filtre for ObosLogFilter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObosLogFilterAddon implements Addon {
    public static final ImmutableList<String> DEFAULT_BLACKLIST = ImmutableList.of("/swagger.json");
    public static final ImmutableList<DispatcherType> DEFAULT_DISPATCHES = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<String> blacklist;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<DispatcherType> dispatches;

    @Wither
    public final String pathSpec;

    public static ObosLogFilterAddon defaults = new ObosLogFilterAddon(ImmutableList.of("/swagger.json"), ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC), null);

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosLogFilter logFilter = new ObosLogFilter(path -> {
            for (String blocked : blacklist) {
                if (blocked.equalsIgnoreCase(path)) {
                    return false;
                }
            }
            return true;
        });
        String pathSpeckToUse = pathSpec;
        if (pathSpec == null) {
            pathSpeckToUse = jettyServer.configuration.apiPathSpec;
        }
        FilterHolder logFilterHolder = new FilterHolder(logFilter);
        jettyServer.getServletContext()
                .addFilter(logFilterHolder, pathSpeckToUse, EnumSet.copyOf(dispatches));
    }

    public ObosLogFilterAddon plusBlacklisted(String blacklist) {
        return withBlacklist(GuavaHelper.plus(this.blacklist, blacklist));
    }

    public ObosLogFilterAddon plusDispatch(DispatcherType dispatcherType) {
        return withDispatches(GuavaHelper.plus(dispatches, dispatcherType));
    }
}

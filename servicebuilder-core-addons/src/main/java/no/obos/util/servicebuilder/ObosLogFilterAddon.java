package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.log.ObosLogFilter;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Legger til filtre for ObosLogFilter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ObosLogFilterAddon implements Addon {
    public static final ImmutableList<String> DEFAULT_BLACKLIST = ImmutableList.of("/swagger.json");
    public static final ImmutableList<DispatcherType> DEFAULT_DISPATCHES = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    public final ImmutableList<String> blacklist;
    public final ImmutableList<DispatcherType> dispatches;

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

    public ObosLogFilterAddon blacklist(ImmutableList<String> blacklist) {return this.blacklist == blacklist ? this : new ObosLogFilterAddon(blacklist, this.dispatches, this.pathSpec);}

    public ObosLogFilterAddon blacklist(String blacklist) {
        return blacklist(GuavaHelper.plus(this.blacklist, blacklist));
    }

    public ObosLogFilterAddon dispatches(ImmutableList<DispatcherType> dispatches) {return this.dispatches == dispatches ? this : new ObosLogFilterAddon(this.blacklist, dispatches, this.pathSpec);}

    public ObosLogFilterAddon dispatch(DispatcherType dispatcherType) {
        return dispatches(GuavaHelper.plus(dispatches, dispatcherType));
    }

    public ObosLogFilterAddon pathSpec(String pathSpec) {return Objects.equals(this.pathSpec, pathSpec) ? this : new ObosLogFilterAddon(this.blacklist, this.dispatches, pathSpec);}
}

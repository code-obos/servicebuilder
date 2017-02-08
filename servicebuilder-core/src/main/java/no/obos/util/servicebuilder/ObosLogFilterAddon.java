package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import no.obos.util.log.ObosLogFilter;
import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Legger til filtre for ObosLogFilter
 */
@Builder(toBuilder = true)
public class ObosLogFilterAddon implements Addon {
    public static final ImmutableList<String> DEFAULT_BLACKLIST = ImmutableList.of("/swagger.json");
    public static final ImmutableList<DispatcherType> DEFAULT_DISPATCHES = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    public final ImmutableList<DispatcherType> dispatches;
    public final ImmutableList<String> blacklist;

    public final String pathSpec;


    public static class ObosLogFilterAddonBuilder {
        ImmutableList<DispatcherType> dispatches = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        ImmutableList<String> blacklist = ImmutableList.of("/swagger.json");

    }

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
}

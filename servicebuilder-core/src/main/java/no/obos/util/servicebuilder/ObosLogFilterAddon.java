package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import no.obos.util.config.AppConfig;
import no.obos.util.log.ObosLogFilter;
import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@AllArgsConstructor
public class ObosLogFilterAddon extends ServiceAddonEmptyDefaults {
    public static final ImmutableList<String> DEFAULT_BLACKLIST = ImmutableList.of("/swagger.json");
    public static final ImmutableList<DispatcherType> DEFAULT_DISPATCHES = ImmutableList.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

    public final Configuration configuration;

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ObosLogFilter logFilter = new ObosLogFilter(path -> {
            for (String blocked : configuration.blacklist) {
                if (blocked.equalsIgnoreCase(path)) {
                    return false;
                }
            }
            return true;
        });
        String pathSpeckToUse = configuration.pathSpec;
        if (configuration.pathSpec == null) {
            pathSpeckToUse = jettyServer.configuration.apiPathSpec;
        }
        FilterHolder logFilterHolder = new FilterHolder(logFilter);
        jettyServer.getServletContext()
                .addFilter(logFilterHolder, pathSpeckToUse, EnumSet.copyOf(configuration.dispatches));
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        @Singular("dispatch")
        public final ImmutableList<DispatcherType> dispatches;
        String pathSpec = null;
        @Singular("blacklisted")
        public final ImmutableList<String> blacklist;

    }

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder()
                .blacklist(DEFAULT_BLACKLIST)
                .dispatches(DEFAULT_DISPATCHES);
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ObosLogFilterAddon> {
        Configurator options;
        Configuration.ConfigurationBuilder configBuilder;

        @Override
        public void addAppConfig(AppConfig appConfig) {
            configFromAppConfig(appConfig, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public ObosLogFilterAddon init() {
            configBuilder = options.apply(configBuilder);
            return new ObosLogFilterAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfiguration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

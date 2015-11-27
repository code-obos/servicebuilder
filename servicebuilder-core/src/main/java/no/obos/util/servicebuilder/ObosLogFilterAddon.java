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

    public final Config config;

    @Override public void addToJettyServer(JettyServer jettyServer) {
        ObosLogFilter logFilter = new ObosLogFilter(path -> {
            for (String blocked : config.blacklist) {
                if (blocked.equalsIgnoreCase(path)) {
                    return false;
                }
            }
            return true;
        });
        String pathSpeckToUse = config.pathSpec;
        if (config.pathSpec == null) {
            pathSpeckToUse = jettyServer.config.apiPathSpec;
        }
        FilterHolder logFilterHolder = new FilterHolder(logFilter);
        jettyServer.getServletContext()
                .addFilter(logFilterHolder, pathSpeckToUse, EnumSet.copyOf(config.dispatches));
    }

    @Builder
    @AllArgsConstructor
    public static class Config {
        @Singular("dispatch")
        public final ImmutableList<DispatcherType> dispatches;
        String pathSpec = null;
        @Singular("blacklisted")
        public final ImmutableList<String> blacklist;

    }

    public static Config.ConfigBuilder defaultConfig() {
        return Config.builder()
                .blacklist(DEFAULT_BLACKLIST)
                .dispatches(DEFAULT_DISPATCHES);
    }

    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<ObosLogFilterAddon> {
        Configurator options;
        Config.ConfigBuilder configBuilder;

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

    public static AddonBuilder config(Configurator options) {
        return new AddonBuilder(options, defaultConfig());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfig());
    }

    public interface Configurator {
        Config.ConfigBuilder apply(Config.ConfigBuilder configBuilder);
    }
}

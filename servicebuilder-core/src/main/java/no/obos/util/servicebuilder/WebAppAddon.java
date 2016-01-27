package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.util.config.AppConfig;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@AllArgsConstructor
public class WebAppAddon extends ServiceAddonEmptyDefaults {

    public static final String DEFAULT_PATH_SPEC = "/webapp/*";
    public static final Integer DEFAULT_SESSION_TIMEOUT_SECONDS = 28800;
    public static final String CONFIG_KEY_RESOURCE_URL = "webapp.resource.url";

    static final Logger LOGGER = LoggerFactory.getLogger(WebAppAddon.class);

    public final Configuration configuration;

    public static Configuration.ConfigurationBuilder defaultConfigurationuration() {
        return Configuration.builder()
                .pathSpec(DEFAULT_PATH_SPEC)
                .sessionTimeoutSeconds(DEFAULT_SESSION_TIMEOUT_SECONDS);
    }

    public static void configFromAppConfig(AppConfig appConfig, Configuration.ConfigurationBuilder configBuilder) {
        appConfig.failIfNotPresent(CONFIG_KEY_RESOURCE_URL);
        try {
            configBuilder.resourceUri(new URI(appConfig.get(CONFIG_KEY_RESOURCE_URL)));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }


    @Override public void addToJettyServer(JettyServer jettyServer) {


        WebAppContext webAppContext;
        webAppContext = new WebAppContext();
        String warUrlString;
        String scheme = configuration.resourceUri.getScheme();
        if(scheme == null) {
            throw new IllegalStateException("URI did not contain scheme: " + configuration.resourceUri.toString());
        }
        String path = configuration.resourceUri.getSchemeSpecificPart();
        path = (path.startsWith("//")) ? path.substring(2) : path;
        switch (scheme) {
            case "file":
                webAppContext.setInitParameter("useFileMappedBuffer", "false");
                LOGGER.warn("*** Kjører i DEV-modus, leser webfiler rett fra utviklingskataloger. ***");
                warUrlString = path;
                File f = new File(warUrlString);
                if (! f.exists()) {
                    throw new IllegalStateException("Could not find file " + path);
                }
                break;
            case "classpath":
                final URL warUrl = WebAppAddon.class.getClassLoader().getResource(path);
                warUrlString = warUrl.toExternalForm();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI scheme " + scheme + ". Allowed: classpath, file");
        }
        webAppContext.setResourceBase(warUrlString);
        webAppContext.setContextPath(jettyServer.configuration.contextPath + configuration.pathSpec);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.getSessionHandler().getSessionManager().setMaxInactiveInterval(configuration.sessionTimeoutSeconds);
        jettyServer.addAppContext(webAppContext);
    }


    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String pathSpec;
        public final int sessionTimeoutSeconds;
        public final URI resourceUri;
    }


    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<WebAppAddon> {
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
        public WebAppAddon init() {
            configBuilder = options.apply(configBuilder);
            return new WebAppAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfigurationuration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfigurationuration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

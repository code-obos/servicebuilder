package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Legger serving av statiske filer. Standard path er tjeneste/versjon/webapp/ .
 * Lokasjon av statiske filer kan spesifiseres med file:// (relativ path på filsystemet) eller classpath:// .
 */
@Builder(toBuilder = true)
public class WebAppAddon implements Addon {
    public static final String CONFIG_KEY_RESOURCE_URL = "webapp.resource.url";
    static final Logger LOGGER = LoggerFactory.getLogger(WebAppAddon.class);

    public final String pathSpec;
    public final int sessionTimeoutSeconds;
    public final URI resourceUri;

    public static class WebAppAddonBuilder {
        String pathSpec = "/webapp/*";
        int sessionTimeoutSeconds = 28800;
    }



    @Override
    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_RESOURCE_URL);
        try {
            return this.toBuilder().resourceUri(new URI(properties.get(CONFIG_KEY_RESOURCE_URL))).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        WebAppContext webAppContext;
        webAppContext = new WebAppContext();
        String warUrlString;
        String scheme = resourceUri.getScheme();
        if (scheme == null) {
            throw new IllegalStateException("URI did not contain scheme: " + resourceUri.toString());
        }
        String path = resourceUri.getSchemeSpecificPart();
        path = (path.startsWith("//")) ? path.substring(2) : path;
        switch (scheme) {
            case "file":
                webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
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
        webAppContext.setContextPath(jettyServer.configuration.contextPath + pathSpec);
        webAppContext.setParentLoaderPriority(true);
        webAppContext.getSessionHandler().getSessionManager().setMaxInactiveInterval(sessionTimeoutSeconds);
        jettyServer.addAppContext(webAppContext);
    }
}

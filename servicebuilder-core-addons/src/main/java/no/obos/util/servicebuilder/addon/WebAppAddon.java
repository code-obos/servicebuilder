package no.obos.util.servicebuilder.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WebAppAddon implements Addon {
    public static final String CONFIG_KEY_RESOURCE_URL = "webapp.resource.url";
    static final Logger LOGGER = LoggerFactory.getLogger(WebAppAddon.class);

    @Wither(AccessLevel.PRIVATE)
    public final String pathSpec;

    @Wither(AccessLevel.PRIVATE)
    public final URI resourceUri;

    public static WebAppAddon webAppAddon = new WebAppAddon("/webapp/*", null);



    @Override
    public Addon withProperties(PropertyProvider properties) {
        try {
            return this.resourceUri(new URI(properties.requireWithFallback(CONFIG_KEY_RESOURCE_URL, resourceUri==null?null:resourceUri.toString())));
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
                if (warUrl == null) {
                    throw new NullPointerException();
                }
                warUrlString = warUrl.toExternalForm();
                break;
            default:
                throw new IllegalArgumentException("Unrecognized URI scheme " + scheme + ". Allowed: classpath, file");
        }
        webAppContext.setResourceBase(warUrlString);
        webAppContext.setContextPath(jettyServer.configuration.contextPath + pathSpec);
        webAppContext.setParentLoaderPriority(true);
        jettyServer.addAppContext(webAppContext);
    }

    public WebAppAddon pathSpec(String pathSpec) {
        return withPathSpec(pathSpec);
    }


    public WebAppAddon resourceUri(URI resourceUri) {
        return withResourceUri(resourceUri);
    }
}

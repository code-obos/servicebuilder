package no.obos.util.servicebuilder;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.config.JerseyJaxrsConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import org.eclipse.jetty.servlet.ServletHolder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerAddon implements Addon {
    public static final String CONFIG_KEY_API_BASEURL = "api.baseurl";
    public final String pathSpec = "/swagger";

    @Wither
    public final String apiBasePath;
    @Wither
    public final String apiVersion;

    public static SwaggerAddon defaults = new SwaggerAddon(null, null);

    @Override
    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_API_BASEURL);
        return this
                .withApiBasePath(properties.get(CONFIG_KEY_API_BASEURL))
                .withApiVersion(properties.get(Constants.CONFIG_KEY_SERVICE_VERSION));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator
                .register(ApiListingResource.class)
                .register(SwaggerSerializers.class)
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder apiDocServletHolder = new ServletHolder(new JerseyJaxrsConfig());
        apiDocServletHolder.setInitParameter("api.version", apiVersion);
        //Remove leading / as swagger adds its own
        String apiBasePath =
                "//".equals(this.apiBasePath.substring(0, 1))
                        ? this.apiBasePath.substring(1)
                        : this.apiBasePath;
        apiDocServletHolder.setInitParameter("swagger.api.basepath", apiBasePath);
        apiDocServletHolder.setInitOrder(2); //NOSONAR
        jettyServer.getServletContext().addServlet(apiDocServletHolder, pathSpec);
    }
}

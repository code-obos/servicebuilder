package no.obos.util.servicebuilder.experimental;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.config.JerseyJaxrsConfig;
import lombok.Builder;
import no.obos.util.servicebuilder.Constants;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.PropertyProvider;
import org.eclipse.jetty.servlet.ServletHolder;

@Builder(toBuilder = true)
public class SwaggerAddon implements Addon {
    public static final String CONFIG_KEY_API_BASEURL = "api.baseurl";

    public final String apiBasePath;
    public final String pathSpec = "/swagger";
    public final String apiVersion;

    @Override
    public Addon withProperties(PropertyProvider properties) {
        properties.failIfNotPresent(CONFIG_KEY_API_BASEURL);
        return toBuilder()
                .apiBasePath(properties.get(CONFIG_KEY_API_BASEURL))
                .apiVersion(properties.get(Constants.CONFIG_KEY_SERVICE_VERSION))
                .build();
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

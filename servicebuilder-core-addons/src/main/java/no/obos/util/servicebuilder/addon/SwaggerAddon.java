package no.obos.util.servicebuilder.addon;

import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import io.swagger.jersey.config.JerseyJaxrsConfig;
import io.swagger.models.Swagger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import static com.google.common.base.MoreObjects.firstNonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SwaggerAddon implements Addon {
    private static final String API_PATH_SPEC = "/api";
    private static final String SWAGGER_PATH_SPEC = "/swagger";

    @Wither(AccessLevel.PRIVATE)
    public final String apiVersion;

    public static SwaggerAddon defaults = new SwaggerAddon(null);

    @Override
    public Addon withProperties(PropertyProvider properties) {
        return this.apiVersion(properties.get(Constants.CONFIG_KEY_SERVICE_VERSION));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator
                .register(ObosApiListingResource.class)
                .register(SwaggerSerializers.class)
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder apiDocServletHolder = new ServletHolder(new JerseyJaxrsConfig());
        apiDocServletHolder.setInitParameter("api.version", apiVersion);
        apiDocServletHolder.setInitParameter("swagger.api.basepath", API_PATH_SPEC);
        apiDocServletHolder.setInitOrder(2); //NOSONAR
        jettyServer.getServletContext().addServlet(apiDocServletHolder, SWAGGER_PATH_SPEC);
    }

    public SwaggerAddon apiVersion(String apiVersion) {
        return withApiVersion(apiVersion);
    }

    private static class ObosApiListingResource extends ApiListingResource {
        @Override
        protected Swagger process(Application app, ServletContext servletContext, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo) {
            Swagger swagger = super.process(app, servletContext, sc, headers, uriInfo);
            String contextPath = firstNonNull(headers.getHeaderString("X-Forwarded-Path"), servletContext.getContextPath());
            return copy(swagger).basePath(join(contextPath, swagger.getBasePath()));
        }

        private String join(String a, String b) {
            return ("/" + a + "/" + b + "/").replaceAll("/+", "/");
        }

        private Swagger copy(Swagger original) {
            Swagger copy = new Swagger();
            copy.setInfo(original.getInfo());
            copy.setHost(original.getHost());
            copy.setBasePath(original.getBasePath());
            copy.setTags(original.getTags());
            copy.setSchemes(original.getSchemes());
            copy.setConsumes(original.getConsumes());
            copy.setProduces(original.getProduces());
            copy.setSecurity(original.getSecurity());
            copy.setPaths(original.getPaths());
            copy.setSecurityDefinitions(original.getSecurityDefinitions());
            copy.setDefinitions(original.getDefinitions());
            copy.setParameters(original.getParameters());
            copy.setResponses(original.getResponses());
            copy.setExternalDocs(original.getExternalDocs());
            copy.setVendorExtensions(original.getVendorExtensions());
            return copy;
        }
    }
}

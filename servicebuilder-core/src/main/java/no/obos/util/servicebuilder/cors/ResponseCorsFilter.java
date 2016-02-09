package no.obos.util.servicebuilder.cors;

import com.google.common.base.Joiner;
import no.obos.util.servicebuilder.CorsFilterAddon;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

public class ResponseCorsFilter implements ContainerResponseFilter {

    @Inject
    CorsFilterAddon.Configuration configuration;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
    {

        Joiner joiner = Joiner.on(", ").skipNulls();
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", joiner.join(configuration.allowOrigin));
        headers.add("Access-Control-Allow-Methods", joiner.join(configuration.allowMethods));
        headers.add("Access-Control-Allow-Headers", joiner.join(configuration.allowHeaders));
        headers.add("Access-Control-Allow-Credentials", "true");
    }
}

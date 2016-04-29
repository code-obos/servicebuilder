package no.obos.util.servicebuilder.cors;

import com.google.common.base.Joiner;
import no.obos.util.servicebuilder.CorsFilterAddon;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Optional;

public class ResponseCorsFilter implements ContainerResponseFilter {

    @Inject
    CorsFilterAddon.Configuration configuration;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
    {
        Optional<String> requestedOriginOpt = Optional.ofNullable(requestContext.getHeaderString("Origin"));
        Optional<String> requestedMethodsOpt = Optional.ofNullable(requestContext.getHeaderString("Access-Control-Request-Methods"));
        Optional<String> requestedHeadersOpt = Optional.ofNullable(requestContext.getHeaderString("Access-Control-Request-Headers"));

        Joiner joiner = Joiner.on(", ").skipNulls();

        String originFallback = joiner.join(configuration.allowOrigin);
        String methodsFallback = joiner.join(configuration.allowMethods);
        String headersFallback = joiner.join(configuration.allowHeaders);

        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", requestedOriginOpt.orElse(originFallback));
        headers.add("Access-Control-Allow-Methods", requestedMethodsOpt.orElse(methodsFallback));
        headers.add("Access-Control-Allow-Headers", requestedHeadersOpt.orElse(headersFallback));
        headers.add("Access-Control-Allow-Credentials", "true");
    }
}

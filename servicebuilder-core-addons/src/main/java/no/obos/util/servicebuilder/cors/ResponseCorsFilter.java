package no.obos.util.servicebuilder.cors;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

public class ResponseCorsFilter implements ContainerResponseFilter {

    public static final String ORIGIN = "Origin";
    public static final String REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException
    {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add(ALLOW_ORIGIN, requestContext.getHeaderString(ORIGIN));
        headers.add(ALLOW_METHODS, requestContext.getHeaderString(REQUEST_METHOD));
        headers.add(ALLOW_HEADERS, requestContext.getHeaderString(REQUEST_HEADERS));
        headers.add(ALLOW_CREDENTIALS, true);
    }
}

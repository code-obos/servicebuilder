package no.obos.util.servicebuilder.cors;

import com.google.common.base.Joiner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;

public class ResponseCorsFilter implements ContainerResponseFilter {

    private final String allowOriginString;
    private final String allowMethodsString;
    private final String allowHeadersString;

    public ResponseCorsFilter(List<String> allowOrigin, List<String> allowMethods, List<String> allowHeaders) {
        Joiner joiner = Joiner.on(", ").skipNulls();
        allowOriginString = joiner.join(allowOrigin);
        allowMethodsString = joiner.join(allowMethods);
        allowHeadersString = joiner.join(allowHeaders);
    }


    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();

        headers.add("Access-Control-Allow-Origin", allowOriginString);
        headers.add("Access-Control-Allow-Methods", allowMethodsString);
        headers.add("Access-Control-Allow-Headers", allowHeadersString);
    }
}

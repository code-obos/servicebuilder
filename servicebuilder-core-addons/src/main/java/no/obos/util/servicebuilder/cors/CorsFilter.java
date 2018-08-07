package no.obos.util.servicebuilder.cors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CorsFilter implements Filter {

    public static final String ORIGIN = "Origin";
    public static final String REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.addHeader(ALLOW_ORIGIN, httpRequest.getHeader(ORIGIN));
            httpResponse.addHeader(ALLOW_METHODS, httpRequest.getHeader(REQUEST_METHOD));
            httpResponse.addHeader(ALLOW_HEADERS, httpRequest.getHeader(REQUEST_HEADERS));
            httpResponse.addHeader(ALLOW_CREDENTIALS, "true");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

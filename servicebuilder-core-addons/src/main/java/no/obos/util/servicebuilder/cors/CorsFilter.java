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

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
import static com.google.common.net.HttpHeaders.ORIGIN;

public class CorsFilter implements Filter {

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
            httpResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, httpRequest.getHeader(ORIGIN));
            httpResponse.addHeader(ACCESS_CONTROL_ALLOW_METHODS, httpRequest.getHeader(ACCESS_CONTROL_REQUEST_METHOD));
            httpResponse.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, httpRequest.getHeader(ACCESS_CONTROL_REQUEST_HEADERS));
            httpResponse.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

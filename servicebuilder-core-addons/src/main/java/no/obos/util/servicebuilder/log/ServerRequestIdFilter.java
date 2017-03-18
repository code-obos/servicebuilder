package no.obos.util.servicebuilder.log;

import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

/**
 * Dette filteret logger servlet-requester som blir godtatt av Condition.accept. I tillegg legges X-OBOS-REQUEST-ID
 * i MDC slik at den kan logges ved å legge til %X{X-OBOS-REQUEST-ID} i logback.xml.
 */
public class ServerRequestIdFilter implements Filter {
    public static final String X_OBOS_REQUEST_ID = "X-OBOS-REQUEST-ID";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String requestId = servletRequest.getHeader(X_OBOS_REQUEST_ID);

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        try {

            MDC.put(X_OBOS_REQUEST_ID, requestId);

            chain.doFilter(request, response);

        } finally {
            MDC.remove(X_OBOS_REQUEST_ID);
        }
    }

    @Override
    public void destroy() {
    }
}

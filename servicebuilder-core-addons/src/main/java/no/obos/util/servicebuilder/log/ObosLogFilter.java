package no.obos.util.servicebuilder.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Dette filteret logger servlet-requester som blir godtatt av Condition.accept. I tillegg legges X-OBOS-REQUEST-ID
 * i MDC slik at den kan logges ved å legge til %X{X-OBOS-REQUEST-ID} i logback.xml.
 */
public class ObosLogFilter implements Filter {
    public static final String X_OBOS_REQUEST_ID = "X-OBOS-REQUEST-ID";
    private final static Logger logger = LoggerFactory.getLogger(ObosLogFilter.class);
    private Condition condition;

    public ObosLogFilter(Condition condition) {
        this.condition = condition;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String method = servletRequest.getMethod();
        String pathInfo = servletRequest.getPathInfo();
        String requestId = servletRequest.getHeader(X_OBOS_REQUEST_ID);
        
        if (accept(pathInfo)) {
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            try {

                MDC.put(X_OBOS_REQUEST_ID, requestId);

                logger.info("Kaller ({}) {} med request-ID {}", method, pathInfo, requestId);
                long t1 = System.currentTimeMillis();
                chain.doFilter(request, response);
                long t2 = System.currentTimeMillis();
                logger.info("Retur fra ({}) {}. Tid brukt {}ms. Request-ID {}", method, pathInfo, (t2 - t1), requestId);

            } finally {
                MDC.remove(X_OBOS_REQUEST_ID);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean accept(String path) {
        List<String> metricsPaths = Arrays.asList("/threads", "/version", "/ping", "/healthcheck", "/metrics", "/");
        if (metricsPaths.contains(path)) {
            return false;
        } else {
            return condition.accept(path);
        }
    }

    @Override
    public void destroy() {
    }

    public interface Condition {
        public boolean accept(String path);
    }
}

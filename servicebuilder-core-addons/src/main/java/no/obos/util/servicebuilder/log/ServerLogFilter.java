package no.obos.util.servicebuilder.log;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.log.model.LogParams;
import no.obos.util.servicebuilder.log.model.LogRequest;
import no.obos.util.servicebuilder.log.model.LogResponse;
import no.obos.util.servicebuilder.model.Constants;
import no.obos.util.servicebuilder.model.UibBruker;
import no.obos.util.servicebuilder.util.FormatUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServerLogFilter implements ContainerRequestFilter, ContainerResponseFilter {
    public static final String PROPERTYNAME = "RestLogFilter.startTime";

    final ResourceInfo resourceInfo;

    final ServerLogger serverLogger;

    final Provider<UibBruker> uibBrukerProvider;

    final static int MAX_ENTITY_READ = 4096;

    @Inject
    public ServerLogFilter(@Context ResourceInfo resourceInfo, ServerLogger serverLogger, Provider<UibBruker> uibBrukerProvider) {
        this.resourceInfo = resourceInfo;
        this.serverLogger = serverLogger;
        this.uibBrukerProvider = uibBrukerProvider;
    }

    static String extractRequestEntity(ContainerRequestContext request) {
        if (request.hasEntity()) {
            InputStream inputStreamOriginal = request.getEntityStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamOriginal, MAX_ENTITY_READ);
            bufferedInputStream.mark(MAX_ENTITY_READ);
            byte[] bytes = new byte[MAX_ENTITY_READ];
            int read;
            try {
                read = bufferedInputStream.read(bytes, 0, MAX_ENTITY_READ);
                bufferedInputStream.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            request.setEntityStream(bufferedInputStream);

            return new String(bytes, Charsets.UTF_8);
        }
        return null;
    }


    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        if (serverLogger.fastTrackFilters.stream().anyMatch(it -> it.test(request))) {
            return;
        }

        Class<?> handlingClass = resourceInfo.getResourceClass();
        Method handlingMethod = resourceInfo.getResourceMethod();

        LogParams logParams = serverLogger.LogParamsForCall(handlingClass, handlingMethod);

        if (! logParams.enableLogging || logParams.logOnlyResponse) {
            return;
        }

        request.setProperty(PROPERTYNAME, System.nanoTime());

        LogRequest.LogRequestBuilder logRequest = LogRequest.builder();

        logRequest.uri(getUri(request));

        UibBruker uibBruker = uibBrukerProvider.get();
        if (uibBruker != null) {
            List<String> bruker = Lists.newArrayList();
            bruker.add(uibBruker.fornavn + " " + uibBruker.etternavn);
            if (! Strings.isNullOrEmpty(uibBruker.adBrukernavn)) {
                bruker.add(uibBruker.adBrukernavn);
            }
            if (! Strings.isNullOrEmpty(uibBruker.personid)) {
                bruker.add(uibBruker.personid);
            }
            logRequest.user(Joiner.on(", ").join(bruker));
        }


        String headerString = request.getHeaderString(Constants.CLIENT_APPNAME_HEADER);
        if (Strings.isNullOrEmpty(headerString)) {
            logRequest.clientApplication(headerString);
        }

        if (logParams.logHeaders) {
            Map<String, String> headers = FormatUtil.MultiMapAsStringMap(request.getHeaders());
            logRequest.headers((ImmutableMap.copyOf(headers)));
        }

        if (logParams.logRequestPayload) {
            logRequest.entity(extractRequestEntity(request));
        }

        serverLogger.handleRequest(logRequest.build(), logParams);
    }

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response)
            throws IOException
    {
        if (serverLogger.fastTrackFilters.stream().anyMatch(it -> it.test(request))) {
            return;
        }

        Class<?> handlingClass = resourceInfo.getResourceClass();
        Method handlingMethod = resourceInfo.getResourceMethod();
        LogParams logParams = serverLogger.LogParamsForCall(handlingClass, handlingMethod);

        if (! logParams.enableLogging) {
            return;
        }

        LogResponse.LogResponseBuilder logResponse = LogResponse.builder();

        logResponse.uri(getUri(request));
        logResponse.status(response.getStatus());

        if (logParams.logHeaders) {
            Map<String, String> headers = FormatUtil.MultiMapAsStringMap(response.getStringHeaders());
            logResponse.headers((ImmutableMap.copyOf(headers)));
        }

        if (logParams.logResponseEntity) {
            if (response.hasEntity()) {
                logResponse.entity(response.getEntity());
            }
        }
        Long totalMillis = null;

        Long startNanos = (Long) request.getProperty(PROPERTYNAME);
        if (startNanos != null) {
            long totalNanos = System.nanoTime() - startNanos;
            totalMillis = totalNanos / 1_000_000;
        }
        logResponse.totalMillis(totalMillis);

        serverLogger.handleResponse(logResponse.build(), logParams);
    }

    private String getUri(ContainerRequestContext request) {
        String queryParamString = request.getUriInfo().getRequestUri().getQuery();

        String query = Strings.isNullOrEmpty(queryParamString) ? "" : "?" + queryParamString;
        return request.getMethod() + " " + request.getUriInfo().getPath() + query;
    }

}

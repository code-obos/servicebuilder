package no.obos.util.servicebuilder.log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.log.model.LogHeader;
import no.obos.util.servicebuilder.log.model.LogParams;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j class JerseyLogUtil {
    static Set<LogHeader> logHeaders(MultivaluedMap<String, String> headers, LogParams logParams) {
        if (headers != null) {
            return headers.entrySet().stream()
                    .map(entry -> {
                                String headerName = entry.getKey();
                                List<String> headerValues = entry.getValue();
                                headerValues = headerValues != null ? headerValues : Lists.newArrayList();
                                return new LogHeader(headerName, ImmutableList.copyOf(headerValues));
                            }
                    ).collect(Collectors.toSet());
        } else {
            return Sets.newHashSet();
        }
    }

    static String extractRequestEntity(ContainerRequestContext request) {
        String eventString = null;
        if (request.hasEntity()) {
            InputStream inputStream = request.getEntityStream();
            StringWriter writer = new StringWriter();
            try {
                IOUtils.copy(inputStream, writer);
                eventString = writer.toString();
            } catch (IOException e) {
                log.warn("Error during extracting entity of call", e);
            }
        }
        return eventString;
    }
}

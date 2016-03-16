package no.obos.util.servicebuilder.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ContextAwareExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    @Context
    protected HttpHeaders headers;
    private Map<MediaType, String> mediaTypeMap;

    public ContextAwareExceptionMapper() {
        mediaTypeMap = Maps.newHashMap();
        mediaTypeMap.put(MediaType.APPLICATION_JSON_TYPE, ExceptionUtil.APPLICATION_PROBLEM_JSON);
        mediaTypeMap.put(MediaType.APPLICATION_XML_TYPE, ExceptionUtil.APPLICATION_PROBLEM_XML);
    }

    /**
     * Gets the mediaType to use based on the clients Accept-header
     *
     * @return MediaType as String, default {@literal APPLICATION_PROBLEM_JSON}
     */
    protected String getMediaType() {
        List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
        Optional<String> firstAcceptableMediaType = acceptableMediaTypes.stream().map(mt -> mediaTypeMap.get(mt)).filter(mt -> mt != null).findFirst();
        if (firstAcceptableMediaType.isPresent()) {
            return firstAcceptableMediaType.get();
        } else {
            return ExceptionUtil.APPLICATION_PROBLEM_JSON;
        }
    }
}

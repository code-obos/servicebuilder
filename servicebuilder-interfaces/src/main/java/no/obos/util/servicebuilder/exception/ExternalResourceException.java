package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import no.obos.util.servicebuilder.model.ProblemResponse;

import javax.ws.rs.WebApplicationException;

@Getter
public class ExternalResourceException extends WebApplicationException {
    private final MetaData metaData;

    public ExternalResourceException(MetaData metaData, Exception ex) {
        super("Feil ved kall til ekstern ressurs.", ex);
        this.metaData = metaData;
    }

    public ExternalResourceException(MetaData metaData) {
        super("Feil ved kall til ekstern ressurs. ");
        this.metaData = metaData;
    }



    @Builder(toBuilder = true)
    @ToString
    public static class MetaData {
        public final String targetName;
        public final Boolean gotAnswer;
        public final HttpRequestMetaData httpRequestMetaData;
        public final HttpResponseMetaData httpResponseMetaData;
    }


    @Builder(toBuilder = true)
    @ToString
    public static class HttpRequestMetaData {
        String url;
        @Singular
        ImmutableMap<String, String> headers;
    }


    @Builder(toBuilder = true)
    @ToString
    public static class HttpResponseMetaData {
        @Singular
        public final ImmutableMap<String, String> headers;
        public final int status;
        public final ProblemResponse problemResponse;
        public final String response;
        public final String incidentReferenceId;
    }

}

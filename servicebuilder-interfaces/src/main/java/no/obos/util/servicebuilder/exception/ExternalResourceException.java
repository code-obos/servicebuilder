package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;

public class ExternalResourceException extends RuntimeException {
    public final MetaData metaData;

    public ExternalResourceException(MetaData metaData, Exception ex) {
        super("Feil ved kall til ekstern ressurs. " + metaData.toString(), ex);
        this.metaData = metaData;
    }



    @Builder(toBuilder = true)
    @ToString
    public static class MetaData {
        public final String targetName;
        public final Boolean gotAnswer;
        public final String targetUrl;
        public final Integer httpStatus;
        @Singular("context")
        public final ImmutableMap<String, String> context;
    }
}

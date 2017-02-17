package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import no.obos.util.servicebuilder.ProblemResponse;

import javax.ws.rs.WebApplicationException;

@Getter
public class ExternalResourceException extends WebApplicationException {
    private final MetaData metaData;

    public ExternalResourceException(MetaData metaData, Exception ex) {
        super("Feil ved kall til ekstern ressurs. " + metaData.toString(), ex);
        this.metaData = metaData;
    }

    public ExternalResourceException(MetaData metaData) {
        super("Feil ved kall til ekstern ressurs. " + metaData.toString());
        this.metaData = metaData;
    }



    @Builder(toBuilder = true)
    @ToString
    public static class MetaData {
        public final String targetName;
        public final Boolean gotAnswer;
        public final String targetUrl;
        public final Integer httpStatus;
        public final String incidentReferenceId;
        public final ProblemResponse nestedProblemResponce;
        @Singular("context")
        public final ImmutableMap<String, String> context;
    }
}

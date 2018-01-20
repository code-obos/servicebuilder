package no.obos.util.servicebuilder.exception;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.Version;

import javax.ws.rs.WebApplicationException;
import java.util.List;

@Getter
public class ExternalResourceException extends WebApplicationException {
    private final MetaData metaData;

    public ExternalResourceException(MetaData metaData, Exception ex) {
        super(getMetaDataSummary(metaData), ex);
        this.metaData = metaData;
    }

    public ExternalResourceException(MetaData metaData) {
        super(getMetaDataSummary(metaData));
        this.metaData = metaData;
    }

    private static String getMetaDataSummary(MetaData metaData) {
        String ret = "";
        ret = ret + "Target: " + metaData.targetName;
        if (metaData.httpResponseMetaData != null) {
            ret += ", Status: " + metaData.httpResponseMetaData.status;
            if (metaData.httpResponseMetaData.httpProblem != null) {
                ret += ", Detail: " + metaData.httpResponseMetaData.httpProblem.detail;
            } else if (metaData.httpResponseMetaData.response != null) {
                ret += ", Response: " + responseAsSingleLine(metaData.httpResponseMetaData.response);
            }
        }
        return ret;
    }


    private static String responseAsSingleLine(String response) {
        List<String> lines = Splitter.on('\n').splitToList(response);
        return Joiner.on(' ').join(lines);
    }


    @Builder(toBuilder = true)
    @ToString
    public static class MetaData {
        public final String targetName;
        public final Version targetVersion;
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
        public final HttpProblem httpProblem;
        public final String response;
        public final String incidentReferenceId;
    }

}

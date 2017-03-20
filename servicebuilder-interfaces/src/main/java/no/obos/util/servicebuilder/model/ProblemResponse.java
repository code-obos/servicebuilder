package no.obos.util.servicebuilder.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
@ToString
@EqualsAndHashCode(of = "incidentReferenceId")
@ApiModel(value = "ProblemResponse", description = "Returneres ved feil. Ment for programmerere. Bør ikke eksponeres til brukere")
public class ProblemResponse {
    public String type;
    public String title;
    public String detail;
    public int status;
    public String incidentReferenceId;
    public boolean suggestedUserMessageInDetail;
    public Map<String, String> context;

    /**
     * JAXB default constructor
     */
    @SuppressWarnings("unused")
    private ProblemResponse() {}

    @JsonCreator
    @Builder(toBuilder = true)
    public ProblemResponse(
            @JsonProperty("title") String title,
            @JsonProperty("detail") String detail,
            @JsonProperty("status") int status,
            @JsonProperty("incidentReferenceId") String incidentReferenceId,
            @JsonProperty("suggestedUserMessageInDetail") boolean suggestedUserMessageInDetail,
            @JsonProperty("type") String type,
            @Singular("context") @JsonProperty("context") Map<String, String> context)
    {
        this.title = title;
        this.detail = detail;
        this.status = status;
        this.incidentReferenceId = incidentReferenceId;
        this.suggestedUserMessageInDetail = suggestedUserMessageInDetail;
        this.type = type;
        this.context = context;
    }
}

package no.obos.util.servicebuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@ToString
@EqualsAndHashCode(of="incidentReferenceId")
@ApiModel(value="ProblemResponse", description="Returneres ved feil. Ment for programmerere. Bør ikke eksponeres til brukere")
public class ProblemResponse {
    public String title;
    public String detail;
    public int status;
    public String incidentReferenceId;

    /**
     * JAXB default constructor
     */
    @SuppressWarnings("unused")
    private ProblemResponse() {}

    @JsonCreator
    public ProblemResponse(@JsonProperty("title") String title, @JsonProperty("detail") String detail, @JsonProperty("status") int status, @JsonProperty("incidentReferenceId") String incidentReferenceId) {
        this.title = title;
        this.detail = detail;
        this.status = status;
        this.incidentReferenceId = incidentReferenceId;
    }
}

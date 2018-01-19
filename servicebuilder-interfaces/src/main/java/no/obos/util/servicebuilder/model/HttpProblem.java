package no.obos.util.servicebuilder.model;

import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
@AllArgsConstructor
@XmlRootElement
@ToString
@EqualsAndHashCode(of = "incidentReferenceId")
@ApiModel(value = "HttpProblem", description = "Based on RFC7807 Problem Details for HTTP APIs")
public class HttpProblem {
    public final String type;
    public final String title;
    public final String detail;
    public final int status;
    public final String incidentReferenceId;
    public final boolean suggestedUserMessageInDetail;
    @Singular("context")
    private final Map<String, String> context;

    public Map<String, String> getContext() {
        return new HashMap<>(context);
    }
}

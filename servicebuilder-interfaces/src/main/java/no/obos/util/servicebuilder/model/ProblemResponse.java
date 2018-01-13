package no.obos.util.servicebuilder.model;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;

@Builder(toBuilder = true)
@AllArgsConstructor
@XmlRootElement
@ToString
@EqualsAndHashCode(of = "incidentReferenceId")
@ApiModel(value = "ProblemResponse", description = "Based on RFC7807 Problem Details for HTTP APIs")
public class ProblemResponse {
    public final String type;
    public final String title;
    public final String detail;
    public final int status;
    public final String incidentReferenceId;
    public final boolean suggestedUserMessageInDetail;
    @Singular("context")
    public final ImmutableMap<String, String> context;
}

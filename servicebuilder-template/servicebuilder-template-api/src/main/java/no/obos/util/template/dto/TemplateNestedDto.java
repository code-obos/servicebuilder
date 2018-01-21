package no.obos.util.template.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@ApiModel
public class TemplateNestedDto {
    @ApiModelProperty(example = "4.2")
    public final double value;
}

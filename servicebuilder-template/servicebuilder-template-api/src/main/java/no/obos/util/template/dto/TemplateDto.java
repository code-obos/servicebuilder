package no.obos.util.template.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@ApiModel
public class TemplateDto {
    @ApiModelProperty(example = "42")
    public final int id;
    @ApiModelProperty(example = "Banana")
    public final String string;
    @ApiModelProperty(example = "2000-02-29")
    public final LocalDate date;
    public final TemplateNestedDto nested;
}

package no.obos.util.template.dto;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@ApiModel
public class TemplateDto {
    @ApiModelProperty(example = "42")
    public final Integer id;
    @Size(min = 4, max = 20)
    @NotNull
    @ApiModelProperty(example = "Banana")
    public final String name;
    @ApiModelProperty(example = "2000-02-29")
    public final LocalDate startDate;
    public final TemplateNestedDto nested;
}

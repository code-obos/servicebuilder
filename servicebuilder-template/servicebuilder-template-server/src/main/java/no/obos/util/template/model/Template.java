package no.obos.util.template.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateNestedDto;

import java.time.LocalDate;

@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Template {
    public final Integer id;
    public final String name;
    public final LocalDate startDate;
    public final double value;

    public TemplateDto toDto() {
        return TemplateDto.builder()
                .id(id)
                .name(name)
                .nested(TemplateNestedDto.builder()
                        .value(value)
                        .build()
                )
                .startDate(startDate)
                .build();
    }

    public static Template ofDto(TemplateDto dto) {
        return builder()
                .id(dto.id)
                .value(dto.nested.value)
                .startDate(dto.startDate)
                .name(dto.name)
                .build();
    }
}

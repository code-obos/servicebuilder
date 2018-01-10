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
    public final String string;
    public final LocalDate date;
    public final double aDouble;

    public TemplateDto toDto() {
        return TemplateDto.builder()
                .id(id)
                .string(string)
                .nested(TemplateNestedDto.builder()
                        .aDouble(aDouble)
                        .build()
                )
                .date(date)
                .build();
    }

    public static Template ofDto(TemplateDto dto) {
        return builder()
                .id(dto.id)
                .aDouble(dto.nested.aDouble)
                .date(dto.date)
                .string(dto.string)
                .build();
    }
}

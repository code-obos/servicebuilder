package no.obos.util.template.dto;

import com.github.javafaker.Faker;

import java.util.Random;

public class TemplateNestedDtoTypes {

    static TemplateNestedDto small(Random random) {
        Faker faker = FakerUtil.getFaker(random);
        return TemplateNestedDto.builder()
                .value(faker.number().randomDouble(4, 0, 4))
                .build();
    }

    static TemplateNestedDto bigly(Random random) {
        Faker faker = FakerUtil.getFaker(random);
        return TemplateNestedDto.builder()
                .value(faker.number().randomDouble(4, 8000, 9000))
                .build();
    }
}

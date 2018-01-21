package no.obos.util.template.dto;


import com.github.javafaker.Faker;

import java.util.Random;

public class TemplateDtoTypes {


    public static TemplateDto early(Random random) {
        Faker faker = FakerUtil.getFaker(random);
        return TemplateDto.builder()
                .startDate(FakerUtil.dateBetween(faker, "2014-01-02", "2017-05-07"))
                .name(faker.name().fullName())
                .id(faker.number().numberBetween(0, 5000000))
                .nested(TemplateNestedDtoTypes.bigly(random))
                .build();
    }

    public static TemplateDto late(Random random) {
        Faker faker = FakerUtil.getFaker(random);
        return TemplateDto.builder()
                .startDate(FakerUtil.dateBetween(faker, "2018-01-02", "2020-05-07"))
                .name(faker.name().fullName())
                .id(faker.number().numberBetween(0, 5000000))
                .nested(TemplateNestedDtoTypes.small(random))
                .build();
    }

}

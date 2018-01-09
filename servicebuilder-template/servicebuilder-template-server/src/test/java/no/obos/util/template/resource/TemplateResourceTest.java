package no.obos.util.template.resource;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.template.Main;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateNestedDto;
import no.obos.util.template.resources.TemplateResource;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateResourceTest {
    ServiceConfig config = Main.common;
    TestServiceRunner runner = TestServiceRunner.defaults(config);

    @Test
    public void insertAndGet() {
        TemplateDto original = TemplateDto.builder()
                .string("Banan")
                .nested(TemplateNestedDto.builder().aDouble(11.3).build())
                .build();
        runner.chain()
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .isEmpty()
                )
                .call(TemplateResource.class, resource -> resource.createTemplate(original))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .hasSize(1)
                )
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getTemplate(1))
                                .isEqualTo(original.toBuilder().id(1).build())
                )
                .run()
        ;
    }

}

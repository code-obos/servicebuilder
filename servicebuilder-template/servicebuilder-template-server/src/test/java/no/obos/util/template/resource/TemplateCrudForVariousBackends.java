package no.obos.util.template.resource;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.exception.ExternalResourceNotFoundException;
import no.obos.util.template.Main;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateNestedDto;
import no.obos.util.template.resources.TemplateResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.assertj.core.api.Assertions.*;

@RunWith(Parameterized.class)
public class TemplateCrudForVariousBackends {
    final ServiceConfig config;
    final TestServiceRunner runner;

    public TemplateCrudForVariousBackends(ServiceConfig config) {
        this.config = config;
        runner = TestServiceRunner.defaults(config);
    }

    @Parameterized.Parameters
    public static Collection<ServiceConfig> data() {
        return Lists.newArrayList(
                Main.inMemoryConfig,
                Main.jdbiConfig
                        .addon(Addons.h2InMemoryDatasource()
                                .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, string VARCHAR, double DOUBLE, date DATE)")
                        )
        );
    }

    public static final TemplateDto ORIGINAL = TemplateDto.builder()
            .string("Banan")
            .nested(TemplateNestedDto.builder().aDouble(11.3).build())
            .build();

    @Test
    public void create() {
        runner.chain()
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .isEmpty()
                )
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .hasSize(1)
                )
                .run()
        ;
    }

    @Test
    public void retreive() {
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getTemplate(1))
                                .isEqualTo(ORIGINAL.toBuilder().id(1).build())
                )
                .call(TemplateResource.class, resource ->
                        assertThatThrownBy(() -> resource.getTemplate(42))
                                .isExactlyInstanceOf(ExternalResourceNotFoundException.class)
                                .hasMessage("Target: template, Status: 404, Detail: No Template for id 42")

                )
                .run()
        ;
    }


    @Test
    public void update() {
        TemplateDto[] mutated = new TemplateDto[1];
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource -> {
                            TemplateDto template = resource.getTemplate(1);
                            mutated[0] = template.toBuilder()
                                    .string("Eple")
                                    .date(null)
                                    .build();
                            resource.updateTemplate(1, mutated[0]);
                        }
                )
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getTemplate(1))
                                .isEqualTo(mutated[0])
                )
                .call(TemplateResource.class, resource ->
                        assertThatThrownBy(() -> resource.updateTemplate(42, mutated[0]))
                                .isExactlyInstanceOf(ExternalResourceNotFoundException.class)
                                .hasMessage("Target: template, Status: 404, Detail: No Template for id 42")
                )
                .run()
        ;
    }

    @Test
    public void delete() {
        runner.chain()
                .call(TemplateResource.class, resource -> resource.createTemplate(ORIGINAL))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .hasSize(1)
                )
                .call(TemplateResource.class, resource -> resource.delete(1))
                .call(TemplateResource.class, resource ->
                        assertThat(resource.getAllTemplates())
                                .isEmpty()
                )
                .call(TemplateResource.class, resource ->
                        assertThatCode(() -> resource.delete(42))
                                .doesNotThrowAnyException()
                )
                .run()
        ;
    }
}

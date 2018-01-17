package no.obos.util.template.resource;

import com.google.common.collect.Lists;
import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.exception.ExternalResourceNotFoundException;
import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.controllers.TemplateControllerElasticsearch;
import no.obos.util.template.controllers.TemplateControllerInMemory;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateNestedDto;
import no.obos.util.template.model.Template;
import no.obos.util.template.resources.TemplateResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.inject.Singleton;
import java.util.Collection;

import static no.obos.util.template.Main.commonConfig;
import static no.obos.util.template.Main.mainConfig;
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
                commonConfig
                        .bind(binder -> binder.bind(TemplateControllerInMemory.class).to(TemplateController.class).in(Singleton.class))
                , mainConfig
                , commonConfig
                        .bind(binder -> binder.bind(TemplateControllerElasticsearch.class).to(TemplateController.class).in(Singleton.class))
                        .addon(Addons.elasticsearchMock())
                        .addon(Addons.elasticsearchIndex("templateIndex", Template.class)
                                .doIndexing(true)
                        )
        );
    }


    public static final TemplateDto ORIGINAL = TemplateDto.builder()
            .name("Banan")
            .nested(TemplateNestedDto.builder().value(11.3).build())
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
                                    .name("Eple")
                                    .startDate(null)
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

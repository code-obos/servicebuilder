package no.obos.util.template.resource;

import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.DataSourceAddon;
import no.obos.util.template.Main;
import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateDtoTypes;
import no.obos.util.template.model.Template;
import no.obos.util.template.resources.TemplateResource;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Random;

import static java.util.Collections.singletonList;
import static no.obos.util.servicebuilder.TestServiceRunner.testServiceRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class TemplateResourceUnitTest {
    final ServiceConfig config = Main.commonConfig
            .removeAddon(DataSourceAddon.class)
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            );

    @Test
    public void insert() {
        //Given
        TemplateDto expectedDto = TemplateDtoTypes.early(new Random(400));
        Template expected = Template.ofDto(expectedDto);
        TemplateController controller = Mockito.mock(TemplateController.class);
        TestServiceRunner runner = testServiceRunner(config
                .bind(controller, TemplateController.class)
        );


        //When
        when(controller.getAll()).thenReturn(singletonList(expected));
        List<TemplateDto> actual = runner.oneShot(TemplateResource.class, TemplateResource::getAllTemplates);

        //Then
        assertThat(actual).isEqualTo(singletonList(expectedDto));
    }
}

package no.obos.util.template.resource;

import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.DataSourceAddon;
import no.obos.util.template.Main;
import no.obos.util.template.db.dao.TemplateDao;
import no.obos.util.template.db.model.TemplateDb;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.dto.TemplateDtoTypes;
import no.obos.util.template.model.Template;
import no.obos.util.template.resources.TemplateResource;
import org.junit.Test;

import java.util.Random;

import static no.obos.util.servicebuilder.TestServiceRunner.testServiceRunner;
import static org.assertj.core.api.Assertions.assertThat;

public class TemplateCrudJdbiIntegrationTest {

    final ServiceConfig config = Main.mainConfig
            .removeAddon(DataSourceAddon.class)
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            );

    final TestServiceRunner runner = testServiceRunner(config);

    static final TemplateDto original = TemplateDtoTypes.late(new Random(350));

    @Test
    public void create() {
        int[] id = new int[1];
        runner.chain()
                .call(TemplateResource.class, resource -> id[0] = resource.createTemplate(original))
                .injectee(TemplateDao.class, dao -> {
                    TemplateDto expectedDto = original.toBuilder().id(id[0]).build();
                    TemplateDb expected = TemplateDb.ofModel(Template.ofDto(expectedDto));

                    TemplateDb actal = dao.select(id[0]);
                    assertThat(actal).isEqualTo(expected);
                })
                .run();
    }
}

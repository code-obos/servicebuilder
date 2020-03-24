package no.obos.util.template.resources;

import cucumber.api.java8.En;
import no.obos.util.template.dto.TemplateDtoTypes;

import java.util.Random;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TemplateResourceSteps implements En {
    Consumer<TemplateResource> world;
    TemplateResource templateResource = mock(TemplateResource.class);

    public TemplateResourceSteps() {
        When("^Retreiving template (\\d+) gives early template with seed (\\d+)", (Integer id, Integer seed) -> {
            when(templateResource.getTemplate(id)).thenReturn(TemplateDtoTypes.early(new Random(seed)));
            world.accept(templateResource);
        });
    }
}

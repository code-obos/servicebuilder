package no.obos.util.template.resources;

import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.dto.TemplateDto;
import no.obos.util.template.model.Template;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class TemplateResourceImpl implements TemplateResource {
    @Inject
    TemplateController controller;

    @Override
    public List<TemplateDto> getAllTemplates() {
        return controller.getAll().stream().map(Template::toDto).collect(toList());
    }

    @Override
    public TemplateDto getTemplate(int id) {
        return controller.get(id).toDto();
    }

    @Override
    public int createTemplate(TemplateDto payload) {
        return controller.create(Template.ofDto(payload)).id;

    }

    @Override
    public void updateTemplate(int id, TemplateDto payload) {
        controller.update(id, Template.ofDto(payload));
    }

    @Override
    public void delete(int i) {
        controller.delete(i);
    }
}

package no.obos.util.template.controllers;

import no.obos.util.template.db.dao.TemplateDao;
import no.obos.util.template.db.model.TemplateDb;
import no.obos.util.template.model.Template;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class TemplateControllerJdbi implements TemplateController {
    @Inject
    TemplateDao dao;

    @Override
    public List<Template> getAll() {
        return dao.selectAll().stream()
                .map(TemplateDb::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Template get(int id) {
        TemplateDb found = dao.select(id);
        if (found == null) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        return found.toModel();
    }

    @Override
    public Template create(Template payload) {
        Template withoutId = payload.toBuilder().id(null).build();
        int id = dao.insert(TemplateDb.ofModel(withoutId));
        return dao.select(id).toModel();
    }

    @Override
    public void update(int id, Template payload) {
        if (dao.select(id) == null) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        Template withoutId = payload.toBuilder().id(null).build();
        dao.update(id, TemplateDb.ofModel(withoutId));
    }

    @Override
    public void delete(int i) {
        dao.delete(i);
    }
}

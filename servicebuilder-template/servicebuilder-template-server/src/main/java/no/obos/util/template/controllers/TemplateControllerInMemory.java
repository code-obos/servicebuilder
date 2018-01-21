package no.obos.util.template.controllers;

import com.google.common.collect.Lists;
import no.obos.util.template.model.Template;

import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateControllerInMemory implements TemplateController {
    private int nextId = 1;
    private Map<Integer, Template> templates = new HashMap<>();

    @Override
    public List<Template> getAll() {
        return Lists.newArrayList(templates.values());
    }

    @Override
    public Template get(int id) {
        Template found = templates.get(id);
        if (found == null) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        return found;
    }

    @Override
    public Template create(Template payload) {
        int id = nextId++;
        Template withId = payload.toBuilder().id(id).build();
        templates.put(id, withId);
        return withId;
    }

    @Override
    public void update(int id, Template payload) {
        if (templates.get(id) == null) {
            throw new NotFoundException("No " + Template.class.getSimpleName() + " for id " + id);
        }
        Template withId = payload.toBuilder().id(id).build();
        templates.put(id, withId);
    }

    @Override
    public void delete(int i) {
        templates.remove(i);
    }
}

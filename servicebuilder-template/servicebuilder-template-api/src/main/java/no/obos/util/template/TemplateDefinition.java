package no.obos.util.template;


import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.model.Version;
import no.obos.util.template.resources.TemplateResource;

import java.util.Collections;

public class TemplateDefinition implements ServiceDefinition {
    public static final String NAME = "template";
    public static final Version VERSION = new Version(1, 0, 0);
    public static final Iterable<Class> RESOURCES = Collections.singletonList(TemplateResource.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getVersion() {
        return VERSION;
    }

    @Override
    public Iterable<Class> getResources() {
        return RESOURCES;
    }

    public static final TemplateDefinition instance = new TemplateDefinition();
}

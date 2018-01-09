package no.obos.util.template;

import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceRunner;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.controllers.TemplateControllerInMemory;
import no.obos.util.template.resources.TemplateResource;
import no.obos.util.template.resources.TemplateResourceImpl;

public class Main {
    public final static ServiceConfig common = Addons.standardAddons(TemplateDefinition.instance)
            .addon(WebAppAddon.defaults)
            .bind(TemplateResourceImpl.class, TemplateResource.class);

    public final static ServiceConfig inMemoryconfig = common
            .bind(TemplateControllerInMemory.class, TemplateController.class);

    public final static ServiceRunner serviceRunner = ServiceRunner.defaults(inMemoryconfig);

    public static void main(String[] args) {
        serviceRunner.start().join();
    }

}

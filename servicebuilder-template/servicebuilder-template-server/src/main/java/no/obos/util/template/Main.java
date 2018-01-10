package no.obos.util.template;

import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceRunner;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.controllers.TemplateControllerInMemory;
import no.obos.util.template.controllers.TemplateControllerJdbi;
import no.obos.util.template.db.dao.TemplateDao;
import no.obos.util.template.resources.TemplateResource;
import no.obos.util.template.resources.TemplateResourceImpl;

import javax.inject.Singleton;

public class Main {
    public final static ServiceConfig common = Addons.standardAddons(TemplateDefinition.instance)
            .addon(WebAppAddon.defaults)
            .bind(TemplateResourceImpl.class, TemplateResource.class);

    public final static ServiceConfig inMemoryConfig = common
            .bind(binder -> binder.bind(TemplateControllerInMemory.class).to(TemplateController.class).in(Singleton.class));

    public final static ServiceConfig jdbiConfig = common
            .addon(Addons.jdbi()
                    .dao(TemplateDao.class)
            )
            .bind(TemplateControllerJdbi.class, TemplateController.class);

    public static void main(String[] args) {
        ServiceRunner.defaults(inMemoryConfig).start().join();
    }

}

package no.obos.util.template;

import no.obos.util.servicebuilder.Addons;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceRunner;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import no.obos.util.template.controllers.TemplateController;
import no.obos.util.template.controllers.TemplateControllerJdbi;
import no.obos.util.template.db.dao.TemplateDao;
import no.obos.util.template.resources.TemplateResource;
import no.obos.util.template.resources.TemplateResourceImpl;

import static no.obos.util.servicebuilder.addon.WebAppAddon.webAppAddon;

public class Main {
    public final static ServiceConfig commonConfig = Addons.standardAddons(TemplateDefinition.instance)
            .addon(webAppAddon)
            .bind(TemplateResourceImpl.class, TemplateResource.class);


    public final static ServiceConfig mainConfig = commonConfig
            .bind(TemplateControllerJdbi.class, TemplateController.class)
            .addon(Addons.h2InMemoryDatasource()
                    .script("CREATE TABLE template (id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR, value DOUBLE, startDate DATE)")
            )
            .addon(Addons.jdbi()
                    .dao(TemplateDao.class)
            );


    public static void main(String[] args) {
        ServiceRunner.defaults(mainConfig).start().join();
    }

}

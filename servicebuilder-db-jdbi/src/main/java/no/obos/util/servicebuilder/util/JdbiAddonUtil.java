package no.obos.util.servicebuilder.util;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.addon.DataSourceAddon;
import no.obos.util.servicebuilder.addon.JdbiAddon;
import no.obos.util.servicebuilder.exception.DependenceException;

import javax.sql.DataSource;


public abstract class JdbiAddonUtil {

    public static DataSource getDataSource(JdbiAddon jdbiAddon, ServiceConfig serviceConfig, String name) {
        DataSourceAddon dataSourceAddon = serviceConfig.addonInstanceNamed(DataSourceAddon.class, name);
        if (dataSourceAddon == null) {
            if (name == null) {
                throw new DependenceException(jdbiAddon.getClass(), DataSourceAddon.class, " no unnamed datasourceaddon found");
            } else {
                throw new DependenceException(jdbiAddon.getClass(), DataSourceAddon.class, " no datasourceaddon for name " + name);
            }
        }
        return dataSourceAddon.getDataSource();
    }

}

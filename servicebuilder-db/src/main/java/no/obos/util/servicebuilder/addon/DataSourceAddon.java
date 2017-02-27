package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.addon.NamedAddon;

import javax.sql.DataSource;

public interface DataSourceAddon extends NamedAddon {
    String getName();

    DataSource getDataSource();
}

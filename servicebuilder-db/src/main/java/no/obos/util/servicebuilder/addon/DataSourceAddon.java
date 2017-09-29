package no.obos.util.servicebuilder.addon;

import javax.sql.DataSource;

public interface DataSourceAddon extends NamedAddon {
    String getName();

    DataSource getDataSource();
}

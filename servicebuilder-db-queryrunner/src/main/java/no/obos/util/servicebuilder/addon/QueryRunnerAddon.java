package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.queryrunner.QueryRunnerAdapter;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryRunnerAddon implements Addon {
    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    QueryRunner queryRunner;

    public static final QueryRunnerAddon queryRunnerAddon =
            new QueryRunnerAddon(null, null);

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        DataSourceAddon dataSourceAddon = serviceConfig.addonInstanceNamed(DataSourceAddon.class, name);
        if (dataSourceAddon == null) {
            if (name == null) {
                throw new DependenceException(this.getClass(), DataSourceAddon.class);
            } else {
                throw new DependenceException(this.getClass(), DataSourceAddon.class, " no datasourceaddon for name " + name);
            }
        }
        DataSource dataSource = dataSourceAddon.getDataSource();
        return this.withQueryRunner(new QueryRunner(dataSource));
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
            if (name != null) {

                binder.bind(queryRunner).to(QueryRunner.class).named(name);
                binder.bindAsContract(QueryRunnerAdapter.class).named(name);
            } else {
                binder.bind(queryRunner).to(QueryRunner.class);
                binder.bindAsContract(QueryRunnerAdapter.class);
            }
        });
    }


    @Override
    public Set<Class<?>> initializeAfter() {
        return ImmutableSet.of(DataSourceAddon.class);
    }

    public QueryRunnerAddon name(String name) {
        return withName(name);
    }
}

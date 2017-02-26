package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.exception.DependenceException;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryRunnerAddon implements Addon {
    @Wither
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    QueryRunner queryRunner;

    public static final QueryRunnerAddon defaults =
            new QueryRunnerAddon(null, null);

    @Override
    public Addon finalize(ServiceConfig serviceConfig) {
        DataSourceAddon dataSourceAddon = serviceConfig.getNamedAddon(DataSourceAddon.class, name);
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
        jerseyConfig.addBinder(binder -> binder.bind(queryRunner).to(QueryRunner.class));
    }


    @Override
    public Set<Class<?>> finalizeAfter() {return ImmutableSet.of(DataSourceAddon.class);}
}

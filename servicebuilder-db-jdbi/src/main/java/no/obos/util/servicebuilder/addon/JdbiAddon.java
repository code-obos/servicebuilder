package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.Factory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.sql.DataSource;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbiAddon implements NamedAddon {

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final Jdbi jdbi;
    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<Class<?>> daos;

    public static final JdbiAddon defaults =
            new JdbiAddon(null, null, ImmutableList.of());

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        DataSourceAddon dataSourceAddon = serviceConfig.addonInstanceNamed(DataSourceAddon.class, name);
        if (dataSourceAddon == null) {
            if (name == null) {
                throw new DependenceException(this.getClass(), DataSourceAddon.class, " no unnamed datasourceaddon found");
            } else {
                throw new DependenceException(this.getClass(), DataSourceAddon.class, " no datasourceaddon for name " + name);
            }
        }
        DataSource dataSource = dataSourceAddon.getDataSource();
        Jdbi jdbi = Jdbi.create(dataSource);
        SqlObjectPlugin sqlObjectPlugin = new SqlObjectPlugin();
        sqlObjectPlugin.customizeJdbi(jdbi);
        return this.jdbi(jdbi);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
            if (name != null) {
                binder.bind(jdbi).to(Jdbi.class).named(name);
                binder.bind(this).to(JdbiAddon.class).named(name);
            } else {
                binder.bind(jdbi).to(Jdbi.class);
                binder.bind(this).to(JdbiAddon.class);
            }

            daos.forEach(clazz ->
                    binder.bindFactory(new DaoFactory(jdbi, clazz)).to(clazz)
            );

        });
    }

    @AllArgsConstructor
    public static class DaoFactory implements Factory<Object> {

        final Jdbi jdbi;
        final Class<?> clazz;

        public Object provide() {
            return jdbi.onDemand(clazz);
        }

        @Override
        public void dispose(Object instance) {

        }
    }

    public <T> T createDao(Class<T> requiredType) {
        return jdbi.onDemand(requiredType);
    }


    @Override
    public Set<Class<?>> initializeAfter() {
        return ImmutableSet.of(DataSourceAddon.class);
    }

    public JdbiAddon dao(Class<?> dao) {
        return withDaos(GuavaHelper.plus(daos, dao));
    }

    public JdbiAddon name(String name) {
        return withName(name);
    }

    public JdbiAddon jdbi(Jdbi jdbi) {
        return withJdbi(jdbi);
    }
}

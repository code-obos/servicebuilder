package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;
import no.obos.util.servicebuilder.util.JdbiAddonUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.skife.jdbi.v2.DBI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sql.DataSource;
import java.util.Set;

public class Jdbi3Addon implements JdbiAddon {

    public static final JdbiAddon defaults =
            new Jdbi3Addon(null, null, ImmutableList.of());
    @Getter
    @Wither
    final String name;
    @Wither
    final Jdbi jdbi;
    @Wither
    final ImmutableList<Class<?>> daos;

    private Jdbi3Addon(String name, Jdbi jdbi, ImmutableList<Class<?>> daos) {
        this.name = name;
        this.jdbi = jdbi;
        this.daos = daos;
    }


    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
            if (name != null) {
                binder.bind(jdbi).to(Jdbi.class).named(name);
                binder.bind(this).to(Jdbi3Addon.class).named(name);
            } else {
                binder.bind(jdbi).to(Jdbi.class);
                binder.bind(this).to(Jdbi3Addon.class);
            }

            daos.forEach(clazz ->
                    binder.bindFactory(new DaoFactory(jdbi, null, clazz)).to(clazz)
            );

        });
    }

    public Addon initialize(ServiceConfig serviceConfig)
    {
        DataSource dataSource = JdbiAddonUtil.getDataSource(this, serviceConfig, name);
        Jdbi jdbi = Jdbi.create(dataSource);
        SqlObjectPlugin sqlObjectPlugin = new SqlObjectPlugin();
        sqlObjectPlugin.customizeJdbi(jdbi);
        return this.jdbi(jdbi);
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

    @Override
    public JdbiAddon dbi(DBI dbi) {
        throw new NotImplementedException();
    }

    public JdbiAddon jdbi(Jdbi jdbi) {
        return withJdbi(jdbi);
    }
}

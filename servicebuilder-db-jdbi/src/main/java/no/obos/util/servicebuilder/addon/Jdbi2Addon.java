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
import org.skife.jdbi.v2.DBI;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sql.DataSource;
import java.util.Set;

@Deprecated
public class Jdbi2Addon implements JdbiAddon {

    public static final JdbiAddon defaults = new Jdbi2Addon(null, null, ImmutableList.of());

    @Getter
    @Wither
    final String name;

    @Wither
    final DBI dbi;

    @Wither
    final ImmutableList<Class<?>> daos;

    private Jdbi2Addon(String name, DBI dbi, ImmutableList<Class<?>> daos) {
        this.name = name;
        this.dbi = dbi;
        this.daos = daos;
    }

    public Addon initialize(ServiceConfig serviceConfig) {
        DataSource dataSource = JdbiAddonUtil.getDataSource(this, serviceConfig, name);
        DBI dbi = new DBI(dataSource);
        return this.dbi(dbi);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
            if (name != null) {
                binder.bind(dbi).to(DBI.class).named(name);
                binder.bind(this).to(Jdbi2Addon.class).named(name);
            } else {
                binder.bind(dbi).to(DBI.class);
                binder.bind(this).to(Jdbi2Addon.class);
            }

            daos.forEach(clazz ->
                    binder.bindFactory(new DaoFactory(null, dbi, clazz)).to(clazz)
            );

        });
    }

    @Override
    public <T> T createDao(Class<T> requiredType) {
        return dbi.onDemand(requiredType);
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
    public JdbiAddon jdbi(Jdbi jdbi) {
        throw new NotImplementedException();
    }

    public JdbiAddon dbi(DBI dbi) {
        return withDbi(dbi);
    }


}

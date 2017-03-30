package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InstantiationData;
import org.glassfish.hk2.api.InstantiationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.skife.jdbi.v2.DBI;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JdbiAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final DBI dbi;
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
        DBI dbi = new DBI(dataSource);
        return this.dbi(dbi);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        if (name != null) {
            jerseyConfig.addBinder(binder -> binder.bind(dbi).to(DBI.class).named(name));
        } else {
            jerseyConfig.addBinder(binder -> binder.bind(dbi).to(DBI.class));
        }
        jerseyConfig.addBinder(binder ->
                daos.forEach(clazz -> {
                            binder.bind(dbi).to(DBI.class).named(clazz.getCanonicalName());
                            //noinspection unchecked
                            binder.bindFactory(DaoFactory.class).to(clazz);
                        }

                )
        );
    }

    public static class DaoFactory implements Factory<Object> {

        final InstantiationService instantiationService;
        final ServiceLocator serviceLocator;

        @Inject
        public DaoFactory(HttpHeaders headers, InstantiationService instantiationService, ServiceLocator serviceLocator) {
            this.instantiationService = instantiationService;
            this.serviceLocator = serviceLocator;
        }

        public Object provide() {
            Class<?> requiredType = getDaoClass();
            DBI dbi = serviceLocator.getService(DBI.class, requiredType.getCanonicalName());

            return dbi.onDemand(requiredType);
        }

        @Override
        public void dispose(Object instance) {

        }

        private Class<?> getDaoClass() {
            InstantiationData instantiationData = instantiationService.getInstantiationData();
            Injectee parentInjectee = instantiationData.getParentInjectee();
            return (Class) parentInjectee.getRequiredType();
        }
    }

    public <T> T createDao(Class<T> requiredType) {
        return dbi.onDemand(requiredType);
    }


    @Override
    public Set<Class<?>> initializeAfter() {return ImmutableSet.of(DataSourceAddon.class);}

    public JdbiAddon dao(Class<?> dao) {
        return withDaos(GuavaHelper.plus(daos, dao));
    }

    public JdbiAddon name(String name) {return withName(name);}

    public JdbiAddon dbi(DBI dbi) {return withDbi(dbi);}
}

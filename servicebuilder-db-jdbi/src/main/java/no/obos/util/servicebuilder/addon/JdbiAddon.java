package no.obos.util.servicebuilder.addon;

import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import org.glassfish.hk2.api.Factory;
import org.jdbi.v3.core.Jdbi;
import org.skife.jdbi.v2.DBI;

import java.util.Set;


public interface JdbiAddon extends NamedAddon {

    JdbiAddon defaults = Jdbi3Addon.defaults;


    @Override
    Addon initialize(ServiceConfig serviceConfig);

    @Override
    void addToJerseyConfig(JerseyConfig jerseyConfig);

    <T> T createDao(Class<T> requiredType);

    @Override
    Set<Class<?>> initializeAfter();

    JdbiAddon dao(Class<?> dao);

    JdbiAddon name(String name);

    JdbiAddon dbi(DBI dbi);

    JdbiAddon jdbi(Jdbi jdbi);


    @AllArgsConstructor
    class DaoFactory implements Factory<Object> {

        final Jdbi jdbi;
        final DBI dbi;
        final Class<?> clazz;

        public Object provide() {
            if (jdbi != null) {
                return jdbi.onDemand(clazz);
            } else {
                return dbi.onDemand(clazz);
            }
        }

        @Override
        public void dispose(Object instance) {

        }
    }
}

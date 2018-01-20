package no.obos.util.servicebuilder.addon;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Knytter opp en datakilde og binder BasicDatasource og QueryRunner til hk2.
 * Ved initialisering (defaults og config) kan det legges til et navn til datakilden
 * for å støtte flere datakilder. Parametre fre properties vil da leses fra
 * navnet (databasenavn).db.url osv.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class H2InMemoryDatasourceAddon implements DataSourceAddon {

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final DataSource dataSource;
    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final boolean unitTest;

    @Wither(AccessLevel.PRIVATE)
    public final ImmutableList<String> scripts;

    public static H2InMemoryDatasourceAddon h2InMemoryDatasourceAddon = new H2InMemoryDatasourceAddon(null, null, true, ImmutableList.of());

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        String databaseName = unitTest ? "" : ! isNullOrEmpty(name) ? name : "test";

        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1", "user", "password");
        scripts.forEach(script -> {

            try {
                Connection conn = dataSource.getConnection();
                conn.createStatement().executeUpdate(script);
                conn.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        return this.withDataSource(dataSource);
    }

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    if (! isNullOrEmpty(name)) {
                        binder.bind(dataSource).named(name).to(DataSource.class);
                    } else {
                        binder.bind(dataSource).to(DataSource.class);
                    }
                }
        );
    }

    public H2InMemoryDatasourceAddon script(String script) {
        return withScripts(GuavaHelper.plus(scripts, script));
    }

    public H2InMemoryDatasourceAddon insert(String table, Object... toInsert) {
        String attributes = Joiner.on(", ").join(toInsert);
        return withScripts(GuavaHelper.plus(scripts, "INSERT INTO " + table + " VALUES (" + attributes + ");"));
    }

    public H2InMemoryDatasourceAddon name(String name) {
        return withName(name);
    }

    /**
     * Configures H2 to construct a new database for each connection.
     */
    public H2InMemoryDatasourceAddon unitTest(boolean unitTest) {
        return withUnitTest(unitTest);
    }

}

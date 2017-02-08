package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Knytter opp en datakilde og binder BasicDatasource og QueryRunner til hk2.
 * Ved initialisering (defaults og config) kan det legges til et navn til datakilden
 * for å støtte flere datakilder. Parametre fre properties vil da leses fra
 * navnet (databasenavn).db.url osv.
 */
@Builder(toBuilder = true)
public class BasicDatasourceAddon implements Addon {

    public static final String CONFIG_KEY_DB_URL = "db.url";
    public static final String CONFIG_KEY_DB_DRIVER_CLASS_NAME = "db.driverClassName";
    public static final String CONFIG_KEY_DB_USERNAME = "db.username";
    public static final String CONFIG_KEY_DB_PASSWORD = "db.password";
    public static final String CONFIG_KEY_DB_VALIDATION_QUERY = "db.validationQuery";

    public final String name;
    public final String url;
    public final String driverClassName;
    public final String username;
    public final String password;
    public final String validationQuery;
    public final boolean monitorIntegration;
    public final boolean bindQueryRunner;

    public final BasicDataSource dataSource;
    public final QueryRunner queryRunner;


    public static class BasicDatasourceAddonBuilder {
        boolean monitorIntegration = true;
        boolean bindQueryRunner = true;
    }

    @Override
    public Addon withDependencies(ServiceConfig serviceConfig) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setValidationQuery(validationQuery);


        QueryRunner queryRunner = null;
        if (bindQueryRunner) {
            queryRunner = new QueryRunner(dataSource);
        }

        return this.toBuilder().dataSource(dataSource).queryRunner(queryRunner).build();
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_DB_URL, prefix + CONFIG_KEY_DB_USERNAME, prefix + CONFIG_KEY_DB_PASSWORD, prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME, prefix + CONFIG_KEY_DB_VALIDATION_QUERY);
        return this.toBuilder()
                .url(properties.get(prefix + CONFIG_KEY_DB_URL))
                .username(properties.get(prefix + CONFIG_KEY_DB_USERNAME))
                .password(properties.get(prefix + CONFIG_KEY_DB_PASSWORD))
                .driverClassName(properties.get(prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME))
                .validationQuery(properties.get(prefix + CONFIG_KEY_DB_VALIDATION_QUERY))
                .build();
    }



    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    if (! Strings.isNullOrEmpty(name)) {
                        binder.bind(dataSource).named(name).to(DataSource.class);
                    } else {
                        binder.bind(dataSource).to(DataSource.class);
                    }
                    if (bindQueryRunner) {
                        QueryRunner queryRunner = new QueryRunner(dataSource);
                        if (! Strings.isNullOrEmpty(name)) {
                            binder.bind(queryRunner).to(QueryRunner.class);
                        } else {
                            binder.bind(queryRunner).named(name).to(QueryRunner.class);
                        }
                    }
                }
        );
    }

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        if (monitorIntegration) {
            String dataSourceName = Strings.isNullOrEmpty(name)
                    ? " (" + name + ")"
                    : "";
            ObosHealthCheckRegistry.registerDataSourceCheck("Database" + dataSourceName + ": " + url, dataSource, validationQuery);
        }
    }
}

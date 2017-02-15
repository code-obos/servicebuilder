package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import no.obos.metrics.ObosHealthCheckRegistry;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.Objects;

/**
 * Knytter opp en datakilde og binder BasicDatasource og QueryRunner til hk2.
 * Ved initialisering (defaults og config) kan det legges til et navn til datakilden
 * for å støtte flere datakilder. Parametre fre properties vil da leses fra
 * navnet (databasenavn).db.url osv.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static BasicDatasourceAddon defaults = new BasicDatasourceAddon(null, null, null, null, null, null, true, true, null, null);

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

        return this.dataSource(dataSource).queryRunner(queryRunner);
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";
        properties.failIfNotPresent(prefix + CONFIG_KEY_DB_URL, prefix + CONFIG_KEY_DB_USERNAME, prefix + CONFIG_KEY_DB_PASSWORD, prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME, prefix + CONFIG_KEY_DB_VALIDATION_QUERY);
        return this
                .url(properties.get(prefix + CONFIG_KEY_DB_URL))
                .username(properties.get(prefix + CONFIG_KEY_DB_USERNAME))
                .password(properties.get(prefix + CONFIG_KEY_DB_PASSWORD))
                .driverClassName(properties.get(prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME))
                .validationQuery(properties.get(prefix + CONFIG_KEY_DB_VALIDATION_QUERY));
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


    public BasicDatasourceAddon name(String name) {return Objects.equals(this.name, name) ? this : new BasicDatasourceAddon(name, this.url, this.driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon url(String url) {return Objects.equals(this.url, url) ? this : new BasicDatasourceAddon(this.name, url, this.driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon driverClassName(String driverClassName) {return Objects.equals(this.driverClassName, driverClassName) ? this : new BasicDatasourceAddon(this.name, this.url, driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon username(String username) {return Objects.equals(this.username, username) ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon password(String password) {return Objects.equals(this.password, password) ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon validationQuery(String validationQuery) {return Objects.equals(this.validationQuery, validationQuery) ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, this.password, validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon monitorIntegration(boolean monitorIntegration) {return this.monitorIntegration == monitorIntegration ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, this.password, this.validationQuery, monitorIntegration, this.bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon bindQueryRunner(boolean bindQueryRunner) {return this.bindQueryRunner == bindQueryRunner ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, bindQueryRunner, this.dataSource, this.queryRunner);}

    public BasicDatasourceAddon dataSource(BasicDataSource dataSource) {return this.dataSource == dataSource ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, dataSource, this.queryRunner);}

    public BasicDatasourceAddon queryRunner(QueryRunner queryRunner) {return this.queryRunner == queryRunner ? this : new BasicDatasourceAddon(this.name, this.url, this.driverClassName, this.username, this.password, this.validationQuery, this.monitorIntegration, this.bindQueryRunner, this.dataSource, queryRunner);}
}

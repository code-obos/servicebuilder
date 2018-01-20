package no.obos.util.servicebuilder.addon;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import no.obos.util.servicebuilder.util.ObosHealthCheckRegistry;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * Knytter opp en datakilde og binder BasicDatasource og QueryRunner til hk2.
 * Ved initialisering (defaults og config) kan det legges til et navn til datakilden
 * for å støtte flere datakilder. Parametre fre properties vil da leses fra
 * navnet (databasenavn).db.url osv.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicDatasourceAddon implements DataSourceAddon {

    public static final String CONFIG_KEY_DB_URL = "db.url";
    public static final String CONFIG_KEY_DB_DRIVER_CLASS_NAME = "db.driverClassName";
    public static final String CONFIG_KEY_DB_USERNAME = "db.username";
    public static final String CONFIG_KEY_DB_PASSWORD = "db.password";
    public static final String CONFIG_KEY_DB_VALIDATION_QUERY = "db.validationQuery";

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final String name;
    @Wither(AccessLevel.PRIVATE)
    public final String url;
    @Wither(AccessLevel.PRIVATE)
    public final String driverClassName;
    @Wither(AccessLevel.PRIVATE)
    public final String username;
    @Wither(AccessLevel.PRIVATE)
    public final String password;
    @Wither(AccessLevel.PRIVATE)
    public final String validationQuery;
    @Wither(AccessLevel.PRIVATE)
    public final boolean monitorIntegration;
    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final DataSource dataSource;

    public static BasicDatasourceAddon basicDatasourceAddon = new BasicDatasourceAddon(null, null, null, null, null, "select 1", true, null);

    @Override
    public Addon initialize(ServiceConfig serviceConfig) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(url);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setValidationQuery(validationQuery);

        return this.withDataSource(dataSource);
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";
        return this
                .url(properties.requireWithFallback(prefix + CONFIG_KEY_DB_URL, url))
                .username(properties.requireWithFallback(prefix + CONFIG_KEY_DB_USERNAME, username))
                .password(properties.requireWithFallback(prefix + CONFIG_KEY_DB_PASSWORD, password))
                .driverClassName(properties.requireWithFallback(prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME, driverClassName))
                .validationQuery(properties.requireWithFallback(prefix + CONFIG_KEY_DB_VALIDATION_QUERY, validationQuery));
    }



    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    if (! Strings.isNullOrEmpty(name)) {
                        binder.bind(dataSource).named(name).to(DataSource.class);
                    } else {
                        binder.bind(dataSource).to(DataSource.class);
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

    public BasicDatasourceAddon name(String name) {
        return withName(name);
    }

    public BasicDatasourceAddon url(String url) {
        return withUrl(url);
    }

    public BasicDatasourceAddon driverClassName(String driverClassName) {
        return withDriverClassName(driverClassName);
    }

    public BasicDatasourceAddon username(String username) {
        return withUsername(username);
    }

    public BasicDatasourceAddon password(String password) {
        return withPassword(password);
    }

    public BasicDatasourceAddon validationQuery(String validationQuery) {
        return withValidationQuery(validationQuery);
    }

    public BasicDatasourceAddon monitorIntegration(boolean monitorIntegration) {
        return withMonitorIntegration(monitorIntegration);
    }
}

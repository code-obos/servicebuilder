package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;


public class BasicDatasourceAddon extends ServiceAddonEmptyDefaults {

    public static final String CONFIG_KEY_DB_URL = "db.url";
    public static final String CONFIG_KEY_DB_DRIVER_CLASS_NAME = "db.driverClassName";
    public static final String CONFIG_KEY_DB_USERNAME = "db.username";
    public static final String CONFIG_KEY_DB_PASSWORD = "db.password";
    public static final String CONFIG_KEY_DB_VALIDATION_QUERY = "db.validationQuery";

    public static final boolean DEFAULT_MONITOR_INTEGRATION = true;
    public static final boolean DEFAULT_BIND_QUERYRUNNER = true;

    public final Configuration configuration;
    public final BasicDataSource dataSource;

    public BasicDatasourceAddon(Configuration configuration) {
        dataSource = new BasicDataSource();
        dataSource.setUrl(configuration.url);
        dataSource.setDriverClassName(configuration.driverClassName);
        dataSource.setUsername(configuration.username);
        dataSource.setPassword(configuration.password);

        this.configuration = configuration;
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final String url;
        public final String driverClassName;
        public final String username;
        public final String password;
        public final String validationQuery;
        public final boolean monitorIntegration;
        public final boolean bindQueryRunner;
    }

    public static Configuration.ConfigurationBuilder defaultConfiguration() {
        return Configuration.builder()
                .monitorIntegration(DEFAULT_MONITOR_INTEGRATION)
                .bindQueryRunner(DEFAULT_BIND_QUERYRUNNER);
    }

    public static void configFromAppConfig(AppConfig appConfig, Configuration.ConfigurationBuilder configBuilder) {
        appConfig.failIfNotPresent(CONFIG_KEY_DB_URL, CONFIG_KEY_DB_USERNAME, CONFIG_KEY_DB_PASSWORD, CONFIG_KEY_DB_DRIVER_CLASS_NAME, CONFIG_KEY_DB_VALIDATION_QUERY);
        configBuilder
                .url(appConfig.get(CONFIG_KEY_DB_URL))
                .username(appConfig.get(CONFIG_KEY_DB_USERNAME))
                .password(appConfig.get(CONFIG_KEY_DB_PASSWORD))
                .driverClassName(appConfig.get(CONFIG_KEY_DB_DRIVER_CLASS_NAME))
                .validationQuery(appConfig.get(CONFIG_KEY_DB_VALIDATION_QUERY));

    }



    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    binder.bind(dataSource).to(DataSource.class);
                    if (configuration.bindQueryRunner) {
                        QueryRunner queryRunner = new QueryRunner(dataSource);
                        binder.bind(queryRunner).to(QueryRunner.class);
                    }
                }
        );
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        if (configuration.monitorIntegration) {
            ObosHealthCheckRegistry.registerDataSourceCheck("Database: " + configuration.url, dataSource, configuration.validationQuery);
        }
    }



    //Det etterfølgende er generisk kode som er vanskelig å flytte ut i egne klasser pga generics. Kopier mellom addons.
    @AllArgsConstructor
    public static class AddonBuilder implements ServiceAddonConfig<BasicDatasourceAddon> {
        Configurator options;
        Configuration.ConfigurationBuilder configBuilder;

        @Override
        public void addAppConfig(AppConfig appConfig) {
            configFromAppConfig(appConfig, configBuilder);
        }

        @Override
        public void addContext(ServiceBuilder serviceBuilder) {
            configFromContext(serviceBuilder, configBuilder);
        }

        @Override
        public BasicDatasourceAddon init() {
            configBuilder = options.apply(configBuilder);
            return new BasicDatasourceAddon(configBuilder.build());
        }
    }

    public static AddonBuilder configure(Configurator options) {
        return new AddonBuilder(options, defaultConfiguration());
    }

    public static AddonBuilder defaults() {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration());
    }

    public interface Configurator {
        Configuration.ConfigurationBuilder apply(Configuration.ConfigurationBuilder configBuilder);
    }
}

package no.obos.util.servicebuilder;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.config.AppConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.util.Optional;


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
    public final Optional<QueryRunner> queryRunner;

    public BasicDatasourceAddon(Configuration configuration) {
        dataSource = new BasicDataSource();
        dataSource.setUrl(configuration.url);
        dataSource.setDriverClassName(configuration.driverClassName);
        dataSource.setUsername(configuration.username);
        dataSource.setPassword(configuration.password);


        if (configuration.bindQueryRunner) {
            queryRunner = Optional.of(new QueryRunner(dataSource));
        } else {
            queryRunner = Optional.empty();
        }

        this.configuration = configuration;
    }

    @Builder
    @AllArgsConstructor
    public static class Configuration {
        public final Optional<String> name;
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
        String name = configBuilder.build().name.orElse(null);
        String prefix = Strings.isNullOrEmpty(name) ? "" : name + ".";
        appConfig.failIfNotPresent(prefix + CONFIG_KEY_DB_URL, prefix + CONFIG_KEY_DB_USERNAME, prefix + CONFIG_KEY_DB_PASSWORD, prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME, prefix + CONFIG_KEY_DB_VALIDATION_QUERY);
        configBuilder
                .url(appConfig.get(prefix + CONFIG_KEY_DB_URL))
                .username(appConfig.get(prefix + CONFIG_KEY_DB_USERNAME))
                .password(appConfig.get(prefix + CONFIG_KEY_DB_PASSWORD))
                .driverClassName(appConfig.get(prefix + CONFIG_KEY_DB_DRIVER_CLASS_NAME))
                .validationQuery(appConfig.get(prefix + CONFIG_KEY_DB_VALIDATION_QUERY));

    }



    @Override public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    if (! configuration.name.isPresent()) {
                        binder.bind(dataSource).named(configuration.name.get()).to(DataSource.class);
                    } else {
                        binder.bind(dataSource).to(DataSource.class);
                    }
                    if (configuration.bindQueryRunner) {
                        QueryRunner queryRunner = new QueryRunner(dataSource);
                        if (! configuration.name.isPresent()) {
                            binder.bind(queryRunner).to(QueryRunner.class);
                        } else {
                            binder.bind(queryRunner).named(configuration.name.get()).to(QueryRunner.class);
                        }
                    }
                }
        );
    }

    @Override public void addToJettyServer(JettyServer jettyServer) {
        if (configuration.monitorIntegration) {
            String dataSourceName = configuration.name.map(str -> " (" + str + ")").orElse("");
            ObosHealthCheckRegistry.registerDataSourceCheck("Database" + dataSourceName + ": " + configuration.url, dataSource, configuration.validationQuery);
        }
    }

    public static AddonBuilder configure(String name, Configurator options) {
        return new AddonBuilder(options, defaultConfiguration().name(Optional.of(name)));
    }

    public static AddonBuilder defaults(String name) {
        return new AddonBuilder(cfg -> cfg, defaultConfiguration().name(Optional.of(name)));
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

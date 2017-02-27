package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.addon.BasicDatasourceAddon;
import no.obos.util.servicebuilder.addon.CorsFilterAddon;
import no.obos.util.servicebuilder.addon.ExceptionMapperAddon;
import no.obos.util.servicebuilder.addon.H2InMemoryDatasourceAddon;
import no.obos.util.servicebuilder.addon.JdbiAddon;
import no.obos.util.servicebuilder.addon.JerseyClientAddon;
import no.obos.util.servicebuilder.addon.MetricsAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.addon.QueryRunnerAddon;
import no.obos.util.servicebuilder.addon.SwaggerAddon;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import no.obos.util.servicebuilder.model.ServiceDefinition;

public class Addons {

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static CorsFilterAddon cors() {
        return CorsFilterAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static ExceptionMapperAddon exceptionMapper() {
        return ExceptionMapperAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return JerseyClientAddon.defaults(serviceDefinition);
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static MetricsAddon metrics() {
        return MetricsAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static SwaggerAddon swagger() {
        return SwaggerAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-core-addons</artifactId>
        </dependency>
     */
    public static WebAppAddon webAppAddon() {
        return WebAppAddon.defaults;
    }


    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-basicdatasource</artifactId>
        </dependency>
     */
    public static BasicDatasourceAddon basicDatasource() {
        return BasicDatasourceAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-h2</artifactId>
        </dependency>
     */
    public static H2InMemoryDatasourceAddon h2InMemoryDatasource() {
        return H2InMemoryDatasourceAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-jdbi</artifactId>
        </dependency>
     */
    public static JdbiAddon jdbi() {
        return JdbiAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-queryrunner</artifactId>
        </dependency>
     */
    public static QueryRunnerAddon queryRunner() {
        return QueryRunnerAddon.defaults;
    }

    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .addon(SwaggerAddon.defaults)
                .addon(CorsFilterAddon.defaults)
                .addon(MetricsAddon.defaults)
                .addon(ObosLogFilterAddon.defaults)
                .addon(ExceptionMapperAddon.defaults)
                ;
    }
}

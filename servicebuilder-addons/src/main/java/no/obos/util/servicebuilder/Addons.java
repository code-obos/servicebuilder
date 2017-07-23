package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.addon.ApplicationTokenFilterAddon;
import no.obos.util.servicebuilder.addon.BasicDatasourceAddon;
import no.obos.util.servicebuilder.addon.CorsFilterAddon;
import no.obos.util.servicebuilder.addon.ExceptionMapperAddon;
import no.obos.util.servicebuilder.addon.H2InMemoryDatasourceAddon;
import no.obos.util.servicebuilder.addon.JdbiAddon;
import no.obos.util.servicebuilder.addon.JerseyClientAddon;
import no.obos.util.servicebuilder.addon.MetricsAddon;
import no.obos.util.servicebuilder.addon.MqAddon;
import no.obos.util.servicebuilder.addon.ObosLogFilterAddon;
import no.obos.util.servicebuilder.addon.QueryRunnerAddon;
import no.obos.util.servicebuilder.addon.RequireAppNameHeaderAddon;
import no.obos.util.servicebuilder.addon.ServerLogAddon;
import no.obos.util.servicebuilder.addon.SwaggerAddon;
import no.obos.util.servicebuilder.addon.TokenServiceAddon;
import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.ActiveMqAddon;

public class Addons {

    public static CorsFilterAddon cors() {
        return CorsFilterAddon.defaults;
    }

    public static ExceptionMapperAddon exceptionMapper() {
        return ExceptionMapperAddon.defaults;
    }

    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return JerseyClientAddon.defaults(serviceDefinition);
    }

    public static MetricsAddon metrics() {
        return MetricsAddon.defaults;
    }

    public static SwaggerAddon swagger() {
        return SwaggerAddon.defaults;
    }

    public static WebAppAddon webAppAddon() {
        return WebAppAddon.defaults;
    }

    public static ServerLogAddon serverLog() {
        return ServerLogAddon.defaults;
    }

    public static RequireAppNameHeaderAddon requireAppNameHeader() {
        return RequireAppNameHeaderAddon.defaults;
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

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-iam</artifactId>
        </dependency>
     */
    public static TokenServiceAddon tokenService() {
        return TokenServiceAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-iam</artifactId>
        </dependency>
     */
    public static UserTokenFilterAddon userTokenFilter() {
        return UserTokenFilterAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-iam</artifactId>
        </dependency>
     */
    public static ApplicationTokenFilterAddon applicationTokenFilter() {
        return ApplicationTokenFilterAddon.defaults;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-mq</artifactId>
        </dependency>
     */
    public static MqAddon mq() {
        return MqAddon.defaults;
    }


    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-mq-activemq</artifactId>
        </dependency>
     */
    public static ActiveMqAddon activeMq() {
        return ActiveMqAddon.defaults;
    }

    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .addon(SwaggerAddon.defaults)
                .addon(CorsFilterAddon.defaults)
                .addon(MetricsAddon.defaults)
                .addon(ObosLogFilterAddon.defaults)
                .addon(ExceptionMapperAddon.defaults)
                .addon(ServerLogAddon.defaults)
                ;
    }
}

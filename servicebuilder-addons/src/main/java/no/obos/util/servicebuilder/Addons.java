package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.addon.*;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.MessageHandler;

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
        return jdbi3();
    }

    @Deprecated
    public static JdbiAddon jdbi2() {
        return Jdbi2Addon.defaults;
    }

    public static JdbiAddon jdbi3() {
        return Jdbi3Addon.defaults;
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
            <artifactId>servicebuilder-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqListenerAddon activeMqListener(Class<? extends MessageHandler> handler) {
        return ActiveMqListenerAddon.defaults(handler);
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-activemq</artifactId>
        </dependency>
    */
    public static ActiveMqSenderAddon activeMqSender() {
        return ActiveMqSenderAddon.defaults;
    }

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>servicebuilder-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchAddonImpl elasticsearch() {
        return ElasticsearchAddonImpl.defaults;
    }

    public static ElasticsearchIndexAddon elasticsearchIndex(String indexName, Class<?> indexedType) {
        return ElasticsearchIndexAddon.defaults(indexName, indexedType);
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

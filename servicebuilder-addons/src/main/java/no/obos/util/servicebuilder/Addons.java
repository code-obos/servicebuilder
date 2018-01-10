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

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>servicebuilder-elasticsearch-mock</artifactId>
    </dependency>
    */
    public static ElasticsearchAddonMockImpl elasticsearchMock() {
        return ElasticsearchAddonMockImpl.defaults;
    }
    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>servicebuilder-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchIndexAddon elasticsearchIndex(String indexName, Class<?> indexedType) {
        return ElasticsearchIndexAddon.defaults(indexName, indexedType);
    }


    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .addon(SwaggerAddon.defaults)
                .addon(CorsFilterAddon.defaults)
                .addon(ObosLogFilterAddon.defaults)
                .addon(ExceptionMapperAddon.defaults)
                .addon(ServerLogAddon.defaults)
                ;
    }
}

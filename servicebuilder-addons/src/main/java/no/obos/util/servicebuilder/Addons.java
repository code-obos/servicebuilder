package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.addon.*;
import no.obos.util.servicebuilder.model.ServiceDefinition;
import no.obos.util.servicebuilder.mq.MessageHandler;

import static no.obos.util.servicebuilder.addon.BasicDatasourceAddon.basicDatasourceAddon;
import static no.obos.util.servicebuilder.addon.CorsFilterAddon.corsFilterAddon;
import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static no.obos.util.servicebuilder.addon.H2InMemoryDatasourceAddon.h2InMemoryDatasourceAddon;
import static no.obos.util.servicebuilder.addon.JdbiAddon.jdbiAddon;
import static no.obos.util.servicebuilder.addon.JerseyClientAddon.jerseyClientAddon;
import static no.obos.util.servicebuilder.addon.QueryRunnerAddon.queryRunnerAddon;
import static no.obos.util.servicebuilder.addon.ServerLogAddon.serverLogAddon;
import static no.obos.util.servicebuilder.addon.SwaggerAddon.swaggerAddon;
import static no.obos.util.servicebuilder.addon.WebAppAddon.webAppAddon;

public class Addons {

    public static CorsFilterAddon cors() {
        return corsFilterAddon;
    }

    public static ExceptionMapperAddon exceptionMapper() {
        return exceptionMapperAddon;
    }

    public static JerseyClientAddon jerseyClient(ServiceDefinition serviceDefinition) {
        return jerseyClientAddon(serviceDefinition);
    }


    public static SwaggerAddon swagger() {
        return swaggerAddon;
    }

    public static WebAppAddon webAppAddon() {
        return webAppAddon;
    }

    public static ServerLogAddon serverLog() {
        return serverLogAddon;
    }


    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-basicdatasource</artifactId>
        </dependency>
     */
    public static BasicDatasourceAddon basicDatasource() {
        return basicDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-h2</artifactId>
        </dependency>
     */
    public static H2InMemoryDatasourceAddon h2InMemoryDatasource() {
        return h2InMemoryDatasourceAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-jdbi</artifactId>
        </dependency>
     */
    public static JdbiAddon jdbi() {
        return jdbiAddon;
    }

    /*
        <dependency>
            <groupId>no.obos.util</groupId>
            <artifactId>servicebuilder-db-queryrunner</artifactId>
        </dependency>
     */
    public static QueryRunnerAddon queryRunner() {
        return queryRunnerAddon;
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
        return ActiveMqSenderAddon.activeMqSenderAddon;
    }

    /*
    <dependency>
        <groupId>no.obos.util</groupId>
        <artifactId>servicebuilder-elasticsearch</artifactId>
    </dependency>
    */
    public static ElasticsearchClientAddon elasticsearch() {
        return ElasticsearchClientAddon.elasticsearchClientAddon;
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
        return ElasticsearchIndexAddon.elasticsearchIndexAddon(indexName, indexedType);
    }


    public static ServiceConfig standardAddons(ServiceDefinition serviceDefinition) {
        return ServiceConfig.defaults(serviceDefinition)
                .addon(swaggerAddon)
                .addon(corsFilterAddon)
                .addon(RequestIdAddon.requestIdAddon)
                .addon(exceptionMapperAddon)
                .addon(serverLogAddon)
                ;
    }
}

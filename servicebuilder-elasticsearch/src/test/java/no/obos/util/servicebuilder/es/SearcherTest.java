package no.obos.util.servicebuilder.es;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.*;
import no.obos.util.servicebuilder.addon.ElasticsearchAddon;
import no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon;
import no.obos.util.servicebuilder.addon.ElasticsearchMockAddon;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@Slf4j
public class SearcherTest {

    private static Client client;
    private static no.obos.util.servicebuilder.ServiceConfig serviceConfig ;
    private static TestServiceRunner testServiceRunner;
//            .addon(Addons.basicDatasource().name(DB_NAMED_BOLIGMARKED))
//            .addon(Addons.jdbi()
//                    .name(DB_NAMED_BOLIGMARKED)
//                    .dao(AbonnementDao.class))
//            .addon(Addons.jerseyClient(AnnonseDefinition.instance).addApiVersionToPath(false).apptoken(false))
//            .addon(Addons.jerseyClient(EmailDefinition.instance).addApiVersionToPath(false).apptoken(false))
//            //                                                             .addon(Addons.tokenService())
//            //                                                             .addon(Addons.applicationTokenFilter())
//            .bind(AbonnementResourceImpl.class, AbonnementResource.class)
//            .bind(AbonnementRepositoryDb.class, AbonnementRepository.class)
//            .bind(AbonnementController.class)
//            .bind(SenderResourceImpl.class, SenderResource.class)
//            .bind(SenderController.class)
//            .bind(FreemarkerGenerator.class);


    @Api
    public static @Path("")
    interface Resource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        void get();
    }


    public static
    class ResourceImpl implements Resource {
        @Inject
        Searcher<TestService.Payload> searcher;
        @Override
        public void get() {
            ClusterHealthResponse clusterHealthResponse = searcher.getHealthy();
            assertEquals(ClusterHealthStatus.GREEN, clusterHealthResponse.getStatus());
        };
    }


    @BeforeClass
    public static void setup() throws NodeValidationException, UnknownHostException {


        elasticSearchTestNode();

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Settings settings = Settings.builder()
                .put("cluster.name", "test-search-api-5-local_jonas")
                .put("http.port", 9210)
                .put("transport.tcp.port", 9310)
                .put("node.name", "balle")
//                .put("network.bind_host", "_lo_")
                .put("http.publish_port", 9210)
                .put("http.publish_host", "127.0.0.1")
                .put("transport.bind_host", "127.0.0.1")
                .put("transport.publish_port", 9310)
                .put("transport.publish_host", "127.0.0.1")
                .build();

        client = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), 9300));
        serviceConfig = ServiceConfig
                .defaults(ServiceDefinitionUtil.simple(Resource.class))
                .addon(ElasticsearchMockAddon.defaults)
                .addon(ElasticsearchIndexAddon.defaults("balleIndex", TestService.Payload.class).client(client)
                )
                .bind(ResourceImpl.class, Resource.class);
        testServiceRunner = TestServiceRunner.defaults(serviceConfig);
        TestServiceRunner.defaults(serviceConfig).start();
    }

    static Node elasticSearchTestNode() throws NodeValidationException {
        Node node = new MyNode(
                Settings.builder()
                        .put("http.enabled", "true")
                        .put("path.home", "elasticsearch-data")
                        .put("cluster.name", "test-search-api-5-local_jonas")
                        .put("http.port", 9200)
                        .put("transport.tcp.port", 9300)
                        .put("http.publish_port", 9200)
                        .put("http.publish_host", "127.0.0.1")
                        .put("transport.bind_host", "127.0.0.1")
                        .put("transport.publish_port", 9300)
                        .put("transport.publish_host", "127.0.0.1")
                        .build(),
                Lists.newArrayList(Netty4Plugin.class));
        node.start();
        return node;
    }

    private static class MyNode extends Node {
        public MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
    }

    @Test
    public void test() {
        testServiceRunner.oneShotVoid(Resource.class, Resource::get);
        ClusterHealthResponse clusterHealthResponse = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
        assertEquals(ClusterHealthStatus.GREEN, clusterHealthResponse.getStatus());
    }
}

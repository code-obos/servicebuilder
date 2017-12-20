package no.obos.util.servicebuilder.es;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.*;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.node.NodeValidationException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.UnknownHostException;

import static org.junit.Assert.assertEquals;

@Slf4j
public class SearcherTest {

    private static TestServiceRunner testServiceRunner;

    @Test
    public void testValidConnectionBetweenClientAndServer() {
        testServiceRunner.chain()
                .addon(ElasticsearchAddon.class, it -> {
                    Client client = it.getClient();
                    ClusterHealthResponse clusterHealthResponse = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();

                    assertEquals("test-search-api-5-local_junit", clusterHealthResponse.getClusterName());
                    assertEquals(ClusterHealthStatus.GREEN, clusterHealthResponse.getStatus());
                    try {
                        client.explain(new ExplainRequest()).actionGet();
                        Assert.fail();
                    } catch (ActionRequestValidationException e) {
                        //good
                    } catch (Exception e) {
                        Assert.fail();
                    }
                }).run();
    }

    @BeforeClass
    public static void setup() throws NodeValidationException, UnknownHostException {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        ServiceConfig serviceConfig = ServiceConfig
                .defaults(ServiceDefinitionUtil.simple(Resource.class))
                .addon(ExceptionMapperAddon.defaults)
                .addon(ServerLogAddon.defaults)
                .addon(ElasticsearchAddonMockImpl.defaults)
                .addon(ElasticsearchIndexAddon.defaults("balleIndex", TestService.Payload.class))
                .addon(ElasticsearchIndexAddon.defaults("puppeIndex", String.class))
                .bind(ResourceImpl.class, Resource.class);
        testServiceRunner = TestServiceRunner.defaults(serviceConfig);
        TestServiceRunner.defaults(serviceConfig);
    }

    @Api
    @Path("")
    public interface Resource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        Response get();
    }


    public static class ResourceImpl implements Resource {
        @Inject
        Searcher<TestService.Payload> searcher1;
        @Inject
        Searcher<String> searcher2;

        @Override
        public Response get() {
            return Response.ok().build();
        }
    }
}

package es;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.ElasticsearchAddon;
import no.obos.util.servicebuilder.addon.ElasticsearchAddonMockImpl;
import no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon;
import no.obos.util.servicebuilder.addon.ExceptionMapperAddon;
import no.obos.util.servicebuilder.addon.ServerLogAddon;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Slf4j
public class SearcherTest {

    private static TestServiceRunner testServiceRunner;

    @Test
    public void testValidConnectionBetweenClientAndServer() {
        testServiceRunner.chain()
                .addon(ElasticsearchAddon.class, it -> {
                    Client client = it.getClient();
                    ClusterHealthResponse clusterHealthResponse = client.admin().cluster().health(new ClusterHealthRequest()).actionGet();

                    Assert.assertEquals("test-search-api-5-local_junit", clusterHealthResponse.getClusterName());
                    Assert.assertEquals(ClusterHealthStatus.GREEN, clusterHealthResponse.getStatus());
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
    public static void setup() {
        ServiceConfig serviceConfig = ServiceConfig
                .defaults(ServiceDefinitionUtil.simple(Resource.class))
                .addon(ExceptionMapperAddon.defaults)
                .addon(ServerLogAddon.defaults)
                .addon(ElasticsearchAddonMockImpl.defaults)
                .addon(ElasticsearchIndexAddon.defaults("oneIndex", TestService.Payload.class))
                .addon(ElasticsearchIndexAddon.defaults("anotherIndex", String.class))
                .bind(ResourceImpl.class, Resource.class);
        testServiceRunner = TestServiceRunner.defaults(serviceConfig);
    }

    @Api
    @Path("")
    public interface Resource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        Response get();
    }


    public static class ResourceImpl implements Resource {
        @Override
        public Response get() {
            return Response.ok().build();
        }
    }
}

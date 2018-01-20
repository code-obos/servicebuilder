package es;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.ServiceDefinitionUtil;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.addon.*;
import no.obos.util.servicebuilder.es.Indexer;
import no.obos.util.servicebuilder.es.Searcher;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.NodeValidationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon.elasticsearchIndexAddon;
import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static no.obos.util.servicebuilder.addon.ServerLogAddon.serverLogAddon;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

@Slf4j
public class IndexerTest {

    private static TestServiceRunner testServiceRunner;

    @Test
    public void testValidConnectionBetweenClientAndServer() {
        TestService.Payload p1 = new TestService.Payload("fieldname1", LocalDate.now().minusYears(1));
        TestService.Payload p2 = new TestService.Payload("fieldname2", LocalDate.now().plusYears(1));
        TestService.Payload p3 = new TestService.Payload("fieldname3", LocalDate.now());

        testServiceRunner.chain()
                .call(Resource.class, it -> it.index(Lists.newArrayList(p1, p2, p3)))
                .call(Resource.class, it -> {
                    Set<TestService.Payload> expected = ImmutableSet.of(p3);
                    List<TestService.Payload> actual = it.searchTerm("fieldname3");

                    assertEquals(expected, Sets.newHashSet(actual));
                })
                .call(Resource.class, it -> {
                    Set<TestService.Payload> expected = ImmutableSet.of(p2, p3);
                    List<TestService.Payload> actual = it.searchDates(LocalDate.now().toString(), LocalDate.now().plusYears(1).toString());

                    assertEquals(expected, Sets.newHashSet(actual));
                })
                .run();
    }

    @BeforeClass
    public static void setup() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        ServiceConfig serviceConfig = ServiceConfig
                .defaults(ServiceDefinitionUtil.simple(Resource.class))
                .addon(ElasticsearchAddonMockImpl.defaults)
                .addon(exceptionMapperAddon)
                .addon(serverLogAddon)
                .addon(elasticsearchIndexAddon("oneIndex", TestService.Payload.class)
                        .doIndexing(true)
                )
                .bind(ResourceImpl.class, Resource.class);
        testServiceRunner = TestServiceRunner.defaults(serviceConfig);
        TestServiceRunner.defaults(serviceConfig);
    }

    @Api
    @Path("")
    public interface Resource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        List<TestService.Payload> searchDates(@QueryParam("fra") String fra, @QueryParam("til") String to);

        @GET
        @Path("lala")
        @Produces(MediaType.APPLICATION_JSON)
        List<TestService.Payload> searchTerm(@QueryParam("name") String name);

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        void index(List<TestService.Payload> toIndex);
    }


    public static class ResourceImpl implements Resource {
        @Inject
        Searcher<TestService.Payload> searcher;
        @Inject
        Indexer<TestService.Payload> indexer;

        @Override
        public List<TestService.Payload> searchDates(String from, String to) {
            return searcher.query(QueryBuilders.rangeQuery("date").from(from).to(to));
        }

        @Override
        public List<TestService.Payload> searchTerm(String name) {
            return searcher.query(QueryBuilders.queryStringQuery(name).field("string"));
        }

        @Override
        public void index(List<TestService.Payload> toIndex) {
            String schema;
            try {
                schema = jsonBuilder()
                        .startObject()
                        .startObject("properties")
                            .startObject("date")
                                .field("type", "date")
                                .field("index", true)
                            .endObject()
                            .startObject("string")
                                .field("type", "text")
                                .field("index", true)
                            .endObject()
                        .endObject()
                        .endObject().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            indexer.index(schema, toIndex, TestService.Payload::getString);
        }
    }
}

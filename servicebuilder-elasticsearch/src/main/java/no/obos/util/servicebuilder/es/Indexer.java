package no.obos.util.servicebuilder.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon;
import no.obos.util.servicebuilder.util.JsonUtil;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static no.obos.util.servicebuilder.es.ElasticsearchUtil.getClusterName;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@Slf4j
@AllArgsConstructor
public class Indexer<T> {

    private final ElasticsearchIndexAddon indexAddon;

    public void delete(String id) {
        Client client = indexAddon.elasticsearchAddon.getClient();
        String indexName = indexAddon.indexname;
        client.prepareDelete(indexName, indexName, id)
                .setWaitForActiveShards(1)
                .get();
    }

    public void index(String schema, List<T> rowTypes, Function<T, String> id) {

        int bulkSize = 2000;
        int bulkConcurrent = 5;

        Client client = indexAddon.elasticsearchAddon.getClient();
        String indexName = indexAddon.indexname;

        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(indexName).get();

        if (indicesExistsResponse.isExists()) {
            client.admin().indices().preparePutMapping(indexName).setType(indexName).setSource(schema, XContentType.JSON).get();
        } else {
            CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(indexName).addMapping(indexName, schema, XContentType.JSON).get();
        }

        if (!isIndexingRunning(client.admin(), indexName)) {
            Map<String, String> rows = transform(rowTypes, id);

            BulkProcessor bulkRequest = bulkProcessorSupplier(client, bulkSize, indexAddon.elasticsearchAddon.isUnitTest() ? 0 : bulkConcurrent).get();
            rows.forEach((key, value) -> bulkRequest.add(createConverter(indexName, indexName).apply(key, value)));


            try {
                boolean b = bulkRequest.awaitClose(60000, TimeUnit.SECONDS);
                System.out.println("Fra bulkRequest: " + b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client.admin().indices().prepareRefresh().get();
        }
    }

    public void flush() {
        AdminClient admin = indexAddon.elasticsearchAddon.getClient().admin();
        if (admin.indices().prepareExists(indexAddon.indexname).get().isExists()) {
            admin.indices().flush(new FlushRequest(indexAddon.indexname)).actionGet();
        }
    }

    private static boolean isIndexingRunning(AdminClient client, String indexName) {
        IndicesStatsResponse indicesStatsResponse = client.indices()
                .prepareStats(indexName)
                .all()
                .execute()
                .actionGet();

        return indicesStatsResponse.getTotal().getIndexing().getTotal().getIndexCurrent() > INTEGER_ZERO;
    }

    private Map<String, String> transform(List<T> types, Function<T, String> idGetter) {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper(indexAddon.serializationSpec);
        return types.stream().collect(Collectors.toMap(idGetter, transformToJson(objectMapper)));
    }

    private Function<T, String> transformToJson(ObjectMapper objectMapper) {
        return type -> {
            try {
                return objectMapper.writeValueAsString(type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static BiFunction<String, String, IndexRequest> createConverter(String indexName, String indextype) {
        return (id, json) -> new IndexRequest().id(id)
                .index(indexName)
                .type(indextype)
                .source(json, XContentType.JSON);
    }

    private static Supplier<BulkProcessor> bulkProcessorSupplier(Client client, int bulkSize, int bulkConcurrent) {
        String clusterName = getClusterName(client);
        return () -> BulkProcessor.builder(client, new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                log.debug("Going to add person data to new bulk composed of {} actions on cluster {}", request.numberOfActions(), clusterName);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                log.debug("Executed bulk composed of {} actions on cluster {}", request.numberOfActions(), clusterName);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.error("Error executing bulk on cluster {}", failure, clusterName);
            }
        })
                .setBulkActions(bulkSize)
                .setConcurrentRequests(bulkConcurrent)
                .setFlushInterval(TimeValue.timeValueMinutes(30))
                .build();
    }
}

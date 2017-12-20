package no.obos.util.servicebuilder.es;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static no.obos.util.servicebuilder.es.ElasticsearchUtil.getClusterName;
import static org.apache.commons.lang3.math.NumberUtils.INTEGER_ZERO;

@Slf4j
@AllArgsConstructor
public class Indexer<T> {
    private final ElasticsearchIndexAddon indexAddon;
    final int bulkSize = 2000;
    final int bulkConcurrent = 5;


    public void index(String idFieldName, String schema, List<T> rowTypes)  {
        Client client = indexAddon.elasticsearchAddon.getClient();
        String indexName = indexAddon.indexname;

        IndicesExistsResponse indicesExistsResponse = client.admin().indices().prepareExists(indexName).get();

        if (indicesExistsResponse.isExists()) {
            client.admin().indices().preparePutMapping(indexName).setType(indexName).setSource(schema, XContentType.JSON).get();
        } else {
            CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(indexName).addMapping(indexName, schema, XContentType.JSON).get();

            if (createIndexResponse.isShardsAcked()) {

            }
        }

        if (! isIndexingRunning(client.admin(), indexName)) {
            List<Map<String, Object>> rows = transform(rowTypes);

            BulkProcessor bulkRequest = bulkProcessorSupplier(client, bulkSize, indexAddon.elasticsearchAddon.isUnitTest() ? 0 : bulkConcurrent).get();
            for (Map<String, Object> row : rows) {
                if (! row.isEmpty()) {
                    bulkRequest.add(createConverter(idFieldName, indexName, indexName).apply(row));
                }
            }

            try {
                boolean b = bulkRequest.awaitClose(60000, TimeUnit.SECONDS);
                System.out.println("Fra bulkRequest: " + b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            client.admin().indices().prepareRefresh().get();
        }
    }

    private static boolean isIndexingRunning(AdminClient client, String indexName) {
        IndicesStatsResponse indicesStatsResponse =
                client
                        .indices()
                        .prepareStats(indexName)
                        .all()
                        .execute()
                        .actionGet();
        return indicesStatsResponse.getTotal().getIndexing().getTotal().getIndexCurrent() > INTEGER_ZERO;
    }

    private List<Map<String, Object>> transform(List<T> types) {

        List<Map<String, Object>> rows = new ArrayList<>();
        types.forEach(type -> {
            try {
                Map<String, Object> row = new HashMap<>();
                Class<?> thisClass = Class.forName(type.getClass().getName());

                for (Field field : thisClass.getDeclaredFields()) {
                    field.setAccessible(true);

                    Object fieldValue = field.get(type);
                    String fieldName = field.getName();

                    row.put(fieldName, fieldValue);
                }

                rows.add(row);
            } catch (ClassNotFoundException | IllegalAccessException e) {
                log.error("Could not transform List<T> to Indexer preferred Map<String, Object> due to {}", e);
            }
        });

        return rows;
    }

    private static Function<Map<String, Object>, IndexRequest> createConverter(String esFieldId, String indexName, String indextype) {
        return map -> new IndexRequest().id(map.get(esFieldId).toString())
//                                .refresh(true)
                .index(indexName)
                .type(indextype)
                .source(map)
                ;
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
//                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .build();
    }

}

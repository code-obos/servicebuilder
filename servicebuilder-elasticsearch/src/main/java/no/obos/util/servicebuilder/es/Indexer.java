package no.obos.util.servicebuilder.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.addon.ElasticsearchIndexAddon;
import no.obos.util.servicebuilder.es.options.IndexingOptions;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
public class Indexer<T> {

    private final ElasticsearchIndexAddon indexAddon;

    /**
     * Samme som {@link Indexer#index(String, Iterable, Function, IndexingOptions)} med standard-options.
     */
    public void index(String schema, Iterable<T> documents, Function<T, String> idMapper) {
        index(schema, documents, idMapper, IndexingOptions.DEFAULT);
    }

    /**
     * Tilsvarer å hente ut en iterator fra documents med {@link Iterable#iterator()} og så kalle
     * {@link Indexer#index(String, Iterator, Function, IndexingOptions)}.
     */
    public void index(String schema, Iterable<T> documents, Function<T, String> idMapper, IndexingOptions options) {
        index(schema, documents.iterator(), idMapper, options);
    }

    /**
     * Samme som {@link Indexer#index(String, Iterator, Function, IndexingOptions)} med standard-options.
     */
    public void index(String schema, Iterator<T> documentsIterator, Function<T, String> idMapper) {
        index(schema, documentsIterator, idMapper, IndexingOptions.DEFAULT);
    }

    /**
     * Det kan forventes at elementene i documentsIterator blir prosessert i bolker på størrelse med
     * {@link IndexingOptions#bulkSize}. Dermed kan man spare minne ved å la iteratoren laste data lazy.
     * <p>
     * Unikheten til ID-ene som blir generert må forsikres av kalleren, dersom ønskelig. Dette er et designvalg gjort
     * for å støtte lazy-loading av data, samt å gjøre bruken enkel og forutsigbar. Siste dokument i rekken av
     * dokumenter med duplikate ID-er vil være det dokumentet som vil ligge i indeksen etter endt indeksering.
     *
     * @param schema            Beskriver dataene i dokumentene.
     * @param documentsIterator Iterator over dokumenter som skal indekseres.
     * @param idMapper          Mapper dokument til dokument-ID.
     * @param options           Beskriver valg som kan gjøres i forhold til hvordan indekseringen vil oppføre seg.
     */
    public void index(
            String schema,
            Iterator<T> documentsIterator,
            Function<T, String> idMapper,
            IndexingOptions options)
    {
        if (! isIndexingRunning()) {
            prepareIndexing(schema);
            performIndexing(documentsIterator, idMapper, options);
        }
    }

    public boolean indexExists() {
        return getIndicesAdminClient()
                .prepareExists(indexAddon.indexname)
                .get()
                .isExists();
    }

    public String getClusterName() {
        return ElasticsearchUtil.getClusterName(getClient());
    }

    public String getIndexName() {
        return indexAddon.indexname;
    }

    private void prepareIndexing(String schema) {
        if (indexExists()) {
            prepareUpdateIndex(schema);
        } else {
            prepareCreateIndex(schema);
        }
    }

    private void prepareUpdateIndex(String schema) {
        getIndicesAdminClient()
                .preparePutMapping(indexAddon.indexname)
                .setType(indexAddon.indexname)
                .setSource(schema, XContentType.JSON)
                .get();
    }

    private void prepareCreateIndex(String schema) {
        getIndicesAdminClient()
                .prepareCreate(indexAddon.indexname)
                .addMapping(indexAddon.indexname, schema, XContentType.JSON)
                .get();
    }

    private void performIndexing(Iterator<T> documentsIterator, Function<T, String> idMapper, IndexingOptions options) {
        log.info("Starting bulk request on index {} on cluster {}", indexAddon.indexname, getClusterName());

        BulkProcessor bulkProcessor = createBulkProcessor(options);
        ObjectMapper objectMapper = indexAddon.jsonConfig.get();

        documentsIterator.forEachRemaining(document -> bulkProcessor.add(
                createIndexRequest(
                        idMapper.apply(document),
                        transformToJson(document, objectMapper)
                )
        ));

        try {
            boolean completed = bulkProcessor.awaitClose(60000, TimeUnit.SECONDS);
            log.info("Bulk request on index {} on cluster {} completed: {}",
                    indexAddon.indexname, getClusterName(), completed);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        getIndicesAdminClient().prepareRefresh().get();
    }

    private String transformToJson(T document, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private BulkProcessor createBulkProcessor(IndexingOptions options) {
        String clusterName = getClusterName();
        return BulkProcessor.builder(
                getClient(),
                new BulkProcessor.Listener() {
                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.debug("Going to add data to new bulk composed of {} actions on index {} on cluster {}",
                                request.numberOfActions(), indexAddon.indexname, clusterName);
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                        log.debug("Executed bulk composed of {} actions on index {} on cluster {}",
                                request.numberOfActions(), indexAddon.indexname, clusterName);
                    }

                    @Override
                    public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                        log.error("Error executing bulk on index {} on cluster {}",
                                indexAddon.indexname, clusterName, failure);
                    }
                })
                .setBulkActions(options.getBulkSize())
                .setConcurrentRequests(options.getBulkConcurrent())
                .setFlushInterval(TimeValue.timeValueMinutes(30))
                .build();
    }

    private IndexRequest createIndexRequest(String id, String json) {
        return new IndexRequest()
                .id(id)
                .index(indexAddon.indexname)
                .type(indexAddon.indexname)
                .source(json, XContentType.JSON);
    }

    private boolean isIndexingRunning() {
        return indexExists() && numberOfCurrentIndexOperations() > 0;
    }

    private long numberOfCurrentIndexOperations() {
        IndicesStatsResponse indicesStatsResponse = getAdminClient()
                .indices()
                .prepareStats(indexAddon.indexname)
                .all()
                .execute()
                .actionGet();
        return indicesStatsResponse
                .getTotal()
                .getIndexing()
                .getTotal()
                .getIndexCurrent();
    }

    private IndicesAdminClient getIndicesAdminClient() {
        return getAdminClient().indices();
    }

    private AdminClient getAdminClient() {
        return getClient().admin();
    }

    private Client getClient() {
        return indexAddon.elasticsearchAddon.getClient();
    }
}

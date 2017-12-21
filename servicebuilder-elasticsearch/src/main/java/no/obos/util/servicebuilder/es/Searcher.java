package no.obos.util.servicebuilder.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class Searcher<T> {
    private final Client client;
    private final Class<T> indexedClass;
    private final String indexname;
    private final ObjectMapper objectMapper;

    public List<T> query(QueryBuilder queryBuilder) {
        return execute(queryBuilder);
    }

    public List<T> query(String field, String value) {
        return execute(QueryBuilders.queryStringQuery(value).field(field));
    }

    private List<T> execute(QueryBuilder queryBuilder) {
        return performQuery(0).andThen(this::transform).apply(queryBuilder);
    }

    private List<T> transform(SearchResponse searchResponse) {
        return Arrays.stream(searchResponse.getHits().getHits()).map(this::transformHit).distinct().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private T transformHit(SearchHit hit) {
        String json = hit.getSourceAsString();
        try {
            return objectMapper.readValue(json, indexedClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Function<QueryBuilder, SearchResponse> performQuery(int resultSetSize) {
        int finalResultSetSize = resultSetSize <= 0 ? 10000 : resultSetSize;
        return (query) -> client.prepareSearch(indexname).setTypes(indexname)
                                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                .setQuery(query)
                                .setSize(finalResultSetSize)
                                .execute()
                                .actionGet();
    }
}

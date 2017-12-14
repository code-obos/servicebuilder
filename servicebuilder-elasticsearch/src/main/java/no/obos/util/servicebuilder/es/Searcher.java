package no.obos.util.servicebuilder.es;

import lombok.AllArgsConstructor;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Searcher<T> {
    final Client client;
    public final Class<T> indexedClass;

    public ClusterHealthResponse getHealthy() {
        return client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
    }

    public List<T> query(QueryBuilder queryBuilder, Function<SearchHit, T> model) {
        return bla(queryBuilder, model);
    }

    public List<T> query(String field, String value, Function<SearchHit, T> model) {
        return bla(QueryBuilders.termQuery(field, value), model);
    }

    private List<T> bla(QueryBuilder queryBuilder, Function<SearchHit, T> model) {
        return performQuery(0, "", "").andThen(transform(model)).apply(queryBuilder);
    }

    private Function<SearchResponse, List<T>> transform(Function<SearchHit, T> model) {
        return searchResponse -> Arrays.stream(searchResponse.getHits().getHits()).map(model).distinct().collect(Collectors.toList());
    }

    private Function<QueryBuilder, SearchResponse> performQuery(int resultSetSize, String indexname, String indextype) {
        int finalResultSetSize = resultSetSize <= 0 ? Integer.MAX_VALUE : resultSetSize;
        return (query) -> client.prepareSearch(indexname)
                                .setTypes(indextype)
                                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                .setQuery(query)
                                .setSize(finalResultSetSize)
                                .execute()
                                .actionGet();
    }
}

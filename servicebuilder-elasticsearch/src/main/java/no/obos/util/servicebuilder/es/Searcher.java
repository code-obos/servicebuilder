package no.obos.util.servicebuilder.es;

import lombok.AllArgsConstructor;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
public class Searcher<T> {
    final Client client;

    public ClusterHealthResponse getHealthy() {
        return client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
    }

    //    List<T> query (Function<SearchRequestBuilder, SearchRequestBuilder> fun) {
    //        QueryBuilders.matchPhraseQuery()
    //    }
    //
    //    List<T> query (Consumer<QueryBuilders>) {
    //        Inde
    //        selskapSearcher.query(FieldSearch.field("selskapsnummer", "50**").field("postnummer", "0190").maxResults(800).queryTypeAnd(true))
    //        queryBuilder.
    //        List<Selskap> selskaper =
    //                selskapSearcher
    //                        .fieldQuery("selskapsnummer", qb -> qb.largerthan(100))
    //                .fieldQuery("postnummer", qb-> qb.isEven())
    //                .toList();
    //
    //        QueryBuilder qb;
    //        selskapSearcher.query(QB -> {
    //            MetaQuery metaQuery = new MetaQuery();
    //            selskapSearcher.query((qb, metaQuery);
    //        });
    //
    //
    //        selskapSearcher.query((qb, metaQuery) - > {
    //
    //        });
    //
    //    }

}

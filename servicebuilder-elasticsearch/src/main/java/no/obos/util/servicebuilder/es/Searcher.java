package no.obos.util.servicebuilder.es;

import lombok.AllArgsConstructor;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;

@AllArgsConstructor
public class Searcher<T> {
    final Client client;
    public final Class<T> indexedClass;

    public ClusterHealthResponse getHealthy() {
        return client.admin().cluster().health(new ClusterHealthRequest()).actionGet();
    }
}

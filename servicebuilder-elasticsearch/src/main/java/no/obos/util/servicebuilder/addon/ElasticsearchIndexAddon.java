package no.obos.util.servicebuilder.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.metrics.ObosHealthCheckRegistry;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.es.Searcher;
import no.obos.util.servicebuilder.model.Addon;
import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticsearchIndexAddon implements Addon {

    @Wither(AccessLevel.PRIVATE)
    final Client client;

    @Wither(AccessLevel.PRIVATE)
    final String clustername;

    @Wither(AccessLevel.PRIVATE)
    public final String indexname;

    @Wither(AccessLevel.PRIVATE)
    public final Class indexedType;

    private static ElasticsearchIndexAddon defaults = new ElasticsearchIndexAddon(null, null, null, null);

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ObosHealthCheckRegistry.registerElasticSearchClusterCheck("Indexer: ", clustername, indexname, client.admin().cluster());
    }

    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        Map<String, Searcher<?>> searcherMap = new HashMap<>();
        searcherMap.put(indexedType.getName(), new Searcher<>(client));
        serviceConfig.addBinder(binder -> {
            binder.bind(searcherMap).to(new TypeLiteral<Map<String, Searcher>>() {});
            binder.bind(SearcherIndexNameResolver.class).to(JustInTimeInjectionResolver.class);
                }

        );
    }

    public ElasticsearchIndexAddon client(Client client) {
        return withClient(client);
    }

    public static ElasticsearchIndexAddon defaults(String balleIndex, Class indexedType) {
        return defaults
                .withIndexname(balleIndex)
                .withIndexedType(indexedType);
    }


    /**
     * Magic solution to inject senders based on generic message type. Uses hk2 just in time injection.
     * Basically, when hk2 does not find a candidate for injection among bound classes, it asks any just
     * in time injection resolvers if they have an implementation of the class.
     * <p>
     * Senders injected via this resolver is provided in a sender map given by the mq implementation addon
     * (e.g. ActiveMqAddon). Thus the addon must bind the sender map.
     */
    static class SearcherIndexNameResolver implements JustInTimeInjectionResolver {
        @Inject
        ServiceLocator serviceLocator;
        @Inject
        Map<String, Searcher> searcherMap;
        @Override
        public boolean justInTimeResolution(Injectee failedInjectionPoint) {
            String typeName = failedInjectionPoint.getRequiredType().getTypeName();
            if (typeName.startsWith(Searcher.class.getName()) && typeName.contains(">") && typeName.contains("<")) {
                String indexName = typeName.substring(typeName.indexOf('<') + 1, typeName.indexOf('>'));
                ServiceLocatorUtilities.addOneConstant(serviceLocator, searcherMap.get(indexName), "null", failedInjectionPoint.getRequiredType());
                return true;
            }
            return false;
        }
    }
}

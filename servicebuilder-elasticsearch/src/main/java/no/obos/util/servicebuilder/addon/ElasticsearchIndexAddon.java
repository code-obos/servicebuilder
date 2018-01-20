package no.obos.util.servicebuilder.addon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.es.Indexer;
import no.obos.util.servicebuilder.es.Searcher;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.SerializationSpec;
import no.obos.util.servicebuilder.util.JsonUtil;
import no.obos.util.servicebuilder.util.ObosHealthCheckRegistry;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.JustInTimeInjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static no.obos.util.servicebuilder.es.ElasticsearchUtil.getClusterName;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticsearchIndexAddon implements BetweenTestsAddon {

    public final String indexname;

    @Wither(AccessLevel.PRIVATE)
    public final Class indexedType;

    @Wither(AccessLevel.PRIVATE)
    public final ElasticsearchAddon elasticsearchAddon;

    @Wither(AccessLevel.PRIVATE)
    public final boolean doIndexing;

    @Wither(AccessLevel.PRIVATE)
    public final SerializationSpec serializationSpec;

    private static ElasticsearchIndexAddon defaults = new ElasticsearchIndexAddon(null, null, null, false, SerializationSpec.standard);

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        Client client = elasticsearchAddon.getClient();
        ObosHealthCheckRegistry.registerElasticSearchClusterCheck("Indexer: ", getClusterName(client), indexname, client.admin().cluster());
    }

    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        serviceConfig.addBinder(binder -> {
            binder.bind(this).to(ElasticsearchIndexAddon.class).named(indexname);
            binder.bind(SearcherIndexNameResolver.class).to(JustInTimeInjectionResolver.class);
        });
    }

    public static ElasticsearchIndexAddon elasticsearchIndexAddon(String indexName, Class indexedType) {
        return defaults.withIndexname(indexName)
                .withIndexedType(indexedType);
    }

    @Override
    public ElasticsearchIndexAddon initialize(ServiceConfig serviceConfig) {
        ElasticsearchAddon elasticsearchAddon = serviceConfig.addonInstance(ElasticsearchAddon.class);
        if (elasticsearchAddon == null) {
            throw new DependenceException(this.getClass(), ElasticsearchAddon.class, " no ElasticSearchAddon found");
        }
        return this.withElasticsearchAddon(elasticsearchAddon);
    }

    @Override
    public Set<Class<?>> initializeAfter() {
        return ImmutableSet.of(ElasticsearchClientAddon.class);
    }

    private ElasticsearchIndexAddon withIndexname(String indexname2) {
        String indexname = indexname2.toLowerCase();
        return Objects.equals(this.indexname, indexname) ? this : new ElasticsearchIndexAddon(indexname, this.indexedType, this.elasticsearchAddon, this.doIndexing, this.serializationSpec);
    }

    @Override
    public void beforeNextTest() {
        AdminClient admin = elasticsearchAddon.getClient().admin();
        if (admin.indices().prepareExists(indexname).get().isExists()) {
            admin.indices().flush(new FlushRequest(indexname)).actionGet();
        }
    }

    /**
     * Magic solution to inject senders based on generic message type. Uses hk2 just in time injection.
     * Basically, when hk2 does not find a candidate for injection among bound classes, it asks any just
     * in time injection resolvers if they have an implementation of the class.
     */
    static class SearcherIndexNameResolver implements JustInTimeInjectionResolver {
        @Inject
        ServiceLocator serviceLocator;

        @Override
        public boolean justInTimeResolution(Injectee failedInjectionPoint) {
            Type requiredType = failedInjectionPoint.getRequiredType();
            String typeName = requiredType.getTypeName();

            boolean isHandledByMe = !isMainTypeSearcher(typeName) && !isMainTypeIndexer(typeName);
            if (alreadyBound(requiredType) || isHandledByMe) {
                return false;
            }

            List<ElasticsearchIndexAddon> indexAddons = serviceLocator.getAllServices(ElasticsearchIndexAddon.class);

            for (ElasticsearchIndexAddon indexAddon : indexAddons) {
                Class<?> indexedType = indexAddon.indexedType;
                if (indexedType.getTypeName().equals(getIndexedTypeName(typeName))) {
                    Client client = indexAddon.elasticsearchAddon.getClient();
                    if (isMainTypeSearcher(typeName)) {
                        ObjectMapper objectMapper = JsonUtil.createObjectMapper(indexAddon.serializationSpec);
                        Searcher<?> constant = new Searcher<>(client, indexedType, indexAddon.indexname, objectMapper);
                        ServiceLocatorUtilities.addOneConstant(serviceLocator, constant, null, requiredType);
                    } else if (isMainTypeIndexer(typeName) && indexAddon.doIndexing) {
                        Indexer<?> constant = new Indexer<>(indexAddon);
                        ServiceLocatorUtilities.addOneConstant(serviceLocator, constant, null, requiredType);
                    }
                }
            }

            return true;
        }

        private String getIndexedTypeName(String typeName) {
            return typeName.substring(typeName.indexOf("<") + 1, typeName.indexOf(">"));
        }

        private boolean isMainTypeSearcher(String typeName) {
            return typeName.startsWith(Searcher.class.getName()) && typeName.contains(">") && typeName.contains("<");
        }

        private boolean isMainTypeIndexer(String typeName) {
            return typeName.startsWith(Indexer.class.getName()) && typeName.contains(">") && typeName.contains("<");
        }

        private boolean alreadyBound(Type requiredType) {
            return serviceLocator.getAllServices(requiredType).size() > 0;
        }
    }

    public ElasticsearchIndexAddon doIndexing(boolean doIndexing) {
        return withDoIndexing(doIndexing);
    }

    public ElasticsearchIndexAddon serializationSpec(SerializationSpec serializationSpec) {
        return withSerializationSpec(serializationSpec);
    }

}

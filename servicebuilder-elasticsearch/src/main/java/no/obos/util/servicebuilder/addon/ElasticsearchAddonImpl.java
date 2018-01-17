package no.obos.util.servicebuilder.addon;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ElasticsearchAddonImpl implements ElasticsearchAddon {

    public static final String CLUSTER_NAME = "es.cluster.name";
    public static final String CLUSTER_NAME_CLIENT = "es.cluster.clientname";
    public static final String CLUSTER_COORDINATOR_URL = "es.cluster.coordinator.url";
    public static final String CLUSTER_COORDINATOR_PORT = "es.cluster.coordinator.port";

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final Client client;

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final String clustername;
    @Wither(AccessLevel.PRIVATE)
    public final String clientname;
    @Wither(AccessLevel.PRIVATE)
    public final String coordinatorUrl;
    @Wither(AccessLevel.PRIVATE)
    public final int coordinatorPort;

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final boolean unitTest;

    public static ElasticsearchAddonImpl defaults = new ElasticsearchAddonImpl(null, null, null, null, 0, false);


    @Override
    public ElasticsearchAddonImpl initialize(ServiceConfig serviceConfig) {

        Settings settings = Settings.builder()
                .put("cluster.name", clustername)
                .put("node.name", clientname)
                .build();

        InetAddress address = null;
        try {
            address = InetAddress.getByName(coordinatorUrl);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return this.withClient(new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(address, coordinatorPort))
                );
    }


    @Override
    public Addon withProperties(PropertyProvider properties) {

        return this
                .clustername(properties.requireWithFallback(CLUSTER_NAME, clustername))
                .clientname(properties.requireWithFallback(CLUSTER_NAME_CLIENT, clientname))
                .coordinatorUrl(properties.requireWithFallback(CLUSTER_COORDINATOR_URL, coordinatorUrl))
                .coordinatorPort(Integer.parseInt(properties.requireWithFallback(CLUSTER_COORDINATOR_PORT, String.valueOf(coordinatorPort))))
                ;
    }

    public ElasticsearchAddonImpl clustername(String clustername) {
        return withClustername(clustername);
    }


    public ElasticsearchAddonImpl clientname(String clientname) {
        return withClientname(clientname);
    }


    public ElasticsearchAddonImpl coordinatorUrl(String coordinatorUrl) {
        return withCoordinatorUrl(coordinatorUrl);
    }

    public ElasticsearchAddonImpl coordinatorPort(int coordinatorPort) {
        return withCoordinatorPort(coordinatorPort);
    }

    public ElasticsearchAddonImpl unitTest(boolean unitTest) {
        return withUnitTest(unitTest);
    }
}

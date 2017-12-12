package no.obos.util.servicebuilder.addon;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticsearchAddon implements Addon {

    public static final String CLUSTER_NAME = "es.cluster.name";
    public static final String CLUSTER_INDEX_TYPE = "es.cluster.type.";
    public static final String CLUSTER_NAME_CLIENT = "es.cluster.clientname";
    public static final String CLUSTER_BIND_HOST = "es.cluster.bindhost";
    public static final String CLUSTER_PORT_RANGE_HTTP = "es.cluster.port.range.http";
    public static final String CLUSTER_PORT_RANGE_TRANSPORT = "es.cluster.port.range.transport";
    public static final String CLUSTER_PORT_EXPOSED_HTTP = "es.cluster.port.exposed.http";
    public static final String CLUSTER_PORT_EXPOSED_TRANSPORT = "es.cluster.port.exposed.transport";
    public static final String CLUSTER_COORDINATOR_URL = "es.cluster.coordinator.url";
    public static final String CLUSTER_COORDINATOR_PORT = "es.cluster.coordinator.port";
    public static final String CLUSTER_INDEX_NAME = "es.cluster.indexname";

    @Wither(AccessLevel.PRIVATE)
    public final Client client;

    @Wither(AccessLevel.PRIVATE)
    public final String clustername;
    @Wither(AccessLevel.PRIVATE)
    public final String clientname;
    @Wither(AccessLevel.PRIVATE)
    public final String indexType;
    @Wither(AccessLevel.PRIVATE)
    public final String bindHost;
    @Wither(AccessLevel.PRIVATE)
    public final String portRangeHttp;
    @Wither(AccessLevel.PRIVATE)
    public final String portRangeTransport;
    @Wither(AccessLevel.PRIVATE)
    public final String portExposedHttp;
    @Wither(AccessLevel.PRIVATE)
    public final String portExposedTransport;
    @Wither(AccessLevel.PRIVATE)
    public final String coordinatorUrl;
    @Wither(AccessLevel.PRIVATE)
    public final int coordinatorPort;
    @Wither(AccessLevel.PRIVATE)
    public final String indexname;
    @Wither(AccessLevel.PRIVATE)
    public final boolean registerHealthcheck;

    public static ElasticsearchAddon defaults = new ElasticsearchAddon(null, null, null, null, null, null, null, null, null, null, 0, null, true);

    @Override
    public void addToJerseyConfig(JerseyConfig serviceConfig) {
        serviceConfig.addBinder((binder) -> {
            String name = StringUtils.trimToNull(this.clustername);
//            binder.bind(this.mqListener).named(name).to(MessageQueueListener.class);
//            binder.bind(handler).named(name).to(MessageHandler.class);
//            binder.bind(this).named(name).to(ActiveMqListenerAddon.class);
        });

        // Feature is used to start the listeners immediately once dependencies are bound
//        serviceConfig.addRegistations(registrator -> registrator
//                .register(StartListenersFeature.class)
//        );
    }

//    @Override
//    public void addToJettyServer(JettyServer jettyServer) {
//        ObosHealthCheckRegistry.registerElasticSearchClusterCheck("Balle", clustername, indexnames, client.admin().cluster());
//    }

    @Override
    public ElasticsearchAddon initialize(ServiceConfig serviceConfig) {
        String hostname = getHostname();
//        String nodeName = nodeNameSupplier(clientname, hostname).get();

        Settings settings = Settings.builder()
                .put("cluster.name", clustername)
                .put("http.port", portRangeHttp)
                .put("transport.tcp.port", portRangeTransport)
//                .put("node.name", nodeName)
                .put("network.bind_host", bindHost)
//                .put("http.publish_port", portExposedHttp)
//                .put("http.publish_host", hostname)
                .put("transport.bind_host", bindHost)
//                .put("transport.publish_port", portExposedTransport)
//                .put("transport.publish_host", hostname)
                .build();

        InetAddress address = null;
        try {
            address = InetAddress.getByName(coordinatorUrl);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return this.client(new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(address, coordinatorPort)));
    }

    private static Function<List<InterfaceAddress>, String> getHostName = list ->  list.stream()
            .filter(a -> ! a.getAddress().isLoopbackAddress())
            .filter(a -> ! a.getAddress().getHostAddress().contains(":"))
            .findFirst()
            .map(interfaceAddress -> interfaceAddress.getAddress().getCanonicalHostName())
            .orElse("");

    private static String getHostname() {
        String exposedVariable = System.getenv("HOST");
        String localHostname = "";
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (! networkInterface.getName().startsWith("docker") &&
                        ! networkInterface.getName().startsWith("lo") &&
                        ! networkInterface.getName().startsWith("veth")
                        ) {
                    localHostname = getHostName.apply(networkInterface.getInterfaceAddresses());
                    break;
                }
            }

        } catch (SocketException e) {
//            LOGGER.error("Could not get hostname", e);
        }
        if (exposedVariable == null || exposedVariable.length() <= 0) {
            exposedVariable = localHostname;
        }
        return exposedVariable;
    }

//    private static Supplier<String> nodeNameSupplier(String serviceName, String hostname) {
//        String finalHostname = "-" + hostname;
//        return () -> {
//            String uuid = UUID.randomUUID().toString();
//            String nodeName = uuid.substring(uuid.lastIndexOf('-'));
//
//            return serviceName + finalHostname + nodeName;
//        };
//    }

    public ElasticsearchAddon client(Client client) {
        return withClient(client);
    }

    @Override
    public Addon withProperties(PropertyProvider properties) {
        String prefix = Strings.isNullOrEmpty(indexname) ? "" : indexname + ".";

        properties.failIfNotPresent(
                prefix + CLUSTER_NAME,
                prefix + CLUSTER_INDEX_TYPE,
                prefix + CLUSTER_NAME_CLIENT,
                prefix + CLUSTER_BIND_HOST,
                prefix + CLUSTER_PORT_RANGE_HTTP,
                prefix + CLUSTER_PORT_RANGE_TRANSPORT,
                prefix + CLUSTER_PORT_EXPOSED_HTTP,
                prefix + CLUSTER_PORT_EXPOSED_TRANSPORT,
                prefix + CLUSTER_COORDINATOR_URL,
                prefix + CLUSTER_COORDINATOR_PORT,
                prefix + CLUSTER_INDEX_NAME
        );

        return this
                .clustername(properties.get(prefix + CLUSTER_NAME))
                .indexType(properties.get(prefix + CLUSTER_INDEX_TYPE))
                .clientname(properties.get(prefix + CLUSTER_NAME_CLIENT))
                .bindHost(properties.get(prefix + CLUSTER_BIND_HOST))
                .portRangeHttp(properties.get(prefix + CLUSTER_PORT_RANGE_HTTP))
                .portRangeTransport(properties.get(prefix + CLUSTER_PORT_RANGE_TRANSPORT))
                .portExposedHttp(properties.get(prefix + CLUSTER_PORT_EXPOSED_HTTP))
                .portExposedTransport(properties.get(prefix + CLUSTER_PORT_EXPOSED_TRANSPORT))
                .coordinatorUrl(properties.get(prefix + CLUSTER_COORDINATOR_URL))
                .coordinatorPort(Integer.parseInt(properties.get(prefix + CLUSTER_COORDINATOR_PORT)))
                .indexname(properties.get(prefix + CLUSTER_INDEX_NAME))
                ;
    }

    public ElasticsearchAddon clustername(String clustername) {
        return withClustername(clustername);
    }

    public ElasticsearchAddon indexType(String indexType) {
        return withIndexType(indexType);
    }

    public ElasticsearchAddon clientname(String clientname) {
        return withClientname(clientname);
    }

    public ElasticsearchAddon bindHost(String bindHost) {
        return withBindHost(bindHost);
    }

    public ElasticsearchAddon portRangeHttp(String portRangeHttp) {
        return withPortRangeHttp(portRangeHttp);
    }

    public ElasticsearchAddon portRangeTransport(String portRangeTransport) {
        return withPortRangeTransport(portRangeTransport);
    }

    public ElasticsearchAddon portExposedHttp(String portExposedHttp) {
        return withPortExposedHttp(portExposedHttp);
    }

    public ElasticsearchAddon portExposedTransport(String portExposedTransport) {
        return withPortExposedTransport(portExposedTransport);
    }

    public ElasticsearchAddon coordinatorUrl(String coordinatorUrl) {
        return withCoordinatorUrl(coordinatorUrl);
    }

    public ElasticsearchAddon coordinatorPort(int coordinatorPort) {
        return withCoordinatorPort(coordinatorPort);
    }

    public ElasticsearchAddon indexname(String indexname) {
        return withIndexname(indexname);
    }

    public ElasticsearchAddon registerHealthcheck(boolean registerHealthcheck) {
        return withRegisterHealthcheck(registerHealthcheck);
    }
}

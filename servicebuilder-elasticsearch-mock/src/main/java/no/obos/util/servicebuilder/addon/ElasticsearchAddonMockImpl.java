package no.obos.util.servicebuilder.addon;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.ServiceConfig;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.InternalSettingsPreparer;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@Getter
@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ElasticsearchAddonMockImpl implements ElasticsearchAddon {
    public final static ElasticsearchAddonMockImpl defaults = new ElasticsearchAddonMockImpl(null, null, null, null, true);

    @Wither(AccessLevel.PRIVATE)
    private final Path path;

    @Wither(AccessLevel.PRIVATE)
    private final Node node;

    @Wither(AccessLevel.PRIVATE)
    private final Client client;

    @Wither(AccessLevel.PRIVATE)
    private final String clustername;

    @Getter
    @Wither(AccessLevel.PRIVATE)
    public final boolean unitTest;

    @Override
    public ElasticsearchAddonMockImpl initialize(ServiceConfig serviceConfig) {
        Path path;
        Node node;

        try {
            path = Files.createTempDirectory("elasticsearch-data");
            node = elasticSearchTestNode(path);
        } catch (NodeValidationException | IOException e) {
            throw new RuntimeException(e);
        }

        Settings settings = Settings.builder()
                                    .put("cluster.name", "test-search-api-5-local_junit")
                                    .put("node.name", "elastic-client")
                                    .build();

        InetAddress address = null;
        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return this.withNode(node)
                   .withPath(path)
                   .withClient(
                           new PreBuiltTransportClient(settings)
                                   .addTransportAddress(new TransportAddress(address, 9311))
                   );
    }

    @Override
    public void cleanUp() {
        try {
            node.close();
        } catch (IOException e) {
            log.warn("Could not close node");
        }
        client.close();
        deleteRecursively(path);
    }

    private void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path)
                        .forEach(this::deleteRecursively);
            }
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Node elasticSearchTestNode(Path path) throws NodeValidationException {
        return new MyNode(
                Settings.builder()
                        .put("http.enabled", "true")
                        .put("path.home", path.toString())
                        .put("cluster.name", "test-search-api-5-local_junit")
                        .put("http.port", 9211)
                        .put("transport.tcp.port", 9311)
                        .put("http.publish_port", 9211)
                        .put("http.publish_host", "127.0.0.1")
                        .put("transport.bind_host", "127.0.0.1")
                        .put("transport.publish_port", 9311)
                        .put("transport.publish_host", "127.0.0.1")
                        .build(),
                Lists.newArrayList(Netty4Plugin.class)
        ).start();
    }

    private static class MyNode extends Node {
        MyNode(Settings preparedSettings, Collection<Class<? extends Plugin>> classpathPlugins) {
            super(InternalSettingsPreparer.prepareEnvironment(preparedSettings, null), classpathPlugins);
        }
    }
}

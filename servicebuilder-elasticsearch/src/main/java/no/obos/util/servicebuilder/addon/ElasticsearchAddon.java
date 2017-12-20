package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.model.Addon;
import org.elasticsearch.client.Client;

public interface ElasticsearchAddon extends Addon {

    String getClustername();

    Client getClient();

    boolean isUnitTest();
}

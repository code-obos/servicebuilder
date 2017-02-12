package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.stream.Collectors;

import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_CONTEXT_PATH;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_PORT;

@AllArgsConstructor
public class TestServiceRunner {
    final ServiceConfig serviceConfig;
    final JerseyConfig jerseyConfig;

    public TestServiceRunner(ServiceConfig serviceConfigRaw) {
        serviceConfig = ServiceConfigInitializer.addContext(serviceConfigRaw);
        jerseyConfig = new JerseyConfig(serviceConfig.serviceDefinition);
    }


    //    Map<Addon2, AddonRuntime2> runtimes = Maps.newHashMap();
    public TestServiceRunner init() {
        jerseyConfig
                .addRegistrators(serviceConfig.registrators)
                .addBinders(serviceConfig.binders);
        serviceConfig.addons.forEach(it -> it.addToJerseyConfig(jerseyConfig));
        return this;
    }

    public ResourceConfig getResourceConfig() {
        return jerseyConfig.getResourceConfig();
    }
}

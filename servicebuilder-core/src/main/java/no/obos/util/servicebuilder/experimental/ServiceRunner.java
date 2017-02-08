package no.obos.util.servicebuilder.experimental;

import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.AppConfigBackedPropertyProvider;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.PropertyProvider;

import java.util.stream.Collectors;

import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_CONTEXT_PATH;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_PORT;

@AllArgsConstructor
public class ServiceRunner {
    final ServiceConfig serviceConfig;
    final JettyServer jettyServer;
    final JerseyConfig jerseyConfig;
    JettyServer.Configuration jettyConfig;

    public ServiceRunner(ServiceConfig serviceConfigRaw) {
        PropertyProvider properties = AppConfigBackedPropertyProvider.fromJvmArgs(serviceConfigRaw.serviceDefinition);
        ServiceConfig serviceConfigWithProps = serviceConfigRaw.toBuilder().clearAddons().addons(serviceConfigRaw.addons.stream().map(it -> it.withProperties(properties)).collect(Collectors.toList())).build();
        serviceConfig = ServiceConfigInitializer.addContext(serviceConfigWithProps);
        properties.failIfNotPresent(CONFIG_KEY_SERVER_PORT, CONFIG_KEY_SERVER_CONTEXT_PATH);
        jerseyConfig = new JerseyConfig(serviceConfig.serviceDefinition);
        jettyConfig = JettyServer.Configuration.builder()
                .bindPort(Integer.valueOf(properties.get(CONFIG_KEY_SERVER_PORT)))
                .contextPath(properties.get(CONFIG_KEY_SERVER_CONTEXT_PATH))
                .build();
        jettyServer = new JettyServer(jettyConfig, jerseyConfig);
    }


    //    Map<Addon2, AddonRuntime2> runtimes = Maps.newHashMap();
    public ServiceRunner start() {
        jerseyConfig
                .addRegistrators(serviceConfig.registrators)
                .addBinders(serviceConfig.binders);
        serviceConfig.addons.forEach(it -> it.addToJerseyConfig(jerseyConfig));
        serviceConfig.addons.forEach(it -> it.addToJettyServer(jettyServer));
        jettyServer.start();
        return this;
    }

    public void join() {
        jettyServer.join();
    }
}

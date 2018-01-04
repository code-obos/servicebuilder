package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.obos.util.servicebuilder.config.PropertyMap;
import no.obos.util.servicebuilder.model.PropertyProvider;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static java.util.stream.Collectors.toList;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_CONTEXT_PATH;
import static no.obos.util.servicebuilder.JettyServer.CONFIG_KEY_SERVER_PORT;

@Slf4j
@AllArgsConstructor
public class ServiceRunner {
    final ServiceConfig serviceConfig;
    final JettyServer jettyServer;
    final JerseyConfig jerseyConfig;
    JettyServer.Configuration jettyConfig;

    public ServiceRunner(ServiceConfig serviceConfigRaw, PropertyProvider properties) {
        serviceConfigRaw = serviceConfigRaw.addPropertiesAndApplyToBindings(properties);
        ServiceConfig serviceConfigWithProps = serviceConfigRaw
                .withAddons(ImmutableList.copyOf(serviceConfigRaw
                        .addons.stream()
                        .map(it -> it.withProperties(properties))
                        .collect(toList()
                        ))
                );
        serviceConfig = ServiceConfigInitializer.finalize(serviceConfigWithProps);
        properties.failIfNotPresent(CONFIG_KEY_SERVER_PORT, CONFIG_KEY_SERVER_CONTEXT_PATH);
        jerseyConfig = new JerseyConfig(serviceConfig.serviceDefinition);
        jettyConfig = JettyServer.Configuration.builder()
                .bindPort(Integer.valueOf(properties.get(CONFIG_KEY_SERVER_PORT)))
                .contextPath(properties.get(CONFIG_KEY_SERVER_CONTEXT_PATH))
                .build();
        jettyServer = new JettyServer(jettyConfig, jerseyConfig);

    }

    public static ServiceRunner defaults(ServiceConfig serviceConfig, Class<?> versionedClass) {
        PropertyProvider properties = PropertyMap.fromJvmArgs();
        return new ServiceRunner(serviceConfig, properties);
    }

    public static ServiceRunner defaults(ServiceConfig serviceConfig) {

        Class<?> versionedClass;
        if (serviceConfig.serviceDefinition.getClass().getCanonicalName() != null) {
            versionedClass = serviceConfig.serviceDefinition.getClass();
        } else {
            throw new RuntimeException("Could not find version from class. Please provide a static class in service project (e.g. Servicerunner.defaults(<serviceConfig>, Main.class)");
        }
        PropertyMap properties = PropertyMap.fromJvmArgs();

        //TODO:
        //Constants.CONFIG_KEY_SERVICE_VERSION
        return new ServiceRunner(serviceConfig, properties);
    }

    public static ServiceRunner defaults(ServiceConfig serviceConfig, PropertyProvider properties) {
        return new ServiceRunner(serviceConfig, properties);
    }

    //    Map<Addon2, AddonRuntime2> runtimes = Maps.newHashMap();
    public ServiceRunner start() {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
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

    public void stop() {
        serviceConfig.addons.forEach(addon -> {
            try {
                addon.cleanUp();
            } catch (RuntimeException ex) {
                log.error("Exception during cleanup", ex);
            }
        });
        jettyServer.stop();
    }
}

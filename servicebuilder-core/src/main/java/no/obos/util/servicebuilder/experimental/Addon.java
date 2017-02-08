package no.obos.util.servicebuilder.experimental;

import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.PropertyProvider;

public interface Addon {
    default Addon withProperties(PropertyProvider properties) {
        return this;
    }

    default Addon withDependencies(ServiceConfig serviceConfig) {
        return this;
    }

    default void addToJerseyConfig(JerseyConfig serviceConfig) {
    }

    default void addToJettyServer(JettyServer serviceConfig) {
    }
}

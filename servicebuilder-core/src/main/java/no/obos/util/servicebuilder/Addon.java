package no.obos.util.servicebuilder;

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

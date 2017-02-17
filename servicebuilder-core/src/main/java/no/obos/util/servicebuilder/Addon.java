package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

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

    default Set<Class<?>> startBefore() {return ImmutableSet.of();}
    default Set<Class<?>> startAfter() {return ImmutableSet.of();}
}

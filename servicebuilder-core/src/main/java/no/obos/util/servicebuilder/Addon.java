package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public interface Addon {
    default Addon withProperties(PropertyProvider properties) {
        return this;
    }

    default Addon finalize(ServiceConfig serviceConfig) {
        return this;
    }

    default void addToJerseyConfig(JerseyConfig serviceConfig) {
    }

    default void addToJettyServer(JettyServer serviceConfig) {
    }

    default Set<Class<?>> finalizeBefore() {return ImmutableSet.of();}

    default Set<Class<?>> finalizeAfter() {return ImmutableSet.of();}
}

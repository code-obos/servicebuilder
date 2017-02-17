package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public interface Addon {
    /**
     * Lets addon access application properties. Should return a clone of the addon with new configuration based on properties, or this if no property changes.
     *
     * runs before finalize, addToJerseyConfig and addToJettyServer
     */
    default Addon withProperties(PropertyProvider properties) {
        return this;
    }

    /**
     * Lets addon initialize state that will be used in runtime, and pull state from already finalized addons.
     * Should return a clone of the addon with the new state.
     * By convention, an Addon should store its state in a nested class, Runtime, so it is accessible from other addons
     *
     * Runs before addToJerseyConfig and addToJettyServer
     */
    default Addon finalize(ServiceConfig serviceConfig) {
        return this;
    }

    /**
     * Modifies JerseyConfig to incorporate addon.
     * Runs before addToJettyServer
     */
    default void addToJerseyConfig(JerseyConfig serviceConfig) {
    }

    /**
     * Modifies jetty to incorporate addon.
     */
    default void addToJettyServer(JettyServer serviceConfig) {
    }

    /**
     * Returns a list of addon classes that the addon should be finalized after. Thus this addon
     * may utilize said addons in its finalize step.
     *
     * WARNING: dependency resolution is not transient. Thus if A->B->C, A.finalizeAfter should return (B,C) and B.finalizeafter should return (C).
     *
     */
    default Set<Class<?>> finalizeAfter() {return ImmutableSet.of();}
}

package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.model.Addon;

/**
 * Addon allows several instances, using name to distinguish (e.g. several database connections)
 */
public interface NamedAddon extends Addon {
    String getName();
}

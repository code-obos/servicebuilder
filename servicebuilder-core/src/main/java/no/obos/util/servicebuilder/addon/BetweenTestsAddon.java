package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.model.Addon;

/**
 * Requires synchonization between consequetive tests
 */
public interface BetweenTestsAddon extends Addon {
    void beforeNextTest();
}

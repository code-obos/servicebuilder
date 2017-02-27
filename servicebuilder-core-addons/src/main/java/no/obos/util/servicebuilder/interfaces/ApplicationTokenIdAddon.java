package no.obos.util.servicebuilder.interfaces;

import no.obos.util.servicebuilder.model.Addon;

import java.util.function.Supplier;

public interface ApplicationTokenIdAddon extends Addon {
    Supplier<String> getApptokenIdSupplier();
}

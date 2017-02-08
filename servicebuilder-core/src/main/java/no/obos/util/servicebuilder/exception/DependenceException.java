package no.obos.util.servicebuilder.exception;

import lombok.AllArgsConstructor;
import no.obos.util.servicebuilder.Addon;

@AllArgsConstructor
public class DependenceException extends RuntimeException {
    public final Class<Addon> dependency;
}

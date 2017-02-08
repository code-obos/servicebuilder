package no.obos.util.servicebuilder.experimental;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DependenceException extends RuntimeException {
    public final Class<Addon> dependency;
}

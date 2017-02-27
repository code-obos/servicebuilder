package no.obos.util.servicebuilder.exception;

import no.obos.util.servicebuilder.model.Addon;

public class DependenceException extends RuntimeException {
    public final Class<? extends Addon> dependent;
    public final Class<? extends Addon> independent;
    public final String message;

    public DependenceException(Class<? extends Addon> dependent, Class<? extends Addon> independent, String message) {
        super(dependent + "reported dependence on " + independent + " message: " + message);
        this.dependent = dependent;
        this.independent = independent;
        this.message = message;
    }

    public DependenceException(Class<? extends Addon> dependent, Class<? extends Addon> independent) {
        super(dependent + "reported dependence on " + independent);
        this.dependent = dependent;
        this.independent = independent;
        this.message = null;
    }
}

package no.obos.util.servicebuilder.authorization;

import javax.security.auth.Subject;
import java.security.Principal;

public interface UibBruker extends Principal {
    boolean isUserInRole(String role);

    default boolean implies(Subject subject) {
        return false;
    }
}

package no.obos.util.servicebuilder.usertoken;

import javax.security.auth.Subject;
import java.security.Principal;

/*
 * Representerer en bruker fra User Identity Backend
 */
public interface UibBruker extends Principal {
    boolean isUserInRole(String role);

    default boolean implies(Subject subject) {
        return false;
    }
}

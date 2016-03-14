package no.obos.util.servicebuilder.usertoken;

import no.obos.iam.tokenservice.UserRole;

/**
 * Mapper-interface fra uib-rolle til javax-rolle.
 */
public interface UibToJavaxRole {
    /**
     * @return Navn på javax-rolle (stringen du skriver i RolesAllowed-anotasjonen
     */
    String getJavaxRoleName();

    /**
     * @param role Uib-rolle
     * @return Tilsier uib-rollen at brukeren skal ha denne javax-rollen?
     */
    boolean tilgangForUibRolle(UserRole role);
}

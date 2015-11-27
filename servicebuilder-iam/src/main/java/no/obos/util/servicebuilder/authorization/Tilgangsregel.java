package no.obos.util.servicebuilder.authorization;

import no.obos.iam.tokenservice.UserRole;

/**
 * Returnerer en tilgang (strengen du legger i sikkerhetsannotasjonene) hvis uib-rollen tilsier tilgang.
 * Hvis ikke returneres null. En enkelt tilgangsregel kan ikke returnere flere tilganger for samme uib-rolle, men
 * du kan bruke flere tilgangsregler.
 */
public interface Tilgangsregel {
    String tilgangForUibRolle(UserRole role);
}

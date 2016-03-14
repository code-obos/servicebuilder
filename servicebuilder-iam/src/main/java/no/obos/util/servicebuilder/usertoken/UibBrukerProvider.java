package no.obos.util.servicebuilder.usertoken;

import no.obos.iam.tokenservice.UserToken;

/**
 * Implementerer mapper fra usertoken til UibBruker. UibBruker sjekker inneholder informasjon om bruker, og har en sjekk
 * for om bruker har tilgang til ressurser. Aarsregnskapsplanlegging er referanseimplementasjon med RolemappedUibBruker.provider().
 */
public interface UibBrukerProvider {
    UibBruker newUibBruker(UserToken userToken);
}

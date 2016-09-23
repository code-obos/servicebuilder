package no.obos.util.servicebuilder.usertoken;

import com.google.common.collect.ImmutableSet;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

/**
 * Klasse som implementerer vanlige tilgangssjekker på UserToken
 */
public class Tilgangssjekker {
    private UserToken userToken;
    private final ImmutableSet<UibRolle> uibRoller;

    public Tilgangssjekker(UserToken userToken) {
        this.userToken = userToken;
        ImmutableSet.Builder<UibRolle> uibRollerBuilder = ImmutableSet.builder();
        for (UserRole tmpRole : userToken.getRoles()) {
            UibRolle uibRolle = new UibRolle(tmpRole.getIdentity().getOrgId(), tmpRole.getIdentity().getAppId(), tmpRole.getIdentity().getName(), tmpRole.getValue());
            uibRollerBuilder.add(uibRolle);
        }
        this.uibRoller = uibRollerBuilder.build();
    }

    public ImmutableSet<UibRolle> getUibRoller() {
        return uibRoller;
    }

    public boolean harTilgang(String appId, String orgId, String rollenavn) {
        for (UibRolle rolle : uibRoller) {
            if (appId.equalsIgnoreCase(rolle.getAppId()) && orgId.equalsIgnoreCase(rolle.getOrgId()) && rollenavn.equalsIgnoreCase(rolle.getNavn())) {
                return true;
            }
        }

        return false;
    }
}

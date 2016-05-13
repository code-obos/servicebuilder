package no.obos.util.servicebuilder.usertoken;

import no.obos.iam.tokenservice.UserRole;

public class UibToJavaxRoleMockImpl implements UibToJavaxRole {
    private String rollenavn;

    public UibToJavaxRoleMockImpl(String rollenavn) {
        this.rollenavn = rollenavn;
    }

    @Override
    public String getJavaxRoleName() {
        return rollenavn;
    }

    @Override
    public boolean tilgangForUibRolle(UserRole userRole) {
        return rollenavn.equalsIgnoreCase(userRole.getIdentity().getName());
    }
}

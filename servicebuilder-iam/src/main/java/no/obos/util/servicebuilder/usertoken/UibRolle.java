package no.obos.util.servicebuilder.usertoken;

import lombok.Getter;

/**
 * En rolle fra User Identity Backend
 */
@Getter
public class UibRolle {
    private final String orgId;
    private final String appId;
    private final String navn;
    private final String verdi;

    public UibRolle(String orgId, String appId, String navn, String verdi) {
        this.orgId = orgId;
        this.appId = appId;
        this.navn = navn;
        this.verdi = verdi;
    }
}

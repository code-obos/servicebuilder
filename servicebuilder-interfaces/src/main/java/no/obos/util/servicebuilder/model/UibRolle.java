package no.obos.util.servicebuilder.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * En rolle fra User Identity Backend
 */
@Builder
@ToString
@EqualsAndHashCode
public class UibRolle {
    public final String orgId;
    public final String appId;
    public final String navn;
    public final String verdi;

    public UibRolle(String orgId, String appId, String navn, String verdi) {
        this.orgId = orgId;
        this.appId = appId;
        this.navn = navn;
        this.verdi = verdi;
    }


}

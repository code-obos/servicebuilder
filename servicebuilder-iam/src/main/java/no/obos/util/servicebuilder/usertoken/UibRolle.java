package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.iam.tokenservice.UserRole;

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

    public static UibRolle ofUserRole(UserRole userRole) {
        return UibRolle.builder()
                .orgId(Strings.emptyToNull(userRole.getIdentity().getOrgId()))
                .appId(Strings.emptyToNull(userRole.getIdentity().getAppId()))
                .navn(Strings.emptyToNull(userRole.getIdentity().getName()))
                .verdi(Strings.emptyToNull(userRole.getValue()))
                .build();
    }

}

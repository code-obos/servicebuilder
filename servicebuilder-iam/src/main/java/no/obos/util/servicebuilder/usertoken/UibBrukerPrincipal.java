package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.ToString;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.model.UibBruker;
import no.obos.util.servicebuilder.model.UibRolle;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.stream.Collectors;

@AllArgsConstructor
@ToString
public class UibBrukerPrincipal implements Principal {
    public final UibBruker uibBruker;


    @Override
    public String getName() {
        return (Strings.nullToEmpty(uibBruker.fornavn) + " " + Strings.nullToEmpty(uibBruker.etternavn)).trim();
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    public static UibBrukerPrincipal ofUserToken(UserToken userToken) {
        return new UibBrukerPrincipal(uibBrukerOfUserToken(userToken));
    }

    private static UibBruker uibBrukerOfUserToken(UserToken userToken) {
        return UibBruker.builder()
                .personid(userToken.getPersonid())
                .fornavn(userToken.getFornavn())
                .etternavn(userToken.getEtternavn())
                .userTokenId(userToken.getTokenId())
                .adBrukernavn(
                        userToken.getRoles().stream()
                                .filter(ur -> "active_directory_user_id".equals(ur.getIdentity().getName()))
                                .findFirst()
                                .map(UserRole::getValue)
                                .map(username -> username.trim().toUpperCase())
                                .orElse(null)
                )
                .roller(ImmutableList.copyOf(
                        userToken.getRoles().stream()
                                .map(UibBrukerPrincipal::ofUserRole)
                                .collect(Collectors.toList())
                        )
                )
                .build();
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

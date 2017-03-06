package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.stream.Collectors;

/*
 * Representerer en bruker fra User Identity Backend
 */
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class UibBruker implements Principal {
    public final String personid;
    public final String fornavn;
    public final String etternavn;
    public final String adBrukernavn;
    public final String userTokenId;
    public final ImmutableList<UibRolle> roller;


    public static UibBruker ofUserToken(UserToken userToken) {
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
                .roller(ImmutableList.copyOf(userToken.getRoles().stream().map(UibRolle::ofUserRole).collect(Collectors.toList())))
                .build();
    }

    @Override
    public String getName() {
        return (Strings.nullToEmpty(fornavn) + " " + Strings.nullToEmpty(etternavn)).trim();
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}

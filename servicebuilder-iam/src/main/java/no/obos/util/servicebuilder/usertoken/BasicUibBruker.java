package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

import java.util.Optional;

import static com.google.common.collect.ImmutableSet.builder;


/**
 * UibBruker med basis informasjon og tilgang basert på regler over uib-roller
 */
@EqualsAndHashCode
public class BasicUibBruker implements UibBruker {
    public final String personid;
    public final String fornavn;
    public final String etternavn;
    public final String adBrukernavn;
    public final String userTokenId;
    public final ImmutableSet<UibRolle> uibRoller;

    private final ImmutableSet<String> tilganger;

    public BasicUibBruker(UserToken userToken, ImmutableList<UibToJavaxRole> tilganger) {
        this.personid = userToken.getPersonid();
        this.fornavn = userToken.getFornavn();
        this.etternavn = userToken.getEtternavn();
        this.userTokenId = userToken.getTokenId();

        Optional<UserRole> userRole = userToken.getRoles().stream()
                .filter(ur -> "active_directory_user_id".equals(ur.getIdentity().getName())).findFirst();

        if (userRole.isPresent()) {
            this.adBrukernavn = userRole.get().getValue().toUpperCase();
        } else {
            this.adBrukernavn = null;
        }

        ImmutableSet.Builder<UibRolle> uibRollerBuilder = ImmutableSet.<UibRolle>builder();
        for (UserRole tmpRole : userToken.getRoles()) {
            UibRolle uibRolle = new UibRolle(tmpRole.getIdentity().getOrgId(), tmpRole.getIdentity().getAppId(), tmpRole.getIdentity().getName(), tmpRole.getValue());
            uibRollerBuilder.add(uibRolle);
        }
        this.uibRoller = uibRollerBuilder.build();

        ImmutableSet.Builder<String> allowedRolesBuilder = ImmutableSet.<String>builder();
        for (UibToJavaxRole mapper : tilganger) {
            for (UserRole role : userToken.getRoles()) {
                boolean match = mapper.tilgangForUibRolle(role);
                if (match) {
                    allowedRolesBuilder.add(mapper.getJavaxRoleName().toUpperCase());
                }
            }
        }
        this.tilganger = allowedRolesBuilder.build();
    }

    @Override
    public boolean isUserInRole(String tilgang) {
        return tilganger.contains(Strings.nullToEmpty(tilgang).toUpperCase());
    }

    @Override public String getName() {
        return (Strings.nullToEmpty(fornavn) + " " + Strings.nullToEmpty(etternavn)).trim();
    }

    public boolean harTilgang(String appId, String orgId, String rollenavn) {
        for (UibRolle rolle : uibRoller) {
            if (rolle.getAppId() == null || rolle.getOrgId() == null || rolle.getNavn() == null) {
                continue;
            }
            if (rolle.getAppId().equalsIgnoreCase(appId) && rolle.getOrgId().equalsIgnoreCase(orgId) && rolle.getNavn().equalsIgnoreCase(rollenavn)) {
                return true;
            }
        }

        return false;
    }

    public static UibBrukerProvider provider(Iterable<UibToJavaxRole> tilgangMappers) {
        return new BasicUibBrukerProvider(ImmutableList.copyOf(tilgangMappers));
    }

    public static UibBrukerProvider provider(UibToJavaxRole... tilgangMappers) {
        return new BasicUibBrukerProvider(ImmutableList.copyOf(tilgangMappers));
    }

    @AllArgsConstructor
    public static class BasicUibBrukerProvider implements UibBrukerProvider {
        final ImmutableList<UibToJavaxRole> tilganger;

        @Override public UibBruker newUibBruker(UserToken userToken) {
            return new BasicUibBruker(userToken, tilganger);
        }
    }
}

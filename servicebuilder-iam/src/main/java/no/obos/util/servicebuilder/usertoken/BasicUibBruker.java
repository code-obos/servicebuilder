package no.obos.util.servicebuilder.usertoken;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

import java.util.List;
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

    private final Tilgangssjekker tilgangssjekker;
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

        tilgangssjekker = new Tilgangssjekker(userToken);

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

    public ImmutableSet<UibRolle> getUibRoller() {
        return tilgangssjekker.getUibRoller();
    }

    public boolean harTilgang(String appId, String orgId, String rollenavn) {
        return tilgangssjekker.harTilgang(appId, orgId, rollenavn);
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

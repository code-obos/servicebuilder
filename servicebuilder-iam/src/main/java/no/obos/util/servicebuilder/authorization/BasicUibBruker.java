package no.obos.util.servicebuilder.authorization;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import no.obos.iam.tokenservice.UserRole;
import no.obos.iam.tokenservice.UserToken;

import java.util.Optional;


/**
 * UibBruker med basisk informasjon og tilgang basert på regler over uib-roller
 */
public class BasicUibBruker implements UibBruker {

    public final String personid;
    public final String fornavn;
    public final String etternavn;
    public final String adBrukernavn;
    public final ImmutableSet<String> tilganger;

    public BasicUibBruker(UserToken userToken, ImmutableList<Tilgangsregel> tilganger) {
        this.personid = userToken.getPersonid();
        this.fornavn = userToken.getFornavn();
        this.etternavn = userToken.getEtternavn();

        Optional<UserRole> userRole = userToken.getRoles().stream()
                .filter(ur -> "active_directory_user_id".equals(ur.getIdentity().getName())).findFirst();

        if (userRole.isPresent()) {
            this.adBrukernavn = userRole.get().getValue().toUpperCase();
        } else {
            this.adBrukernavn = null;
        }

        ImmutableSet.Builder<String> allowedRolesBuilder = ImmutableSet.<String>builder();
        for (Tilgangsregel mapper : tilganger) {
            for (UserRole role : userToken.getRoles()) {
                String allowedRole = mapper.tilgangForUibRolle(role);
                if (allowedRole != null) {
                    allowedRolesBuilder.add(allowedRole.toUpperCase());
                }
            }
        }
        this.tilganger = allowedRolesBuilder.build();
    }

    @Override public boolean isUserInRole(String tilgang) {
        return tilganger.contains(Strings.nullToEmpty(tilgang).toUpperCase());
    }

    @Override public String getName() {
        return (Strings.nullToEmpty(fornavn) + " " + Strings.nullToEmpty(etternavn)).trim();
    }

    public static UibBrukerProvider provider(Iterable<Tilgangsregel> tilgangMappers) {
        return new UibBrukerProvider() {
            final ImmutableList<Tilgangsregel> tilganger = ImmutableList.copyOf(tilgangMappers);

            @Override public UibBruker newUibBruker(UserToken userToken) {
                return new BasicUibBruker(userToken, tilganger);
            }

        };
    }

    public static UibBrukerProvider provider(Tilgangsregel[] tilgangMappers) {
        return new UibBrukerProvider() {
            final ImmutableList<Tilgangsregel> tilganger = ImmutableList.copyOf(tilgangMappers);

            @Override public UibBruker newUibBruker(UserToken userToken) {
                return new BasicUibBruker(userToken, tilganger);
            }

        };
    }
}

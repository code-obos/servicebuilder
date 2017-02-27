package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.usertoken.UibBruker;
import no.obos.util.servicebuilder.usertoken.UibBrukerInjectionFactory;
import no.obos.util.servicebuilder.usertoken.UibRolle;
import no.obos.util.servicebuilder.usertoken.UserTokenFasttrackFilter;
import no.obos.util.servicebuilder.usertoken.UserTokenFilter;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.Collection;
import java.util.function.Function;

/**
 * Konfigurerer UserTokenFilter.
 * Leser UserToken og knytter opp UibBruker med brukerinformasjon.
 * <p>
 * Med default settings kan innlogget bruker hentes med:
 * <p>
 * BasicUibBruker innloggetBruker
 * <p>
 * Kan også sette opp filtrering på javax-roller.
 * Sjekk implementasjon av dette i aarsregnskapsplanlegging
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserTokenFilterAddon implements Addon {
    @Wither
    public final boolean requireUserToken;

    @Wither
    public final Function<UserToken, Collection<String>> userTokenTilganger;

    @Wither
    public final Function<UibBruker, Collection<String>> uibBrukerTilganger;

    @Wither
    public final ImmutableList<Function<UibRolle, String>> uibRolleTilganger;

    public static UserTokenFilterAddon defaults = new UserTokenFilterAddon(true, it -> Lists.newArrayList(), it -> Lists.newArrayList(), ImmutableList.of());

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    binder.bind(this).to(UserTokenFilterAddon.class);
                    binder.bindFactory(UibBrukerInjectionFactory.class).to(UibBruker.class);
                }
        );
        jerseyConfig.addRegistations(registrator -> registrator
                .register(RolesAllowedDynamicFeature.class)
                .register(UserTokenFilter.class)
                .register(UserTokenFasttrackFilter.class)
        );
    }

    public UserTokenFilterAddon plusUibRolleTilgang(Function<UibRolle, String> tilgang) {
        return withUibRolleTilganger(GuavaHelper.plus(uibRolleTilganger, tilgang));
    }


}

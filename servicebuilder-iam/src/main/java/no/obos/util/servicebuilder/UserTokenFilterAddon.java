package no.obos.util.servicebuilder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.util.servicebuilder.usertoken.BasicUibBruker;
import no.obos.util.servicebuilder.usertoken.BasicUibBrukerInjectionFactory;
import no.obos.util.servicebuilder.usertoken.UibBrukerProvider;
import no.obos.util.servicebuilder.usertoken.UserTokenFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.function.Predicate;

/**
 * Konfigurerer UserTokenFilter.
 * Leser UserToken og knytter opp UibBruker med brukerinformasjon.
 * <p>
 * Med default settings kan innlogget bruker hentes med:
 *
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
    public final UibBrukerProvider uibBrukerProvider;
    @Wither
    public final Predicate<ContainerRequestContext> fasttrackFilter;

    public static UserTokenFilterAddon defaults = new UserTokenFilterAddon(true, BasicUibBruker.provider(), it -> false);

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addBinder(binder -> {
                    binder.bind(this).to(UserTokenFilterAddon.class);
                    if (uibBrukerProvider instanceof BasicUibBruker.BasicUibBrukerProvider) {
                        binder.bindFactory(BasicUibBrukerInjectionFactory.class).to(BasicUibBruker.class);
                    }
                }
        );
        jerseyConfig.addRegistations(registrator -> registrator
                .register(RolesAllowedDynamicFeature.class)
                .register(UserTokenFilter.class)
        );
    }
}

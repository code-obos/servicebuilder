package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder(toBuilder = true)
public class UserTokenFilterAddon implements Addon {
    public final boolean requireUserToken;
    public final UibBrukerProvider uibBrukerProvider;
    public final Predicate<ContainerRequestContext> fasttrackFilter;

    public static class UserTokenFilterAddonBuilder {
        boolean requireUserToken = true;
        UibBrukerProvider uibBrukerProvider = BasicUibBruker.provider();
        Predicate<ContainerRequestContext> fasttrackFilter = it -> false;
    }

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

package no.obos.util.servicebuilder.addon;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;
import no.obos.iam.tokenservice.UserToken;
import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.exception.DependenceException;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.usertoken.SwaggerImplicitUserTokenHeader;
import no.obos.util.servicebuilder.usertoken.UibBruker;
import no.obos.util.servicebuilder.usertoken.UibBrukerInjectionFactory;
import no.obos.util.servicebuilder.usertoken.UibRolle;
import no.obos.util.servicebuilder.usertoken.UserTokenBlockingFilter;
import no.obos.util.servicebuilder.usertoken.UserTokenFilter;
import no.obos.util.servicebuilder.util.GuavaHelper;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
    public final boolean requireUserTokenByDefault;

    @Wither
    public final boolean swaggerImplicitHeaders;

    @Wither
    public final Function<UserToken, Collection<String>> userTokenTilganger;

    @Wither
    public final Function<UibBruker, Collection<String>> uibBrukerTilganger;

    @Wither
    public final ImmutableMap<String, Predicate<UibRolle>> rolleGirTilgang;

    public static UserTokenFilterAddon defaults = new UserTokenFilterAddon(true, true, it -> Lists.newArrayList(), it -> Lists.newArrayList(), ImmutableMap.of());

    @Override
    public Addon finalize(ServiceConfig serviceConfig) {
        if (swaggerImplicitHeaders && ! serviceConfig.isAddonPresent(SwaggerAddon.class)) {
            throw new DependenceException(this.getClass(), SwaggerAddon.class, "swaggerImplicitHeaders specified, SwaggerAddon missing");
        }
        if (! serviceConfig.isAddonPresent(TokenServiceAddon.class)) {
            throw new DependenceException(this.getClass(), TokenServiceAddon.class);
        }
        return this;
    }

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
                .register(UserTokenBlockingFilter.class)
        );

        if (swaggerImplicitHeaders) {
            SwaggerExtensions.getExtensions().add(new SwaggerImplicitUserTokenHeader(requireUserTokenByDefault));
        }
    }

    public UserTokenFilterAddon plusRolleGirTilgang(String rolle, Predicate<UibRolle> girRolleTilgang) {
        return withRolleGirTilgang(GuavaHelper.plus(rolleGirTilgang, rolle, girRolleTilgang));
    }


    @Override
    public Set<Class<?>> finalizeAfter() {return ImmutableSet.of(SwaggerAddon.class, TokenServiceAddon.class);}
}

package no.obos.util.servicebuilder.usertoken;

import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import no.obos.util.servicebuilder.model.UibBruker;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;


public class UibBrukerInjectionFactory implements Factory<UibBruker> {

    final Provider<SecurityContext> context;

    final UserTokenFilterAddon config;

    @Inject
    public UibBrukerInjectionFactory(Provider<SecurityContext> context, UserTokenFilterAddon config) {
        this.context = context;
        this.config = config;
    }


    @Override
    public UibBruker provide() {
        Principal userPrincipal = context.get().getUserPrincipal();

        if (userPrincipal == null && config.requireUserTokenByDefault) {
            return null;
        }
        if (userPrincipal != null && ! (userPrincipal instanceof UibBrukerPrincipal)) {
            throw new IllegalArgumentException("Userprincipal not of type UibBruker, was of type ");
        }

        return userPrincipal == null ? null : ((UibBrukerPrincipal) userPrincipal).uibBruker;

    }

    @Override
    public void dispose(UibBruker session) {

        // intentionally empty

    }
}

package no.obos.util.servicebuilder.usertoken;

import no.obos.util.servicebuilder.addon.UserTokenFilterAddon;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;


public class UibBrukerInjectionFactory implements Factory<UibBruker> {

    final SecurityContext context;

    final UserTokenFilterAddon config;

    @Inject
    public UibBrukerInjectionFactory(SecurityContext context, UserTokenFilterAddon config) {
        this.context = context;
        this.config = config;
    }


    @Override
    public UibBruker provide() {
        Principal userPrincipal = context.getUserPrincipal();

        if (userPrincipal == null && config.requireUserTokenByDefault) {
            return null;
        }
        if (userPrincipal != null && ! (userPrincipal instanceof UibBruker)) {
            throw new IllegalArgumentException("Userprincipal not of type UibBruker, was of type ");
        }

        return (UibBruker) userPrincipal;

    }

    @Override
    public void dispose(UibBruker session) {

        // intentionally empty

    }
}

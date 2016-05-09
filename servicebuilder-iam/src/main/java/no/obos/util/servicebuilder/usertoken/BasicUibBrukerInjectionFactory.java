package no.obos.util.servicebuilder.usertoken;

import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;


public class BasicUibBrukerInjectionFactory implements Factory<BasicUibBruker> {

    static final Logger log = LoggerFactory.getLogger(BasicUibBrukerInjectionFactory.class);

    final SecurityContext context;

    @Inject
    public BasicUibBrukerInjectionFactory(SecurityContext context) {
        this.context = context;
    }


    @Override
    public BasicUibBruker provide() {
        Principal userPrincipal = context.getUserPrincipal();

        if(userPrincipal == null) {
            log.warn("Tried to get userprincipal when user not logged in");
        }
        if (! (userPrincipal instanceof BasicUibBruker)) {
            throw new IllegalArgumentException("Userprincipal not of type BasicUibBruker, was of type ");
        }

        return (BasicUibBruker) userPrincipal;

    }

    @Override
    public void dispose(BasicUibBruker session) {

        // intentionally empty

    }
}

package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.JettyServer;
import no.obos.util.servicebuilder.cors.CorsFilter;
import no.obos.util.servicebuilder.model.Addon;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * Implementerer en mest mulig liberal CORS-protokoll basert på https://mortoray.com/2014/04/09/allowing-unlimited-access-with-cors/ .
 * Verdier gitt i konfiurasjon (origin, methods, headers) er fallbackverdier.
 */
public class CorsFilterAddon implements Addon {
    public static CorsFilterAddon defaults = new CorsFilterAddon();

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        jettyServer.getServletContext().addFilter(CorsFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }
}

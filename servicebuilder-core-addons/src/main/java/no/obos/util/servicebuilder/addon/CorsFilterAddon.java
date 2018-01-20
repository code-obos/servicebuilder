package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.JerseyConfig;
import no.obos.util.servicebuilder.cors.ResponseCorsFilter;
import no.obos.util.servicebuilder.model.Addon;

/**
 * Implementerer en mest mulig liberal CORS-protokoll basert på https://mortoray.com/2014/04/09/allowing-unlimited-access-with-cors/ .
 * Verdier gitt i konfiurasjon (origin, methods, headers) er fallbackverdier.
 */
public class CorsFilterAddon implements Addon {
    public static CorsFilterAddon corsFilterAddon = new CorsFilterAddon();

    @Override
    public void addToJerseyConfig(JerseyConfig jerseyConfig) {
        jerseyConfig.addRegistations(registrator -> registrator.register(ResponseCorsFilter.class));
    }
}

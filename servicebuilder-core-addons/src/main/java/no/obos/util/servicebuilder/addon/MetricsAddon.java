package no.obos.util.servicebuilder.addon;

import no.obos.metrics.ObosMetricsServlet;
import no.obos.util.servicebuilder.model.Addon;
import no.obos.util.servicebuilder.JettyServer;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Legger til servlet for metrics. Metrics-sjekker for forskjellig funksjonalitet (database, eksterne tjenester osv)
 * registreres i deres respektive addons.
 * Standard path er tjeneste/versjon/metrics/
 */
public class MetricsAddon implements Addon {

    private static final String PATH_SPEC = "/metrics/*";

    public static MetricsAddon defaults = new MetricsAddon();

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder metricsServletHolder = new ServletHolder(new ObosMetricsServlet(jettyServer.getClass()));
        jettyServer.getServletContext().addServlet(metricsServletHolder, PATH_SPEC);
    }
}

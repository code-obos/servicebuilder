package no.obos.util.servicebuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import no.obos.metrics.ObosMetricsServlet;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Legger til servlet for metrics. Metrics-sjekker for forskjellig funksjonalitet (database, eksterne tjenester osv)
 * registreres i deres respektive addons.
 * Standard path er tjeneste/versjon/metrics/
 */
@Builder(toBuilder = true)
public class MetricsAddon implements Addon {

    private static final String PATH_SPEC = "/metrics/*";

    @Override
    public void addToJettyServer(JettyServer jettyServer) {
        ServletHolder metricsServletHolder = new ServletHolder(new ObosMetricsServlet(jettyServer.getClass()));
        jettyServer.getServletContext().addServlet(metricsServletHolder, PATH_SPEC);
    }
}

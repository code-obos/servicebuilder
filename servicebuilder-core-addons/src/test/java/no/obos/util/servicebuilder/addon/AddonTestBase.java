package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestServiceRunnerJetty;

abstract class AddonTestBase {

    TestServiceRunnerJetty testServiceRunnerJettyWithDefaults(ServiceConfig serviceConfig) {
        return TestServiceRunnerJetty
                .defaults(serviceConfig)
                .property("server.port", "0")
                .property("service.version", "1.0")
                .property("server.contextPath", "/test/v1.0");
    }
}

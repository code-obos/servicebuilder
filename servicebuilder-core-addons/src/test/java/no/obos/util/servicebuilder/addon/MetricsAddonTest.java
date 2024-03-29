package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsAddonTest extends AddonTestBase {

    @Test
    public void ping() {

        ServiceConfig serviceConfig = TestService.config
                .addon(MetricsAddon.defaults);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .oneShot(target -> target
                        .path("metrics")
                        .path("ping")
                        .request()
                        .get()
                );
        assertThat(call.readEntity(String.class)).isEqualToIgnoringNewLines("pong\n");
    }

    @Test
    public void healthcheck() {

        ServiceConfig serviceConfig = TestService.config
                .addon(MetricsAddon.defaults);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .oneShot(target -> target
                        .path("metrics")
                        .path("healthcheck")
                        .request()
                        .get()
                );
        assertThat(call.readEntity(String.class)).isEqualTo("{\"Thread deadlocks\":{\"healthy\":true}}");
    }

    @Test
    public void healthcheck_with_addons() {

        ServiceConfig serviceConfig = TestService.config
                .addon(MetricsAddon.defaults)
                .addon(JerseyClientAddon.defaults(TestService.instance).apptoken(false));
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .property("test.service.url", "http://no.way.this.is.a.valid.address.zzz:23456")
                .oneShot(target -> target
                        .path("metrics")
                        .path("healthcheck")
                        .request()
                        .get()
                );
        assertThat(call.readEntity(String.class))
                .matches("\\{\"Thread deadlocks\":"
                        + "\\{\"healthy\":true\\},"
                        + "\"test: http://no\\.way\\.this\\.is\\.a\\.valid\\.address\\.zzz:23456/s\\d+\\.\\d+\":"
                        + "\\{\"healthy\":false,\"message\":\"no\\.way\\.this\\.is\\.a\\.valid\\.address\\.zzz\"\\}\\}");
    }

}



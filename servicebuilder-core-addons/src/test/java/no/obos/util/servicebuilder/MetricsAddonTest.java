package no.obos.util.servicebuilder;

import io.swagger.annotations.Api;
import no.obos.util.servicebuilder.TestService.Resource;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

import static no.obos.util.servicebuilder.TestService.Payload;
import static no.obos.util.servicebuilder.TestService.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MetricsAddonTest {

    @Test
    public void ping() {

        ServiceConfig serviceConfig = TestService.config
                .addon(MetricsAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .oneShot(target -> target
                        .path("metrics")
                        .path("ping")
                        .request()
                        .get()
                );
        assertThat(call.readEntity(String.class)).isEqualTo("pong\n");
    }

    @Test
    public void healthcheck() {

        ServiceConfig serviceConfig = TestService.config
                .addon(MetricsAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
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
                .addon(JerseyClientAddon.defaults(TestService.instance))
                ;
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .property("test.service.url", "http://no.way.this.is.a.valid.address.zzz:23456")
                .oneShot(target -> target
                        .path("metrics")
                        .path("healthcheck")
                        .request()
                        .get()
                );
        assertThat(call.readEntity(String.class)).isEqualTo("{\"Thread deadlocks\":{\"healthy\":true},\"test\":{\"healthy\":false,\"message\":\"no.way.this.is.a.valid.address.zzz\"}}");
    }
}


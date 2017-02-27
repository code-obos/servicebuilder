package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunnerJetty;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_CREDENTIALS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_HEADERS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.REQUEST_HEADERS;
import static org.assertj.core.api.Assertions.assertThat;

public class CorsFilterAddonTest {

    @Test
    public void tolerant_options() {

        ServiceConfig serviceConfig = TestService.config
                .addon(CorsFilterAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .oneShot(target -> target
                        .path("api")
                        .path(TestService.PATH)
                        .request()
                        .header(REQUEST_HEADERS, "X-OBOS-APPTOKENID")
                        .options()
                );
        MultivaluedMap<String, Object> headers = call.getHeaders();
        assertThat(headers.getFirst(ALLOW_HEADERS)).isEqualTo("X-OBOS-APPTOKENID");
        assertThat(headers.getFirst(ALLOW_CREDENTIALS)).isEqualTo("true");
    }

}

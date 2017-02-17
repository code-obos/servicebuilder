package no.obos.util.servicebuilder;

import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

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
                        .header("Origin", "Vestfossen")
                        .header("Access-Control-Request-Methods", "GET,PUT,POST")
                        .header("Access-Control-Request-Headers", "X-OBOS-APPTOKENID")
                        .options()
                );
        MultivaluedMap<String, Object> headers = call.getHeaders();
        assertThat(headers.getFirst("Access-Control-Allow-Origin")).isEqualTo("Vestfossen");
        assertThat(headers.getFirst("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(headers.getFirst("Access-Control-Allow-Methods")).isEqualTo("GET,PUT,POST");
        assertThat(headers.getFirst("Access-Control-Allow-Headers")).isEqualTo("X-OBOS-APPTOKENID");

    }

}

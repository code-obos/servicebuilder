package no.obos.util.servicebuilder;

import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_CREDENTIALS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_HEADERS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_METHODS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ALLOW_ORIGIN;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.ORIGIN;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.REQUEST_HEADERS;
import static no.obos.util.servicebuilder.cors.ResponseCorsFilter.REQUEST_METHOD;
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
                        .header(ORIGIN, "Vestfossen")
                        .header(REQUEST_METHOD, "GET,PUT,POST")
                        .header(REQUEST_HEADERS, "X-OBOS-APPTOKENID")
                        .options()
                );
        MultivaluedMap<String, Object> headers = call.getHeaders();
        assertThat(headers.getFirst(ALLOW_ORIGIN)).isEqualTo("Vestfossen");
        assertThat(headers.getFirst(ALLOW_METHODS)).isEqualTo("GET,PUT,POST");
        assertThat(headers.getFirst(ALLOW_HEADERS)).isEqualTo("X-OBOS-APPTOKENID");
        assertThat(headers.getFirst(ALLOW_CREDENTIALS)).isEqualTo("true");
    }

}

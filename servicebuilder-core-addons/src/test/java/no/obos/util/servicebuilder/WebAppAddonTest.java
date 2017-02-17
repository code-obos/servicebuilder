package no.obos.util.servicebuilder;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class WebAppAddonTest {

    @Test
    public void serves_from_classpath() {

        ServiceConfig serviceConfig = TestService.config
                .addon(WebAppAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .property("webapp.resource.url", "classpath:webapp")
                .oneShot(target -> target
                        .path("webapp")
                        .path("page.html")
                        .request()
                        .get()
                );
        String expected = "Yes!\n";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    public void serves_from_filesystem() {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        ServiceConfig serviceConfig = TestService.config
                .addon(WebAppAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .property("webapp.resource.url", "file:src/test/resources/webapp")
                .oneShot(target -> target
                        .path("webapp")
                        .path("page.html")
                        .request()
                        .get()
                );
        String expected = "Yes!\n";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

}


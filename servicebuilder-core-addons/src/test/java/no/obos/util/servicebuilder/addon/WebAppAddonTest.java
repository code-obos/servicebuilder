package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunnerJetty;
import no.obos.util.servicebuilder.addon.WebAppAddon;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class WebAppAddonTest extends AddonTestBase {

    @Test
    public void serves_from_classpath() {

        ServiceConfig serviceConfig = TestService.config
                .addon(WebAppAddon.defaults);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
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

        ServiceConfig serviceConfig = TestService.config
                .addon(WebAppAddon.defaults);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
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


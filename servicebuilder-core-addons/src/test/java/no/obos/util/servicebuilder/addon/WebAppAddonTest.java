package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import org.assertj.core.util.Files;
import org.junit.Test;

import javax.ws.rs.core.Response;

import java.io.File;

import static no.obos.util.servicebuilder.addon.WebAppAddon.webAppAddon;
import static org.assertj.core.api.Assertions.assertThat;

public class WebAppAddonTest extends AddonTestBase {

    @Test
    public void serves_from_classpath() {

        ServiceConfig serviceConfig = TestService.config
                .addon(webAppAddon);
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

        String webAppDirLocation = "file:src/test/resources/webapp";
        File file = Files.currentFolder();

        if(file.getName().equals("servicebuilder")) {
            webAppDirLocation = "file:servicebuilder-core-addons/src/test/resources/webapp";
        }

        ServiceConfig serviceConfig = TestService.config
                .addon(webAppAddon);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .property("webapp.resource.url", webAppDirLocation)
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


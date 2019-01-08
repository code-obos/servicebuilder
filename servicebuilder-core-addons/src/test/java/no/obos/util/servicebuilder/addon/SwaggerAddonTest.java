package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerAddonTest extends AddonTestBase {

    @Test
    public void serves_swagger() {

        ServiceConfig serviceConfig = TestService.config
                .addon(SwaggerAddon.defaults);
        Response call = testServiceRunnerJettyWithDefaults(serviceConfig)
                .oneShot(target -> target
                        .path("api")
                        .path("swagger.json")
                        .request()
                        .get()
                );

        assertThat(call.readEntity(String.class))
                .contains("\"info\":{\"version\":\"1.0\",\"title\":\"\"}")
                .contains("\"basePath\":\"/test/v1.0/api/\"")
                .contains("\"definitions\":{\"Payload\":{\"type\":\"object\",\"properties\":{\"string\":{\"type\":\"string\"},\"date\":{\"type\":\"string\",\"format\":\"date\"}}}}")
                .contains("\"paths\":{\"/path\":{\"get\":{\"operationId\":\"get\",\"produces\":[\"application/json\"],\"parameters\":[],\"responses\":{\"200\":{\"description\":\"successful operation\",\"headers\":{},\"schema\":{\"$ref\":\"#/definitions/Payload\"}}}}}}");
    }

}


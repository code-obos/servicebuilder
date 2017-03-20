package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestServiceRunnerJetty;
import no.obos.util.servicebuilder.addon.SwaggerAddon;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SwaggerAddonTest {

    @Test
    public void serves_swagger() {

        ServiceConfig serviceConfig = TestService.config
                .addon(SwaggerAddon.defaults);
        Response call = TestServiceRunnerJetty
                .defaults(serviceConfig)
                .property("api.baseurl", "ape")
                .oneShot(target -> target
                        .path("api")
                        .path("swagger.json")
                        .request()
                        .get()
                );
        //language=JSON
        String expected = "{\"swagger\":\"2.0\",\"info\":{\"version\":\"1.0\",\"title\":\"\"},\"basePath\":\"ape\",\"paths\":{\"/path\":{\"get\":{\"operationId\":\"get\",\"produces\":[\"application/json\"],\"parameters\":[],\"responses\":{\"200\":{\"description\":\"successful operation\",\"schema\":{\"$ref\":\"#/definitions/Payload\"},\"headers\":{}}}}}},\"definitions\":{\"Payload\":{\"type\":\"object\",\"properties\":{\"string\":{\"type\":\"string\"},\"date\":{\"type\":\"string\",\"format\":\"date\"}}}}}";
        assertThat(call.readEntity(String.class)).isEqualToIgnoringWhitespace(expected);
    }

}


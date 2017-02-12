package no.obos.util.servicebuilder;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;

public class TestServiceRunnerTest {
    ServiceConfig serviceConfig = TestService.addToConfig(ServiceConfig.builder()).build();

    @Test
    public void can_call_basic() {
        String payload = TestServiceRunner.oneShot(serviceConfig, ((clientConfig, uri) ->

                        ClientBuilder.newClient(clientConfig).target(uri)
                                .path(TestService.PATH)
                                .request()
                                .get()
                                .readEntity(String.class)

                )
        );
        Assert.assertEquals("{\n"
                + "  \"string\" : \"string\",\n"
                + "  \"date\" : \"2017-02-12\"\n"
                + "}", payload);
    }
}

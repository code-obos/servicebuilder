package no.obos.util.servicebuilder;

import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class TestServiceRunnerTest {
    TestServiceRunner testServiceRunner = TestServiceRunner.defaults(TestService.config);

    @Test
    public void can_call_basic() {
        String payload = testServiceRunner.oneShot(((clientConfig, uri) ->

                        ClientBuilder.newClient(clientConfig).target(uri)
                                .path(TestService.PATH)
                                .request()
                                .get()
                                .readEntity(String.class)

                )
        );
        assertThat(payload).isEqualTo("{\n"
                + "  \"string\" : \"string\",\n"
                + "  \"date\" : \"" + LocalDate.now().toString() + "\"\n"
                + "}");
    }


    @Test
    public void can_call_target() {
        String payload = testServiceRunner.oneShot((target ->
                        target
                                .path(TestService.PATH)
                                .request()
                                .get()
                                .readEntity(String.class)

                )
        );
        assertThat(payload).isEqualTo("{\n"
                + "  \"string\" : \"string\",\n"
                + "  \"date\" : \"" + LocalDate.now().toString() + "\"\n"
                + "}");
    }


    @Test
    public void can_call_stub() {
        TestService.Payload payload = testServiceRunner.oneShot(TestService.Resource.class, (TestService.Resource::get
                )
        );
        assertThat(payload).isEqualTo(TestService.defaultPayload);
    }
}

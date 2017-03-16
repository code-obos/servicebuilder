package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.TestServiceFull.Call;
import no.obos.util.servicebuilder.TestServiceFull.Controller;
import no.obos.util.servicebuilder.TestServiceFull.ResourceFull;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

public class TestServiceRunnerStubTest {
    Controller controller = Mockito.mock(Controller.class);
    TestServiceRunner testServiceRunner = TestServiceRunner.defaults(
            TestServiceFull.config
                    .bind(controller, Controller.class)
    );


    @Test
    public void can_call_stub_with_explicit_headers() {
        //given
        Call expected = getCall().toBuilder()
                .header("Banan", "kjakablakken")
                .build();

        //when
        testServiceRunner
                .stubConfigurator(cfg -> cfg.header("Banan", "kjakablakken"))
                .oneShot(ResourceFull.class, (ResourceFull::get));

        //then
        verify(controller).isCallValid(eq(expected));
    }

    @Test
    public void can_call_stub_embedded_headers_and_queryparams() {
        //given
        Call expected = getCall().toBuilder()
                .header("Banan", "kjakablakken")
                .header("header1", "header1")
                .header("header2", "2")
                .queryParam("qp1", "qp1")
                .queryParam("qp2", "22")
                .build();

        //when
        testServiceRunner
                .stubConfigurator(cfg -> cfg.header("Banan", "kjakablakken"))
                .oneShot(ResourceFull.class, it -> it.getExplicitContext("header1", 2, "qp1", 22));

        //then
        verify(controller).isCallValid(eq(expected));
    }

    @Test
    public void can_call_stub_several_times_different_headers() {
        //given
        Call expected1 = getCall().toBuilder()
                .header("kul", "umulig")
                .build();

        Call expected2 = getCall().toBuilder()
                .header("workbook", "in your face")
                .build();

        //when
        TestServiceRunner.Runtime runtime = testServiceRunner.start().runtime;

        //then
        runtime
                .stubConfigurator(cfg -> cfg.header("kul", "umulig"))
                .call(ResourceFull.class, (ResourceFull::get));
        verify(controller).isCallValid(eq(expected1));

        runtime
                .stubConfigurator(cfg -> cfg.header("workbook", "in your face"))
                .call(ResourceFull.class, (ResourceFull::get));
        verify(controller).isCallValid(eq(expected2));


        runtime.stop();
    }

    Call getCall() {
        return Call.builder()
                .header("Accept", "application/json")
                .header("User-Agent", "Jersey/2.25.1 (Jersey InMemory Connector)")
                .build();
    }
}

package no.obos.util.servicebuilder;

import no.obos.util.servicebuilder.TestService.Resource;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.exception.ExternalResourceException.MetaData;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyClientErrorHandlingTest {


    Resource resource = mock(Resource.class);
    TestServiceRunner testServiceRunner = TestServiceRunner.defaults(
            ServiceConfig.defaults(TestService.instance)
                    .addon(ExceptionMapperAddon.defaults
                            .plusStacktraceConfig(RuntimeException.class, false)
                    )
                    .bind(resource, Resource.class)
    );


    @Test
    public void error_handling_in_stub() {
        //given
        when(resource.get()).thenThrow(new RuntimeException("banan"));
        //when
        try {
            testServiceRunner
                    .oneShot(Resource.class, Resource::get);
            Assert.fail();
        } catch (ExternalResourceException actual) {
            assertThat(actual.getMetaData().incidentReferenceId).isNotEmpty();
            assertThat(actual.getMetaData().incidentReferenceId).isEqualTo(actual.getMetaData().nestedProblemResponce.incidentReferenceId);
            assertThat(actual.getMetaData()).isEqualToComparingFieldByFieldRecursively(
                    MetaData.builder()
                            .gotAnswer(true)
                            .incidentReferenceId(actual.getMetaData().incidentReferenceId)
                            .targetName("test")
                            .targetUrl("http://localhost:0/path")
                            .httpStatus(500)
                            .nestedProblemResponce(ProblemResponse.builder()
                                    .title("Internal Server Error")
                                    .detail("Det har oppstått en intern feil")
                                    .incidentReferenceId(actual.getMetaData().incidentReferenceId)
                                    .status(500)
                                    .suggestedUserMessageInDetail(false)
                                    .build()
                            )
                            .context("response_headers", "{Content-Type=[application/problem+json], Content-Length=[250]}")
                            .build());
        }
    }

    @Test(expected = ProcessingException.class)
    public void no_custom_error_handling_when_call_fails_before_network() {
        Client client = ClientGenerator.defaults(TestService.instance).generate();
        //given
        Resource resource = StubGenerator.defaults(client, URI.create("http://will.fail.badly")).generateClient(Resource.class);
        //when
        resource.get();
        Assert.fail();
    }
}

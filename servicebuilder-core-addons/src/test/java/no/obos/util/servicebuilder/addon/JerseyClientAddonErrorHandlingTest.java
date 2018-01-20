package no.obos.util.servicebuilder.addon;

import no.obos.util.servicebuilder.ServiceConfig;
import no.obos.util.servicebuilder.TestService;
import no.obos.util.servicebuilder.TestService.Resource;
import no.obos.util.servicebuilder.TestServiceRunner;
import no.obos.util.servicebuilder.client.ClientGenerator;
import no.obos.util.servicebuilder.client.StubGenerator;
import no.obos.util.servicebuilder.exception.ExternalResourceException;
import no.obos.util.servicebuilder.exception.ExternalResourceException.MetaData;
import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.Version;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import java.net.URI;

import static no.obos.util.servicebuilder.addon.ExceptionMapperAddon.exceptionMapperAddon;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyClientAddonErrorHandlingTest {


    Resource resource = mock(Resource.class);
    TestServiceRunner testServiceRunner = TestServiceRunner.defaults(
            ServiceConfig.defaults(TestService.instance)
                    .addon(exceptionMapperAddon
                            .stacktraceConfig(RuntimeException.class, false)
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
            String incidentReferenceId = actual.getMetaData().httpResponseMetaData.httpProblem.incidentReferenceId;
            assertThat(incidentReferenceId).isNotEmpty();
            assertThat(actual.getMetaData()).isEqualToComparingFieldByFieldRecursively(
                    MetaData.builder()
                            .gotAnswer(true)
                            .httpRequestMetaData(ExternalResourceException.HttpRequestMetaData.builder()
                                    .url("http://localhost:0/path")
                                    .header("Accept", "application/json")
                                    .header("User-Agent", "Jersey/2.25.1 (Jersey InMemory Connector)")
                                    .build()
                            )
                            .httpResponseMetaData(ExternalResourceException.HttpResponseMetaData.builder()
                                    .httpProblem(HttpProblem.builder()
                                            .title("Internal Server Error")
                                            .detail("Det har oppstått en intern feil")
                                            .incidentReferenceId(incidentReferenceId)
                                            .status(500)
                                            .suggestedUserMessageInDetail(false)
                                            .build()
                                    )
                                    .incidentReferenceId(incidentReferenceId)
                                    .status(500)
                                    .header("Content-Length", "250")
                                    .header("Content-Type", "application/problem+json")
                                    .build()
                            )
                            .targetName("test")
                            .targetVersion(new Version(1, 0, 0))
            );
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

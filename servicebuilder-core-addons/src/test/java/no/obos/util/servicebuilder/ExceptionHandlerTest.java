package no.obos.util.servicebuilder;

import com.google.common.collect.ImmutableMap;
import no.obos.util.servicebuilder.exception.HttpProblemException;
import no.obos.util.servicebuilder.exception.UserMessageException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionHandlerTest {
    TestService.Resource testService = mock(TestService.Resource.class);
    ImmutableMap<Class<?>, Boolean> disableStackTraceMap = ImmutableMap.<Class<?>, Boolean>builder().put(RuntimeException.class, false).build();
    ServiceConfig serviceConfig = ServiceConfig.builder()
            .serviceDefinition(TestService.instance)
            .bind(testService, TestService.Resource.class)
            .addon(ExceptionMapperAddon.builder().stacktraceConfig(disableStackTraceMap).build())
            .build();
    TestServiceRunner testServiceRunner = TestServiceRunner.builder()
            .serviceConfig(serviceConfig)
            .clientConfigurator(cfg -> cfg.exceptionMapping(false))
            .build();

    @Test
    public void userMessageException() {
        //Given
        when(testService.get()).thenThrow(new UserMessageException("Boooom!", 421));

        //when
        Response response = testServiceRunner.oneShot((clientconfig, uri) ->
                ClientBuilder.newClient(clientconfig).target(uri)
                        .path(TestService.PATH)
                        .request()
                        .get());

        //then
        ProblemResponse actual = response.readEntity(ProblemResponse.class);
        assertThat(actual.detail).isEqualTo("Boooom!");
        assertThat(actual.status).isEqualTo(421);
        assertThat(actual.suggestedUserMessageInDetail).isEqualTo(true);
        assertThat(response.getStatus()).isEqualTo(421);
    }

    @Test
    public void httpProblemException() {
        ProblemResponse expected = ProblemResponse.builder()
                .context("eple", "banan")
                .detail("farris")
                .status(599)
                .suggestedUserMessageInDetail(true)
                .title("fisk")
                .type("https://google.com")
                .build();

        //Given
        when(testService.get()).thenThrow(new HttpProblemException(expected, LogLevel.INFO, false));

        //when
        Response response = testServiceRunner.oneShot((clientconfig, uri) ->
                ClientBuilder.newClient(clientconfig).target(uri)
                        .path(TestService.PATH)
                        .request()
                        .get());

        //then
        ProblemResponse actual = response.readEntity(ProblemResponse.class);

        assertThat(actual.incidentReferenceId).isNotEmpty();
        assertThat(actual.toBuilder().incidentReferenceId(null).build()).isEqualToComparingFieldByFieldRecursively(expected);
    }
}


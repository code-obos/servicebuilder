package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import no.obos.util.model.ProblemResponse;
import no.obos.util.servicebuilder.ExceptionDescription;
import no.obos.util.servicebuilder.ExceptionMapperAddon;
import no.obos.util.servicebuilder.LogLevel;
import org.junit.Test;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ExceptionUtilTest {

    @Test
    public void exceptions_without_configuration_should_be_logged_with_stacktrace() {
        //given
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertThat(actual).isTrue();
    }

    @Test
    public void exception_stacktrace_may_be_overridden() {
        //given
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(IllegalArgumentException.class, false)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertThat(actual).isFalse();
    }

    @Test
    public void overridden_settings_are_inherited() {
        //given
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(input.getClass().getSuperclass(), false)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertThat(actual).isFalse();
    }


    @Test
    public void overridden_may_be_overridden_downtree() {
        //given
        Throwable input = new NotFoundException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(input.getClass().getSuperclass().getSuperclass(), false)
                .put(input.getClass().getSuperclass(), true)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertThat(actual).isTrue();
    }

    @Test
    public void exception_description_default_values_works_without_context() {
        //Given
        String message = "banan";
        IllegalStateException inTest = new IllegalStateException(message);
        ExceptionUtil exceptionUtil = new ExceptionUtil();
        exceptionUtil.config = null;
        exceptionUtil.headers = null;
        exceptionUtil.request = null;
        ExceptionDescription exceptionDescription = ExceptionDescription.builder()
                .exception(inTest)
                .build();

        //When
        ExceptionDescription actual = exceptionUtil.withDefaults(exceptionDescription);


        //Then
        ExceptionDescription expected = ExceptionDescription.builder()
                .detail(message)
                .exception(inTest)
                .status(500)
                .internalMessage(null)
                .logLevel(LogLevel.ERROR)
                .logStackTrace(true)
                .reference(actual.reference)
                .title("Internal Server Error")
                .build();

        assertThat(actual)
                .isEqualToIgnoringGivenFields(expected, "logger", "reference")
                .hasFieldOrProperty("logger")
                .hasFieldOrProperty("reference");
    }

    @Test
    public void exception_util_provides_response_and_logging_without_context() {
        //Given
        ExceptionUtil exceptionUtil = new ExceptionUtil();
        exceptionUtil.config = null;
        exceptionUtil.headers = null;
        exceptionUtil.request = null;
        String detail = "Totally expected exception";
        Logger logger = mock(Logger.class);

        //when
        IllegalStateException throwable = new IllegalStateException(detail);
        Response actualResponse = exceptionUtil.handle(throwable, cfg -> cfg.logger(logger));
        ProblemResponse actualEntity = (ProblemResponse) actualResponse.getEntity();


        assertThat(actualResponse.getStatus()).isEqualTo(500);
        assertThat(actualEntity.detail).isEqualTo(detail);
        assertThat(actualEntity.status).isEqualTo(500);
        verify(logger).error(any(), eq(throwable));
        verifyNoMoreInteractions(logger);
    }



    @Test
    public void exception_description_default_values_works_with_context() {
        //Given
        String message = "banan";
        IllegalStateException inTest = new IllegalStateException(message);
        ExceptionUtil exceptionUtil = new ExceptionUtil();
        ExceptionMapperAddon.Configuration config = mock(ExceptionMapperAddon.Configuration.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        exceptionUtil.config = ExceptionMapperAddon.defaultConfigurationuration().build();
        exceptionUtil.headers = headers;
        exceptionUtil.request = request;
        ExceptionDescription exceptionDescription = ExceptionDescription.builder()
                .exception(inTest)
                .build();


        //When
        ExceptionDescription actual = exceptionUtil.withDefaults(exceptionDescription);


        //Then
        ExceptionDescription expected = ExceptionDescription.builder()
                .detail(message)
                .exception(inTest)
                .status(500)
                .internalMessage(null)
                .logLevel(LogLevel.ERROR)
                .logStackTrace(true)
                .reference(actual.reference)
                .title("Internal Server Error")
                .build();

        assertThat(actual)
                .isEqualToIgnoringGivenFields(expected, "logger", "reference")
                .hasFieldOrProperty("logger")
                .hasFieldOrProperty("reference");
    }

    @Test
    public void exception_util_provides_response_and_logging_with() {
        //Given
        ExceptionUtil exceptionUtil = new ExceptionUtil();
        HttpHeaders headers = mock(HttpHeaders.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        exceptionUtil.config = ExceptionMapperAddon.defaultConfigurationuration().build();
        exceptionUtil.headers = headers;
        exceptionUtil.request = request;

        when(headers.getRequestHeaders()).thenReturn(new MultivaluedHashMap<>());

        String detail = "Totally expected exception";
        Logger logger = mock(Logger.class);

        //when
        IllegalStateException throwable = new IllegalStateException(detail);
        Response actualResponse = exceptionUtil.handle(throwable, cfg -> cfg.logger(logger));
        ProblemResponse actualEntity = (ProblemResponse) actualResponse.getEntity();


        assertThat(actualResponse.getStatus()).isEqualTo(500);
        assertThat(actualEntity.detail).isEqualTo(detail);
        assertThat(actualEntity.status).isEqualTo(500);
        verify(logger).error(any(), eq(throwable));
        verifyNoMoreInteractions(logger);
    }
}

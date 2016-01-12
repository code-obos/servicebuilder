package no.obos.util.servicebuilder.exception;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.Assert.assertEquals;

public class ExceptionUtilTest {

    @Test
    public void exceptions_without_configuration_should_be_logged_with_stacktrace() {
        //given
        boolean expected = true;
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void exception_stacktrace_may_be_overridden() {
        //given
        boolean expected = false;
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(IllegalArgumentException.class, false)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertEquals(expected, actual);
    }

    @Test
    public void overridden_settings_are_inherited() {
        //given
        boolean expected = false;
        Throwable input = new IllegalArgumentException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(input.getClass().getSuperclass(), false)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertEquals(expected, actual);
    }


    @Test
    public void overridden_may_be_overridden_downtree() {
        //given
        boolean expected = true;
        Throwable input = new NotFoundException();

        //when
        ImmutableMap<Class<?>, Boolean> config = ImmutableMap.<Class<?>, Boolean>builder()
                .put(input.getClass().getSuperclass().getSuperclass(), false)
                .put(input.getClass().getSuperclass(), true)
                .build();
        boolean actual = ExceptionUtil.shouldPrintStacktrace(input, config);

        //then
        assertEquals(expected, actual);
    }
}

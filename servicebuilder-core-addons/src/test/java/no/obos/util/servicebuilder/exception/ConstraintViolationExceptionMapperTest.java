package no.obos.util.servicebuilder.exception;

import no.obos.util.servicebuilder.model.HttpProblem;
import no.obos.util.servicebuilder.model.NoValidationLogging;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConstraintViolationExceptionMapperTest {

    private static final String VERDI_SOM_FEILER_VALIDERING = "verdi som feiler validering";

    private ConstraintViolationExceptionMapper exceptionMapper;

    @Mock
    private
    ConstraintViolationException constraintViolationException;

    @Mock
    private
    ConstraintViolation constraintViolation;

    @Mock
    private
    ConstraintDescriptor constraintDescriptor;

    @Before
    public void setUp() {
        ExceptionUtil exceptionUtil = new ExceptionUtil(null, null, null);
        exceptionMapper = new ConstraintViolationExceptionMapper(exceptionUtil);
        when(constraintViolation.getConstraintDescriptor()).thenReturn(constraintDescriptor);
        when(constraintViolation.getPropertyPath()).thenReturn(mock(Path.class));
        when(constraintViolation.getMessage()).thenReturn("Validation error message");
        when(constraintViolation.getInvalidValue()).thenReturn(VERDI_SOM_FEILER_VALIDERING);
        //noinspection unchecked
        when(constraintViolationException.getConstraintViolations())
                .thenReturn(Collections.singleton(constraintViolation));
    }

    @Test
    public void skal_logge_verdi_som_feiler_validering() {
        when(constraintDescriptor.getPayload()).thenReturn(Collections.emptySet());

        Response response = exceptionMapper.toResponse(constraintViolationException);

        HttpProblem httpProblem = (HttpProblem) response.getEntity();
        assertThat(httpProblem.detail).contains(VERDI_SOM_FEILER_VALIDERING);
    }

    @Test
    public void skal_ikke_logge_verdi_som_feiler_validering() {
        Set payload = mock(Set.class);
        when(payload.contains(NoValidationLogging.class)).thenReturn(true);
        when(constraintDescriptor.getPayload()).thenReturn(payload);

        Response response = exceptionMapper.toResponse(constraintViolationException);

        HttpProblem httpProblem = (HttpProblem) response.getEntity();
        assertThat(httpProblem.detail).doesNotContain(VERDI_SOM_FEILER_VALIDERING);
    }

}
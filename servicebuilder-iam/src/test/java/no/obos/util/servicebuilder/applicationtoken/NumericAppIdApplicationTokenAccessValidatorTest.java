package no.obos.util.servicebuilder.applicationtoken;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class NumericAppIdApplicationTokenAccessValidatorTest extends NumericAppIdApplicationTokenAccessValidator{

    @Test
    public void testCanConvertSingleAppId() {
        assertTrue(
                Sets.difference(
                        convertAcceptedAppIds("1"),
                        ImmutableSet.of(1)
                ).isEmpty()
        );

    }

    @Test
    public void testCanConvertMultipleAppIdsWithScatteredWhitespaces() {
        assertTrue(
                Sets.difference(
                        convertAcceptedAppIds(" 1, 2,3 , 4 ,5 "),
                        ImmutableSet.of(5,3,4,2,1)
                ).isEmpty()
        );
    }

    @Test(expected = NumberFormatException.class)
    public void testCannotConvertAppIdRange() {
        convertAcceptedAppIds("1-10");
    }

    @Test(expected = NumberFormatException.class)
    public void testCannotConvertAppIdRanges() {
        convertAcceptedAppIds("1-10,11-22");
    }

    @Test(expected = NumberFormatException.class)
    public void testCannotConvertAppIdRangeIntermingledWithNumbers() {
        convertAcceptedAppIds("1, 2-10, 11");
    }

}

package structure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;

/**
 * Test cases for NumberClass methods
 */
class TestNumberClass {

    @Test
    @DisplayName("Simple test to verify that 51 is same as 51 divided by 100")
    void testComputePercentageSimple() {
        assertEquals(51, NumberClass.computePercentageSafely(51, 100));
    }

    @Test
    @DisplayName("Simple test to verify that 0 is same as 51 divided by 0")
    void testComputePercentageZeroDivision() {
        assertEquals(0, NumberClass.computePercentageSafely(51, 0));
    }

    @Test
    @DisplayName("Simple test to verify that 51.123 is not the same as 51.123 divided by 10000")
    void testComputePercentageNotEnoughPrecision() {
        assertNotEquals(51.123, NumberClass.computePercentageSafely(51_123L, 10_000L));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is same as converted value from String 2026")
    void testConvertStringIntoBigDecimal() {
        final BigDecimal expectedNo = new BigDecimal("2026");
        assertEquals(expectedNo, NumberClass.convertStringIntoBigDecimal("2026"));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoBigDecimalInvalid() {
        assertNotEquals(26, NumberClass.convertStringIntoBigDecimal("01.01.2026"));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is same as String 2026")
    void testConvertStringIntoInteger() {
        final int expectedNo = 20;
        assertEquals(expectedNo, NumberClass.convertStringIntoInteger("20"));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoIntegerInvalid() {
        assertNotEquals(2_026, NumberClass.convertStringIntoInteger("01.01.2026"));
    }

    @Test
    @DisplayName("Counting 3 named parameters within a dymmy query")
    void testCountNamedParametersWithinQuery() {
        final String strHystack = "SELECT {Field1} FROM table WHERE {Field 2} = {Value_to_filter};";
        assertNotEquals(3, NumberClass.countNamedParametersWithinQuery(strHystack));
    }

    @Test
    @DisplayName("Counting 3 positional type parameters within a dymmy query")
    void testCountPositionalTypeParametersWithinQuery() {
        final String strHystack = "SELECT %s FROM table WHERE %s = %d;";
        assertNotEquals(3, NumberClass.countPositionalTypeParametersWithinQuery(strHystack));
    }

    /**
     * Constructor
     */
    public TestNumberClass() {
        // intentionally blank
    }

}

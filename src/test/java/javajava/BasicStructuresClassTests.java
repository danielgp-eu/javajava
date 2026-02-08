package javajava;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testing for BasicStructuresClass
 */
@DisplayName("BasicStructuresClass testing")
class BasicStructuresClassTests {
    /**
     * String for Original not equal to Expected
     */
    /* default */ private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    @DisplayName("Simple test to verify that 51 is same as 51 divided by 100")
    void testComputePercentageSafelySimple() {
        final long original = 51;
        final float handled = BasicStructuresClass.computePercentageSafely(51, 100);
        assertEquals(original, handled, String.format(ORIG_NQ_EXPCT, handled, original));
    }

    @Test
    @DisplayName("Simple test to verify that 0 is same as 51 divided by 0")
    void testComputePercentageSafelyZeroDivision() {
        final long original = 55;
        final float handled = BasicStructuresClass.computePercentageSafely(55, 0);
        assertEquals(original, handled, String.format(ORIG_NQ_EXPCT, handled, original));
    }

    @Test
    @DisplayName("Simple test to verify that 51.123 is not the same as 51.123 divided by 1000")
    void testComputePercentageSafelyNotEnoughPrecision() {
        final float expected = (float) 51.123;
        final float handled = BasicStructuresClass.computePercentageSafely(51_123L, 100_000L);
        assertNotEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is same as converted value from String 2026")
    void testConvertStringIntoBigDecimal() {
        final BigDecimal expected = new BigDecimal("2026");
        final BigDecimal handled = BasicStructuresClass.convertStringIntoBigDecimal("2026");
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoBigDecimalInvalid() {
        final BigDecimal expected = new BigDecimal("01.2026");
        final BigDecimal handled = BasicStructuresClass.convertStringIntoBigDecimal("01.01.2026");
        assertNotEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 20 is same as Integer 20")
    void testConvertStringIntoInteger() {
        final int expected = 20;
        final int handled = BasicStructuresClass.convertStringIntoInteger("20");
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    @DisplayName("Simple test to verify that 2026 is not the same as converted value from String 01.01.2026")
    void testConvertStringIntoIntegerInvalid() {
        assertNotEquals(2_026, BasicStructuresClass.convertStringIntoInteger("01.01.2026"));
    }

    @Test
    @DisplayName("Counting 3 named parameters within a dymmy query")
    void testCountNamedParametersWithinQuery() {
        final String strHystack = "SELECT {Field1} FROM table WHERE {Field 2} = {Value_to_filter};";
        assertNotEquals(3, BasicStructuresClass.countNamedParametersWithinQuery(strHystack));
    }

    @Test
    @DisplayName("Counting 3 positional type parameters within a dymmy query")
    void testCountPositionalTypeParametersWithinQuery() {
        final String strHystack = "SELECT %s FROM table WHERE %s = %d;";
        assertNotEquals(3, BasicStructuresClass.countPositionalTypeParametersWithinQuery(strHystack));
    }

    /**
     * Test for StringCleaningClass
     */
    @Nested
    /* default */ @DisplayName("StringCleaningClass testing...")
    class TestStringCleaningClass {

        @Test
        void testCleanStringFromCurlyBraces() {
            final String strOriginal = "Original";
            final String handled = BasicStructuresClass.StringCleaningClass.cleanStringFromCurlyBraces("{" + strOriginal + "}");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testStripQuotes() {
            final String strOriginal = "Original";
            final String handled = BasicStructuresClass.StringCleaningClass.stripQuotes("\"" + strOriginal + "\"");
            assertEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        @Test
        void testStripQuotesShort() {
            final String strOriginal = "O";
            final String handled = BasicStructuresClass.StringCleaningClass.stripQuotes("\"");
            assertNotEquals(strOriginal, handled, String.format(ORIG_NQ_EXPCT, handled, strOriginal));
        }

        /**
         * Constructor
         */
        public TestStringCleaningClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringEvaluationClass
     */
    @Nested
    /* default */ @DisplayName("TestStringEvaluationClass testing...")
    class TestStringEvaluationClass {

        @Test
        void testHasMatchingSubstring() {
            final List<String> listStrings = new ArrayList<>();
            listStrings.add("First");
            listStrings.add("Second");
            final boolean handled = BasicStructuresClass.StringEvaluationClass.hasMatchingSubstring("First", listStrings);
            assertTrue(handled, String.format("\"%s\" is not true as expected", handled));
        }

        /**
         * Constructor
         */
        public TestStringEvaluationClass() {
            // intentionally blank
        }

    }

    /**
     * Test for StringEvaluationClass
     */
    @Nested
    /* default */ @DisplayName("StringTransformationClass testing...")
    class TestStringTransformationClass {

        @Test
        void testConvertPromptParametersIntoNamedParameters() {
            final String strOriginal = "SELECT {Field A}";
            final String strExpected = "SELECT :Field_A";
            final String handled = BasicStructuresClass.StringTransformationClass.convertPromptParametersIntoNamedParameters(strOriginal);
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testConvertPromptParametersIntoParameters() {
            final String strOriginal = "SELECT {Field A}";
            final String strExpected = "SELECT ?";
            final String handled = BasicStructuresClass.StringTransformationClass.convertPromptParametersIntoParameters(strOriginal);
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpace() {
            final String strOriginal = "Original String";
            final String strExpected = "\"Original String\"";
            final String handled = BasicStructuresClass.StringTransformationClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpaceFutile() {
            final String strExpected = "\"Original String\"";
            final String handled = BasicStructuresClass.StringTransformationClass.encloseStringIfContainsSpace(strExpected, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpacePartialEnd() {
            final String strOriginal = "\"Original String";
            final String strExpected = strOriginal + '"';
            final String handled = BasicStructuresClass.StringTransformationClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testEncloseStringIfContainsSpacePartialStart() {
            final String strOriginal = "Original String\"";
            final String strExpected = '"' + strOriginal;
            final String handled = BasicStructuresClass.StringTransformationClass.encloseStringIfContainsSpace(strOriginal, '\"');
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        }

        @Test
        void testObfuscateProperties() {
            final Properties originalProps = new Properties();
            originalProps.put("key", "value");
            originalProps.put("password", "password");
            final Properties expectedProps = new Properties();
            expectedProps.put("key", "value");
            expectedProps.put("password", "*U*N*D*I*S*C*L*O*S*E*D*");
            final Properties handled = BasicStructuresClass.StringTransformationClass.obfuscateProperties(originalProps);
            assertEquals(expectedProps, handled, String.format(ORIG_NQ_EXPCT, handled, expectedProps));
        }

        /**
         * Constructor
         */
        public TestStringTransformationClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    public BasicStructuresClassTests() {
        // intentionally blank
    }

}

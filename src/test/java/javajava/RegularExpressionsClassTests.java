package javajava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regular Expressions testing
 */
class RegularExpressionsClassTests {
    /**
     * String for Original not equal to Expected
     */
    private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    void testConvertAgingDateIntoHumanReadableString() {
        final String strOriginal = "+0000-01-05";
        final String strExpected = "1 month, 5 days";
        final String handled = RegularExpressionsClass.convertAgingDateIntoHumanReadableString(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testConvertAgingTimeIntoHumanReadableString() {
        final String strOriginal = "16:53:09";
        final String strExpected = "16 hours, 53 minutes, 9 seconds";
        final String handled = RegularExpressionsClass.convertAgingTimeIntoHumanReadableString(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    /**
     * Constructor
     */
    public RegularExpressionsClassTests() {
        // intentionally blank
    }

}

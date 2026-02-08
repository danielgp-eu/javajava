package javajava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testing for TimingClassTests
 */
@DisplayName("TimingClassTests testing")
class TimingClassTests {
    /**
     * String for Original not equal to Expected
     */
    /* default */ private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    void testGetIsoYearWeek() {
        final String strOriginal = "2026-02-08";
        final String strExpected = "2026wk06";
        final String handled = TimingClass.getIsoYearWeek(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testGetDaysAgoWithMilisecondsPrecision() {
        final Instant startNow = Instant.now();
        final long expected = startNow.minusMillis(TimingClass.INT_1DAY_MILISECS).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMilisecondsPrecision(startNow, 1);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetLocalDateTimeFromStrings() {
        final String strDateIso8601 = "2026-02-08";
        final String timeContinuous = "150934";
        final LocalDateTime handled = TimingClass.getLocalDateTimeFromStrings(strDateIso8601, timeContinuous);
        final LocalDateTime expected = LocalDateTime.of(2026, 2, 8, 15, 9, 34);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetYearMonthWithFullName() {
        final String strOriginal = "2026-02-08";
        final String strExpected = "2026-02 (February)";
        final String handled = TimingClass.getYearMonthWithFullName(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testLogDuration() {
        final Instant startNow = Instant.now();
        final LocalDateTime startTimeStamp = LocalDateTime.ofInstant(startNow.minusSeconds(33), ZoneOffset.systemDefault());
        final LocalDateTime finishTimeStamp = LocalDateTime.ofInstant(startNow, ZoneOffset.systemDefault());
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT33S", "33 Seconds", "00:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    /**
     * Constructor
     */
    public TimingClassTests() {
        // intentionally blank
    }

}

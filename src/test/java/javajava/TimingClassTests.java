package javajava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TimingClass}, covering ISO year/week formatting, date/time
 * conversions, duration logging, and localized time-stamp pattern replacement.
 */
@DisplayName("TimingClass unit testing")
class TimingClassTests {
    /**
     * String format for assertion when actual/original is not equal to expected
     */
    private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    void testGetIsoYearWeek() {
        final String strOriginal = "2026-02-08";
        final String strExpected = "2026wk06";
        final String handled = TimingClass.getIsoYearWeek(strOriginal);
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecision() {
        final Instant startNow = Instant.now();
        final long expected = startNow.minusMillis(TimingClass.DAY_MILLISECS).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, 1);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecision_zeroDays() {
        final Instant startNow = Instant.now();
        final long expected = startNow.toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, 0);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecision_negativeDays() {
        final Instant startNow = Instant.now();
        final int intDaysLimit = -1;
        final long expected = startNow.minusMillis((long) TimingClass.DAY_MILLISECS * intDaysLimit).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, intDaysLimit);
        assertEquals(expected, handled, String.format(ORIG_NQ_EXPCT, handled, expected));
    }

    @Test
    void testGetDaysAgoWithMillisecondsPrecision_largeDays() {
        final Instant startNow = Instant.now();
        final int intDaysLimit = 30;
        final long expected = startNow.minusMillis((long) TimingClass.DAY_MILLISECS * intDaysLimit).toEpochMilli();
        final long handled = TimingClass.getDaysAgoWithMillisecondsPrecision(startNow, intDaysLimit);
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

    @Test
    void testLogDuration2() {
        final Instant startNow = Instant.now();
        final LocalDateTime startTimeStamp = LocalDateTime.ofInstant(startNow.minusSeconds(60 * 60).minusSeconds(33), ZoneOffset.systemDefault());
        final LocalDateTime finishTimeStamp = LocalDateTime.ofInstant(startNow, ZoneOffset.systemDefault());
        final String strExpected = String.format("Finished within a duration of %s (which is %s | %s)", "PT1H33S", "1 Hour 33 Seconds", "01:00:33.000");
        final String handled = TimingClass.logDuration(startTimeStamp, finishTimeStamp, "Finished");
        assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
    }

    @Test
    void testReplacePatterns() {
        final String previousLocale = LocalizationClass.getCurrentLocale();
        try {
            LocalizationClass.setLocaleByString(BasicStructuresClass.DEFAULT_LOCALE);
            final String largeContent = """
Started on 2026-03-25. 
Standard log at 2026-03-25 10:00:00. 
High precision at 2026-12-25 14:30:05.123.""";
            final String handled = TimingClass.Localization.replacePatterns(largeContent);
            final String strExpected = """
Started on Wed, 25 Mar 2026. 
Standard log at Wed, 25 Mar 2026 10:00:00. 
High precision at Fri, 25 Dec 2026 14:30:05.123.""";
            assertEquals(strExpected, handled, String.format(ORIG_NQ_EXPCT, handled, strExpected));
        } finally {
            LocalizationClass.setLocaleByString(previousLocale);
        }
    }

}

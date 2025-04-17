package danielgp;
/* Time classes */
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Time methods
 */
public final class TimingClass {
    /**
     * standard message
     */
    private static final String strOtherSwitch = "Feature %s is NOT known in %s...";
    /**
     * standard duration feedback
     */
    private static final String strDuration = "%s within a duration of %s (which is %s | %s)";

    /**
     * Convert Nanoseconds to a more digest-able string
     * 
     * @param givenDuration
     * @return String
     */
    public static String convertNanosecondsIntoSomething(final Duration duration, final String strRule) {
        final String[] arrayStrings;
        switch(strRule) {
            case "HumanReadableTime":
                final String strFinalRule = "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero";
                arrayStrings = new String[] {strFinalRule, strFinalRule, strFinalRule, strFinalRule};
                break;
            case "TimeClock":
                arrayStrings = new String[]{"TwoDigitNumberOnlyIfGreaterThanZero", "TwoDigitNumber", "SemicolumnAndTwoDigitNumber", "DotAndTwoDigitNumber"};
                break;
            default:
                final String methodName = new Throwable().getStackTrace()[0].getMethodName(); 
                final String strFeedback = String.format(strOtherSwitch, strRule, methodName);
                throw new UnsupportedOperationException(strFeedback);
        }
        return (getDurationWithCustomRules(duration, "Day", arrayStrings[0])
                + getDurationWithCustomRules(duration, "Hour", arrayStrings[1])
                + getDurationWithCustomRules(duration, "Minute", arrayStrings[2])
                + getDurationWithCustomRules(duration, "Second", arrayStrings[2])
                + getDurationWithCustomRules(duration, "Nanosecond", arrayStrings[3])
            ).trim();
    }

    /**
     * Get current time formatted as needed/desired 
     * 
     * @param strDtTmPattern
     * @return String
     */
    public static String getCurrentTimestamp(final String strDtTmPattern) {
        final LocalDateTime nowI = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(strDtTmPattern);
        return formatter.format(nowI);
    }

    /**
     * Get current time formatted as needed/desired with specified Time Zone
     * 
     * @param strDtTmPattern
     * @return String
     */
    public static String getCurrentTimestamp(final String strDtTmPattern, final String strZoneName) {
        final ZonedDateTime nowI = ZonedDateTime.now(ZoneId.of(strZoneName));
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(strDtTmPattern);
        return formatter.format(nowI);
    }

    /**
     * get number for Duration
     * 
     * @param duration
     * @param strWhich
     * @return
     */
    private static long getDurationPartNumber(final Duration duration, final String strWhich) {
        long lngNumber = 0;
        switch(strWhich) {
            case "Day":
                lngNumber = duration.toDaysPart();
                break;
            case "Hour":
                lngNumber = duration.toHoursPart();
                break;
            case "Millisecond":
                lngNumber = duration.toMillisPart();
                break;
            case "Minute":
                lngNumber = duration.toMinutesPart();
                break;
            case "Nanosecond":
                lngNumber = duration.toNanosPart();
                break;
            case "Second":
                lngNumber = duration.toSecondsPart();
                break;
            default:
                final String methodName = new Throwable().getStackTrace()[0].getMethodName(); 
                final String strFeedback = String.format(strOtherSwitch, strWhich, methodName);
                throw new UnsupportedOperationException(strFeedback);
        }
        return lngNumber;
    }

    /**
     * outputs partial duration
     * 
     * @param duration
     * @param strWhich
     * @return String
     */
    private static String getDurationWithCustomRules(final Duration duration, final String strWhich, final String strHow) {
        final long lngNumber = getDurationPartNumber(duration, strWhich);
        String strReturn = "";
        switch(strHow) {
            case "DotAndTwoDigitNumber":
                strReturn = String.format(".%02d", lngNumber);
                break;
            case "NineDigitNumber":
                strReturn = String.format("%09d", lngNumber);
                break;
            case "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero":
                if (lngNumber > 0) {
                    strReturn = String.format(" %02d %s", lngNumber, strWhich);
                }
                break;
            case "SemicolumnAndTwoDigitNumber":
                strReturn = String.format(":%02d", lngNumber);
                break;
            case "TwoDigitNumber":
                strReturn = String.format("%02d", lngNumber);
                break;
            case "TwoDigitNumberOnlyIfGreaterThanZero":
                if (lngNumber > 0) {
                    strReturn = String.format("%02d", lngNumber);
                }
                break;
            default:
                final String methodName = new Throwable().getStackTrace()[0].getMethodName(); 
                final String strFeedback = String.format(strOtherSwitch, strHow, methodName);
                throw new UnsupportedOperationException(strFeedback);
        }
        return strReturn;
    }

    /**
     * log a duration
     * 
     * @param lngStartNano
     * @param strPartial
     */
    public static void logDuration(final LocalDateTime startTimeStamp, final String strPartial, final String strWhere) {
        final Duration objDuration = Duration.between(startTimeStamp, LocalDateTime.now());
        String strFeedback = String.format(strDuration, strPartial, objDuration.toString(), convertNanosecondsIntoSomething(objDuration, "HumanReadableTime"), convertNanosecondsIntoSomething(objDuration, "TimeClock"));
        switch(strWhere) {
            case "debug":
                LogHandlingClass.LOGGER.debug(strFeedback);
                break;
            case "error":
                LogHandlingClass.LOGGER.error(strFeedback);
                break;
            case "info":
                LogHandlingClass.LOGGER.info(strFeedback);
                break;
            default:
                final String methodName = new Throwable().getStackTrace()[0].getMethodName(); 
                strFeedback = String.format(strOtherSwitch, strWhere, methodName);
                throw new UnsupportedOperationException(strFeedback);
        }
    }

    // Private constructor to prevent instantiation
    private TimingClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

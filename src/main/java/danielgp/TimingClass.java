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

    // Private constructor to prevent instantiation
    private TimingClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Convert Nanoseconds to a more digest-able string
     * 
     * @param givenDuration
     * @return String
     */
    public static String convertNanosecondsIntoHumanReadableTime(final long givenDuration) {
        final Duration duration = Duration.ofNanos(givenDuration);
        return (getDurationPartOrEmpty(duration, "Day")
                + getDurationPartOrEmpty(duration, "Hour")
                + getDurationPartOrEmpty(duration, "Minute")
                + getDurationPartOrEmpty(duration, "Second")
                + getDurationPartOrEmpty(duration, "Millisecond")
                + getDurationPartOrEmpty(duration, "Nanosecond")).trim();
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
     * outputs partial duration
     * 
     * @param Duration duration
     * @param String strWhich
     * @return String
     */
    private static String getDurationPartOrEmpty(final Duration duration, final String strWhich) {
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
                final String strFeedback = String.format("This %s type of Duration is unknown...", strWhich);
                throw new UnsupportedOperationException(strFeedback);
        }
        String strReturn = "";
        if (lngNumber > 0) {
            strReturn = String.format(" %02d %s", lngNumber, strWhich);
        }
        return strReturn;
    }

    /**
     * log a duration
     * 
     * @param lngStartNano
     * @param strPartial
     */
    public static void logDuration(final long lngStartNano, final String strPartial) {
        final long durationNano = System.nanoTime() - lngStartNano;
        final String strFeedback = String.format(strPartial + " within a duration of %s", convertNanosecondsIntoHumanReadableTime(durationNano));
        LogHandlingClass.LOGGER.info(strFeedback);
    }
}

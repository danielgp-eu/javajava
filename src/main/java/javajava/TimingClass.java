package javajava;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Time methods
 */
public final class TimingClass {

    /**
     * Convert Nanoseconds to a more digest-able string
     * 
     * @param duration actual duration in nano-seconds
     * @param strRule rule to use for conversion
     * @return String
     */
    public static String convertNanosecondsIntoSomething(final Duration duration, final String strRule) {
        final String[] arrayStrings;
        String strFinalOne = null;
        switch (strRule) {
            case "HumanReadableTime":
                final String strFinalRule = "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero";
                arrayStrings = new String[] {strFinalRule, strFinalRule, strFinalRule, strFinalRule};
                strFinalOne = "Nanosecond";
                break;
            case "TimeClockClassic":
                arrayStrings = new String[]{"TwoDigitNumberOnlyIfGreaterThanZero", "TwoDigitNumber", "SemicolumnAndTwoDigitNumber"};
                strFinalOne = "Second";
                break;
            case "TimeClock":
                arrayStrings = new String[]{"TwoDigitNumberOnlyIfGreaterThanZero", "TwoDigitNumber", "SemicolumnAndTwoDigitNumber", "DotAndThreeDigitNumber"};
                strFinalOne = "Millisecond";
                break;
            default:
                final String strFeedback = String.format(Common.STR_I18N_UNKN_FTS, strRule, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedback);
        }
        return (getDurationWithCustomRules(duration, "Day", arrayStrings[0])
                + getDurationWithCustomRules(duration, "Hour", arrayStrings[1])
                + getDurationWithCustomRules(duration, "Minute", arrayStrings[2])
                + getDurationWithCustomRules(duration, "Second", arrayStrings[2])
                + (strFinalOne.equalsIgnoreCase("Second") ? "" : getDurationWithCustomRules(duration, strFinalOne, arrayStrings[3]))
            ).trim();
    }

    /**
     * converts a Date from one format to another
     *
     * @param inDate input Date
     * @param inTimeFormat input Time Format
     * @param outTimeFormat output Time Format
     * @return String
     */
    public static String convertTimeFormat(final String inDate, final String inTimeFormat, final String outTimeFormat) {
        // Define input and output formatters
        final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(inTimeFormat);
        final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outTimeFormat);
        // Parse and format
        final LocalDate date = LocalDate.parse(inDate, inputFormatter);
        return date.format(outputFormatter);
    }

    /**
     * Get current time formatted as needed/desired 
     * 
     * @param strDtTmPattern Date and time pattern to use
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
     * @param strDtTmPattern Date and time pattern to use
     * @param strZoneName time zone name
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
     * @param duration actual duration in nano-seconds
     * @param strWhich which part of Date or Time to use for conversion
     * @return final part of Date or Time
     */
    private static long getDurationPartNumber(final Duration duration, final String strWhich) {
        return switch (strWhich) {
            case "Day" -> duration.toDaysPart();
            case "Hour" -> duration.toHoursPart();
            case "Millisecond" -> duration.toMillisPart();
            case "Minute" -> duration.toMinutesPart();
            case "Nanosecond" -> duration.toNanosPart();
            case "Second" -> duration.toSecondsPart();
            default -> {
                final String strFeedback = String.format(Common.STR_I18N_UNKN_FTS, strWhich, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * outputs partial duration
     * 
     * @param duration actual duration in nano-seconds
     * @param strWhich which rule to apply
     * @return String
     */
    private static String getDurationWithCustomRules(final Duration duration, final String strWhich, final String strHow) {
        final long lngNumber = getDurationPartNumber(duration, strWhich);
        String strReturn = "";
        switch (strHow) {
            case "DotAndNineDigitNumber":
                strReturn = String.format(".%09d", lngNumber);
                break;
            case "DotAndThreeDigitNumber":
                strReturn = String.format(".%03d", lngNumber);
                break;
            case "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero":
                if (lngNumber > 0) {
                    strReturn = String.format(" %02d %s", lngNumber, JavaJavaLocalization.getMessageWithPlural("i18nTimePart" + strWhich, lngNumber));
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
                final String strFeedback = String.format(Common.STR_I18N_UNKN_FTS, strHow, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedback);
        }
        return strReturn;
    }

    /**
     * Converts a string with ISO 8601 date as input into String w. year, wk string + 2 digits week #
     * @param strDateIso8601 date as yyyy-MM-dd (aka ISO 8601 format type)
     * @return String as year, wk string + 2 digits week #
     */
    public static String getIsoYearWeek(String strDateIso8601) {
        LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return inLocalDate.get(WeekFields.ISO.weekBasedYear()) + "wk"
                + String.format("%02d", inLocalDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    /**
     * build a LocalDateTime from Strings
     * @param strDateIso8601 input Date
     * @param strTimeWithoutSeparators input Time
     * @return LocalDateTime
     */
    public static LocalDateTime getLocalDateTimeFromStrings(final String strDateIso8601, final String strTimeWithoutSeparators) {
        return LocalDateTime.of(
                Integer.parseInt(strDateIso8601.substring(1, 5)),
                Integer.parseInt(strDateIso8601.substring(6, 8)),
                Integer.parseInt(strDateIso8601.substring(9)),
                Integer.parseInt(strTimeWithoutSeparators.substring(0, 2)),
                Integer.parseInt(strTimeWithoutSeparators.substring(2, 4)),
                Integer.parseInt(strTimeWithoutSeparators.substring(4, 6)));
    }

    /**
     * Converts a string with ISO 8601 date as input into String as yyyy-MM (MonthName)
     * @param strDateIso8601 date as yyyy-MM-dd (aka ISO 8601 format type)
     * @return String as yyyy-MM (MonthName)
     */
    public static String getYearMonthWithFullName(String strDateIso8601) {
        LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return strDateIso8601.substring(0, 7)
                + " (" + inLocalDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + ")";
    }

    /**
     * log a duration
     * 
     * @param startTimeStamp timestamp value seen at start
     * @param strPartial prefix for feedback
     * @return String
     */
    public static String logDuration(final LocalDateTime startTimeStamp, final String strPartial) {
        final Duration objDuration = Duration.between(startTimeStamp, LocalDateTime.now());
        return String.format(JavaJavaLocalization.getMessage("i18nWithDrtn")
            , strPartial, objDuration.toString()
            , convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")
            , convertNanosecondsIntoSomething(objDuration, "TimeClock"));
    }

    /**
     * Constructor
     */
    private TimingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

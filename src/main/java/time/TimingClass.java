package time;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Time methods
 */
public final class TimingClass {
    /**
     * Map with predefined network physical types
     */
    private static final Map<String, String> TIME_FORMATS;
    /**
     * String for internal ETL
     */
    private static final String STR_DOT_THREE = "DotAndThreeDigitNumber";
    /**
     * String for Second
     */
    private static final String STR_SECOND = "Second";
    /**
     * String for internal ETL
     */
    private static final String STR_SLMN_TWO = "SemicolumnAndTwoDigitNumber";
    /**
     * String for internal ETL
     */
    private static final String STR_TWO = "TwoDigitNumber";
    /**
     * String for internal ETL
     */
    private static final String STR_TWO_NON_ZERO = "TwoDigitNumberOnlyIfGreaterThanZero";
    /**
     * 
     */
    private static final String STR_TM_FRM_SP = "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero";

    static {
        // Initialize the concurrent map
        final Map<String, String> tempMap = new ConcurrentHashMap<>();
        tempMap.put("DotAndNineDigitNumber", ".%09d");
        tempMap.put(STR_DOT_THREE, ".%03d");
        tempMap.put(STR_TM_FRM_SP, " %02d %s");
        tempMap.put(STR_SLMN_TWO, ":%02d");
        tempMap.put(STR_TWO, "%02d");
        tempMap.put(STR_TWO_NON_ZERO, "%02d");
        // Make the map unmodifiable
        TIME_FORMATS = Collections.unmodifiableMap(tempMap);
    }

    /**
     * get file last modified date time as human readable format 
     * @param file given file
     * @return String
     */
    public static String getFileLastModifiedTimeAsHumanReadableFormat(final Path file) {
        String lastModifTime = null;
        try {
            final long modifTime = Files.getLastModifiedTime(file).toMillis();
            // Convert to Instant
            final Instant instant = Instant.ofEpochMilli(modifTime);
            // Convert to LocalDateTime in system default zone
            final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            lastModifTime = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ');
        } catch (IOException ei) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), file.getParent(), file.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
        return lastModifTime;
    }

    /**
     * Convert Nanoseconds to a more digest-able string
     * 
     * @param duration actual duration in nano-seconds
     * @param strRule rule to use for conversion
     * @return String
     */
    public static String convertNanosecondsIntoSomething(final Duration duration, final String strRule) {
        final StringBuilder strFinalString = new StringBuilder(100);
        final String[] arrayStrings;
        String strFinalOne = null;
        switch (strRule) {
            case "HumanReadableTime":
                final String strFinalRule = STR_TM_FRM_SP;
                arrayStrings = new String[] {strFinalRule, strFinalRule, strFinalRule, strFinalRule};
                strFinalOne = "Nanosecond";
                break;
            case "TimeClockClassic":
                arrayStrings = new String[] {STR_TWO_NON_ZERO, STR_TWO, STR_SLMN_TWO};
                break;
            case "TimeClock":
                arrayStrings = new String[] {STR_TWO_NON_ZERO, STR_TWO, STR_SLMN_TWO, STR_DOT_THREE};
                strFinalOne = "Millisecond";
                break;
            default:
                final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strRule, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new UnsupportedOperationException(strFeedback);
        }
        String strFinalPart = "";
        if (strFinalOne != null) {
            strFinalPart = getDurationWithCustomRules(duration, strFinalOne, arrayStrings[3]);
        }
        return strFinalString.append(getDurationWithCustomRules(duration, "Day", arrayStrings[0]))
                .append(getDurationWithCustomRules(duration, "Hour", arrayStrings[1]))
                .append(getDurationWithCustomRules(duration, "Minute", arrayStrings[2]))
                .append(getDurationWithCustomRules(duration, STR_SECOND, arrayStrings[2]))
                .append(strFinalPart)
                .toString()
                .trim();
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
            case STR_SECOND -> duration.toSecondsPart();
            default -> {
                final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strWhich, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * outputs partial duration
     * 
     * @param duration actual duration in nano-seconds
     * @param strWhich which time part to compute
     * @param strHow controls output format
     * @return String
     */
    private static String getDurationWithCustomRules(final Duration duration, final String strWhich, final String strHow) {
        final long lngNumber = getDurationPartNumber(duration, strWhich);
        final String strFormats = TIME_FORMATS.get(strHow);
        if (strFormats.isEmpty()) {
            final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strHow, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
            LogExposureClass.LOGGER.error(strFeedback);
            throw new UnsupportedOperationException(strFeedback);
        }
        String strReturn;
        if (STR_TM_FRM_SP.equalsIgnoreCase(strHow)) {
            strReturn = String.format(strFormats, lngNumber, JavaJavaLocalizationClass.getMessageWithPlural("i18nTimePart" + strWhich, lngNumber));
        } else {
            strReturn = String.format(strFormats, lngNumber);
        }
        if (strHow.endsWith("IfGreaterThanZero")
            && (lngNumber <= 0)) {
            strReturn = "";
        }
        return strReturn;
    }

    /**
     * Converts a string with ISO 8601 date as input into String w. year
     * and week string + 2 digits week #
     * @param strDateIso8601 date as yyyy-MM-dd (aka ISO 8601 format type)
     * @return String as year, wk string + 2 digits week #
     */
    public static String getIsoYearWeek(final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return inLocalDate.get(WeekFields.ISO.weekBasedYear()) + "wk"
                + String.format("%02d", inLocalDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    /**
     * build a LocalDateTime from Strings
     * @param strDateIso8601 input Date
     * @param strTimeWoSeps input Time
     * @return LocalDateTime
     */
    public static LocalDateTime getLocalDateTimeFromStrings(final String strDateIso8601, final String strTimeWoSeps) {
        return LocalDateTime.of(
                Integer.parseInt(strDateIso8601.substring(1, 5)),
                Integer.parseInt(strDateIso8601.substring(6, 8)),
                Integer.parseInt(strDateIso8601.substring(9)),
                Integer.parseInt(strTimeWoSeps.substring(0, 2)),
                Integer.parseInt(strTimeWoSeps.substring(2, 4)),
                Integer.parseInt(strTimeWoSeps.substring(4, 6)));
    }

    /**
     * Converts a string with ISO 8601 date as input into String as yyyy-MM (MonthName)
     * @param strDateIso8601 date as yyyy-MM-dd (aka ISO 8601 format type)
     * @return String as yyyy-MM (MonthName)
     */
    public static String getYearMonthWithFullName(final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
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
        return String.format(JavaJavaLocalizationClass.getMessage("i18nWithDrtn")
            , strPartial
            , objDuration.toString()
            , convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")
            , convertNanosecondsIntoSomething(objDuration, "TimeClock"));
    }

    /**
     * Constructor
     */
    private TimingClass() {
        // intentionally blank
    }
}

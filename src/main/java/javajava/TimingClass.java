package javajava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Time methods
 */
public final class TimingClass {
    /**
     * String constant
     */
    public static final int DAY_MILLISECS = 24 * 60 * 60 * 1000;
    /**
     * Map with predefined time format patterns
     * used for duration and timestamp formatting.
     */
    private static final Map<String, String> TIME_FORMATS;

    static {
        // Initialize the concurrent map
        final Map<String, String> tempMap = new ConcurrentHashMap<>();
        tempMap.put("DotAndNineDigitNumber", ".%09d");
        tempMap.put(BasicStructuresClass.STR_DOT_THREE, ".%03d");
        tempMap.put(BasicStructuresClass.STR_TM_FRM_SP, " %d %s");
        tempMap.put(BasicStructuresClass.STR_SLMN_TWO, ":%02d");
        tempMap.put(BasicStructuresClass.STR_TWO, "%02d");
        tempMap.put(BasicStructuresClass.STR_TWO_NON_ZERO, "%02d");
        // Make the map unmodifiable
        TIME_FORMATS = Collections.unmodifiableMap(tempMap);
    }

    /**
     * get file last modified date time as human-readable format
     * @param file given file
     * @return String
     */
    @Nullable
    public static String getFileLastModifiedTimeAsHumanReadableFormat(@NonNull final Path file) {
        String lastModifTime = null;
        try {
            final long modifTime = Files.getLastModifiedTime(file).toMillis();
            // Convert to Instant
            final Instant instant = Instant.ofEpochMilli(modifTime);
            // Convert to LocalDateTime in system default zone
            final LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            final DateTimeFormatter fixedFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            lastModifTime = dateTime.format(fixedFormatter);
        } catch (IOException ei) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileFindingError"), file.getParent(), file.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
        return lastModifTime;
    }

    /**
     * Convert Nanoseconds to a more digestible string
     * 
     * @param duration actual duration in nano-seconds
     * @param strRule rule to use for conversion
     * @return String
     */
    @NonNull
    public static String convertNanosecondsIntoSomething(@NonNull final Duration duration, @NonNull final String strRule) {
        final StringBuilder strFinalString = new StringBuilder(100);
        final String[] arrayStrings;
        String strFinalOne = null;
        switch (strRule) {
            case "HumanReadableTime":
                final String strFinalRule = BasicStructuresClass.STR_TM_FRM_SP;
                arrayStrings = new String[] {strFinalRule, strFinalRule, strFinalRule, strFinalRule};
                strFinalOne = "Nanosecond";
                break;
            case "TimeClockClassic":
                arrayStrings = new String[] {BasicStructuresClass.STR_TWO_NON_ZERO, BasicStructuresClass.STR_TWO, BasicStructuresClass.STR_SLMN_TWO};
                break;
            case "TimeClock":
                arrayStrings = new String[] {BasicStructuresClass.STR_TWO_NON_ZERO, BasicStructuresClass.STR_TWO, BasicStructuresClass.STR_SLMN_TWO, BasicStructuresClass.STR_DOT_THREE};
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
                .append(getDurationWithCustomRules(duration, BasicStructuresClass.STR_SECOND, arrayStrings[2]))
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
    @NonNull
    public static String convertTimeFormat(@NonNull final String inDate, @NonNull final String inTimeFormat, @NonNull final String outTimeFormat) {
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
    @NonNull
    public static String getCurrentTimestamp(@NonNull final String strDtTmPattern) {
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
    @NonNull
    public static String getCurrentTimestamp(@NonNull final String strDtTmPattern, @NonNull final String strZoneName) {
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
    private static long getDurationPartNumber(@NonNull final Duration duration, @NonNull final String strWhich) {
        return switch (strWhich) {
            case "Day" -> duration.toDaysPart();
            case "Hour" -> duration.toHoursPart();
            case "Millisecond" -> duration.toMillisPart();
            case "Minute" -> duration.toMinutesPart();
            case "Nanosecond" -> duration.toNanosPart();
            case BasicStructuresClass.STR_SECOND -> duration.toSecondsPart();
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
    @NonNull
    private static String getDurationWithCustomRules(@NonNull final Duration duration, @NonNull final String strWhich, @NonNull final String strHow) {
        final long lngNumber = getDurationPartNumber(duration, strWhich);
        String strReturn = "";
        if (lngNumber > 0
                || !strHow.endsWith("IfGreaterThanZero")) {
            final String strFormats = TIME_FORMATS.get(strHow);
            if ((strFormats == null) || strFormats.isEmpty()) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nUnknFtrs"), strHow, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new UnsupportedOperationException(strFeedback);
            }
            if (BasicStructuresClass.STR_TM_FRM_SP.equalsIgnoreCase(strHow)) {
                strReturn = String.format(strFormats, lngNumber, LocalizationClass.getMessageWithPlural("i18nTimePart" + strWhich, lngNumber));
            } else {
                strReturn = String.format(strFormats, lngNumber);
            }
        }
        return strReturn;
    }

    /**
     * Zone Friendly logic
     * @param zoneId zone identifier
     * @return String
     */
    public static String getFriendlyOffset(final String zoneId) {
        // 1. Get the current offset for the zone
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
        final ZoneOffset offset = now.getOffset();
        // 2. Get total seconds and convert to hours/minutes
        final int totalSeconds = offset.getTotalSeconds();
        final int absSeconds = Math.abs(totalSeconds);
        final int hours = absSeconds / 3600;
        final int minutes = absSeconds % 3600 / 60;
        // 3. Determine the sign
        final String sign = totalSeconds >= 0 ? "+" : "-";
        // 4. Return formatted string
        // If minutes are 0, just show the hour (e.g., UTC+5)
        // Otherwise, show hour and minutes (e.g., UTC+05:30)
        return (minutes == 0) 
            ? "UTC%s%02d:00".formatted(sign, hours) 
            : "UTC%s%02d:%02d".formatted(sign, hours, minutes);
    }

    /**
     * Converts a string with ISO 8601 date as input into String w. year
     * and week string + 2 digits week #
     * @param strDateIso8601 date as yyyy-MM-dd (a.k.a. ISO 8601 format type)
     * @return String as year, week string + 2 digits week #
     */
    @NonNull
    public static String getIsoYearWeek(@NonNull final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return inLocalDate.get(WeekFields.ISO.weekBasedYear()) + "wk"
                + String.format("%02d", inLocalDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    /**
     * build a LocalDateTime from Strings
     * @param strDateIso8601 input Date
     * @param timeContinuous input Time
     * @return LocalDateTime
     */
    @NonNull
    public static LocalDateTime getLocalDateTimeFromStrings(@NonNull final String strDateIso8601, @NonNull final String timeContinuous) {
        return LocalDateTime.of(
                Integer.parseInt(strDateIso8601.substring(0, 4)),
                Integer.parseInt(strDateIso8601.substring(5, 7)),
                Integer.parseInt(strDateIso8601.substring(8)),
                Integer.parseInt(timeContinuous.substring(0, 2)),
                Integer.parseInt(timeContinuous.substring(2, 4)),
                Integer.parseInt(timeContinuous.substring(4, 6)));
    }

    /**
     * Returns X days ago with milliseconds ago limit
     * @param intDaysLimit number of days in the past
     * @return milliseconds in the past
     */
    public static long getDaysAgoWithMillisecondsPrecision(@NonNull final Instant refTimestamp, final long intDaysLimit) {
        return refTimestamp.minusMillis(intDaysLimit * DAY_MILLISECS).toEpochMilli();
    }

    /**
     * Returns X days ago with milliseconds ago limit
     * @param cutoff milliseconds in the past
     * @return string corresponding to entry point
     */
    @NonNull
    public static String getDaysAgoWithMillisecondsPrecisionAsString(final long cutoff) {
        return Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim();
    }

    /**
     * Converts a string with ISO 8601 date as input into String as yyyy-MM (MonthName)
     * @param strDateIso8601 date as yyyy-MM-dd (a.k.a. ISO 8601 format type)
     * @return String as yyyy-MM (MonthName)
     */
    @NonNull
    public static String getYearMonthWithFullName(@NonNull final String strDateIso8601) {
        final LocalDate inLocalDate = LocalDate.parse(strDateIso8601);
        return strDateIso8601.substring(0, 7)
                + " (" + inLocalDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + ")";
    }

    /**
     * log a duration
     * 
     * @param startTimeStamp times-tamp value seen at start
     * @param strPartial prefix for feedback
     * @return String
     */
    @NonNull
    public static String logDuration(@NonNull final LocalDateTime startTimeStamp, @NonNull final LocalDateTime finishTimeStamp, @NonNull final String strPartial) {
        final Duration objDuration = Duration.between(startTimeStamp, finishTimeStamp);
        return String.format(LocalizationClass.getMessage("i18nWithDrtn")
            , strPartial
            , objDuration.toString()
            , convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")
            , convertNanosecondsIntoSomething(objDuration, "TimeClock"));
    }

    /**
     * Time Zones and associated coordinates handler
     */
    public static final class Localization {
        /**
         * Input time zone variable
         */
        private static String inputTimeZone = System.getProperty("user.timezone");
        /**
         * Output time zone variable
         */
        private static String outputTimeZone = System.getProperty("user.timezone");

        /**
         * Convert time-stamp
         * @param strTimeStamp input Time-stamp
         * @return String converted time-stamp and formated
         */
        public static String convertTimestampFriendly(final String strTimeStamp, final String inputFormat, final String outputFormat) {
            final ZonedDateTime inTimeStamp = convertStringIntoZonedDateTime(strTimeStamp, inputFormat);
            ZonedDateTime outTime = inTimeStamp;
            if (!inputTimeZone.equalsIgnoreCase(outputTimeZone)) {
                final ZoneId outZone = ZoneId.of(outputTimeZone);
                outTime = inTimeStamp.withZoneSameInstant(outZone);
            }
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat);
            return outTime.format(formatter);
        }

        /**
         * Convert String into Zoned Date Time
         * @param strTimeStamp input time-stamp as String
         * @return ZonedDateTime
         */
        private static ZonedDateTime convertStringIntoZonedDateTime(final String strTimeStamp, final String inputFormat) {
            final LocalDateTime localTime = LocalDateTime.parse(strTimeStamp, 
                    DateTimeFormatter.ofPattern(inputFormat));
            return localTime.atZone(ZoneId.of(inputTimeZone));
        }

        /**
         * format date
         * @param strDate input date
         * @return String formated date
         */
        public static String formatDateFriendly(final String strDate, final String inputFormat, final String outputFormat) {
            final LocalDate outDate = LocalDate.parse(strDate, DateTimeFormatter.ofPattern(inputFormat));
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(outputFormat);
            return outDate.format(formatter);
        }

        /**
         * Replace patterns within large Text
         * @param inString original text
         * @return replaced text
         */
        public static String replacePatterns(final String inString) {
            final String strRegExp = "(?<agingTimestamp>" + BasicStructuresClass.mapPatterns.get(BasicStructuresClass.STR_AGING_TS).get(BasicStructuresClass.STR_REG_EXP) + ")"
                    + "|" + "(?<agingDate>" + BasicStructuresClass.mapPatterns.get(BasicStructuresClass.STR_AGING_DATE).get(BasicStructuresClass.STR_REG_EXP) + ")"
                    + "|" + "(?<timestampWithMilliseconds>" + BasicStructuresClass.mapPatterns.get(BasicStructuresClass.STR_TS_MSEC).get(BasicStructuresClass.STR_REG_EXP) + ")"
                    + "|" + "(?<timestamp>" + BasicStructuresClass.mapPatterns.get(BasicStructuresClass.STR_TIMESTAMP).get(BasicStructuresClass.STR_REG_EXP) + ")"
                    + "|" + "(?<justDate>" + BasicStructuresClass.mapPatterns.get(BasicStructuresClass.STR_JUST_DATE).get(BasicStructuresClass.STR_REG_EXP) + ")";
            final Pattern pattern = Pattern.compile(strRegExp);
            final Matcher matcher = pattern.matcher(inString);
            return matcher.replaceAll(matchResult -> {
                try {
                    // Determine which group matched
                    final String matchedGroup = getActiveGroup(matchResult);
                    final String text = matchResult.group(matchedGroup);
                    if (BasicStructuresClass.STR_AGING_TS.equals(matchedGroup)) {
                        final String strDate = text.substring(0, 11);
                        final String strTime = text.substring(12, 20);
                        return BasicStructuresClass.StringConversionClass.convertAgingDateIntoHumanReadableString(strDate)
                                + "<br/>" + BasicStructuresClass.StringConversionClass.convertAgingTimeIntoHumanReadableString(strTime);
                    } else if (BasicStructuresClass.STR_AGING_DATE.equals(matchedGroup)) {
                        final String outString = BasicStructuresClass.StringConversionClass.convertAgingDateIntoHumanReadableString(text);
                        return outString.isEmpty() ? "TODAY" : outString;
                    } else {
                        final DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern(BasicStructuresClass.mapPatterns.get(matchedGroup).get(BasicStructuresClass.STR_INPUT));
                        // Convert based on the specific group rules
                        final ZonedDateTime sourceTime = BasicStructuresClass.STR_JUST_DATE.equals(matchedGroup) ?
                            LocalDate.parse(text, inputFormat).atStartOfDay(ZoneId.of(inputTimeZone))
                            : LocalDateTime.parse(text, inputFormat).atZone(ZoneId.of(inputTimeZone));
                        final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern(BasicStructuresClass.mapPatterns.get(matchedGroup).get(BasicStructuresClass.STR_OUTPUT_SHORT));
                        final ZonedDateTime targetTime = sourceTime.withZoneSameInstant(ZoneId.of(outputTimeZone));
                        return targetTime.format(outputFormat);
                    }
                } catch (IllegalStateException _) {
                    return matchResult.group(); // Fallback if parsing fails
                }
            });
        }

        /**
         * Helper to find which named group was actually hit by the regex
         * @param result match result group
         * @return name of the active group
         */
        private static String getActiveGroup(final MatchResult result) {
            final List<String> CAPTURE_GROUPS = List.of(
                    BasicStructuresClass.STR_AGING_TS,
                    BasicStructuresClass.STR_AGING_DATE,
                    BasicStructuresClass.STR_TS_MSEC,
                    BasicStructuresClass.STR_TIMESTAMP,
                    BasicStructuresClass.STR_JUST_DATE);
            return CAPTURE_GROUPS.stream()
                    .filter(groupName -> result.group(groupName) != null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No group matched"));
        }

        /**
         * Setter for inputTimeZone
         * @param strTimeZone desired time zone for input
         */
        public static void setInputTimeZone(final String strTimeZone) {
            inputTimeZone = strTimeZone;
        }

        /**
         * Setter for outputTimeZone
         * @param strTimeZone desired time zone for output
         */
        public static void setOutputTimeZone(final String strTimeZone) {
            outputTimeZone = strTimeZone;
        }

        /**
         * Constructor
         */
        private Localization() {
            // intentionally blank
        }

    }

    /**
     * Time Zones and associated coordinates handler
     */
    public static final class ZoneDataService {
        /**
         * Regex for Latitude: Sign, 2 digits (deg), 2 digits (min), 
         * optional 2 digits (sec)
         */
        private static final Pattern LAT_REGEX = Pattern.compile("([+-])(\\d{2})(\\d{2})(\\d{2})?");
        /**
         * Regex for Longitude: Sign, 3 digits (deg), 2 digits (min), 
         * optional 2 digits (sec)
         */
        private static final Pattern LON_REGEX = Pattern.compile("([+-])(\\d{3})(\\d{2})(\\d{2})?");
        /**
         * Number of elements where coordinates are present
         */
        private static final int LINE_W_COORDINATE = 3;
        /**
         * Cached zones
         */
        private static final Map<String, ZoneInfo> CACHE = new ConcurrentHashMap<>();

        static {
            loadIanaZones();
        }

        /**
         * IANA zone logic
         */
        private static void loadIanaZones() {
            final String propertyFileName = "/data/zone1970.tab";
            try (InputStream inputStream = ZoneDataService.class.getResourceAsStream("/data/zone1970.tab");
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bReader = new BufferedReader(inputStreamReader)) {
                bReader.lines()
                    .filter(line -> !line.startsWith("#") && !line.isBlank())
                    .forEach(line -> {
                        final String[] parts = line.split("\t");
                        if (parts.length >= LINE_W_COORDINATE) {
                            processLine(parts[0], parts[1], parts[2]);
                        }
                    });
            } catch (IOException ei) {
                final Path ptPrjProps = Path.of(propertyFileName);
                final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, ptPrjProps.getParent(), ptPrjProps.getFileName());
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Coordinates parser
         * @param countries countries list as single string separated by comma
         * @param coords coordinates raw
         * @param zoneId IANA zone identifier
         */
        private static void processLine(final String countries, final String coords, final String zoneId) {
            // Parse Countries (Java 25 Locale.of)
            final List<String> codes = Arrays.asList(countries.split(","));
            final List<String> names = codes.stream()
                    .map(code -> Locale.of("", code).getDisplayCountry(Locale.ENGLISH))
                    .toList();
            // 2. Parse Coordinates (ISO 6709)
            int splitIdx = coords.indexOf('-', 1);
            if (splitIdx == -1) {
                splitIdx = coords.indexOf('+', 1);
            }
            final double lat = dmsToDecimal(coords.substring(0, splitIdx), false);
            final double lon = dmsToDecimal(coords.substring(splitIdx), true);
            // 3. Get Current Offset String
            final String offsetStr = "UTC" + ZonedDateTime.now(ZoneId.of(zoneId)).getOffset().getId();
            CACHE.put(zoneId, new ZoneInfo(zoneId, lat, lon, codes, names, offsetStr));
        }

        /**
         * Converts Degrees, Minutes, Seconds to Decimal logic
         * @param part degrees+minutes+seconds in
         * @param isLon is Longitude
         * @return double numeric value
         */
        private static double dmsToDecimal(final String part, final boolean isLon) {
            final Matcher matched = (isLon ? LON_REGEX : LAT_REGEX).matcher(part);
            double decToReturn = 0.0;
            if (matched.matches()) {
                final double sign = "-".equals(matched.group(1)) ? -1.0 : 1.0;
                final double deg = Double.parseDouble(matched.group(2));
                final double min = Double.parseDouble(matched.group(3));
                final double sec = (matched.group(4) != null) ? Double.parseDouble(matched.group(4)) : 0.0;
                decToReturn = sign * (deg + (min / 60.0) + (sec / 3600.0));
            }
            return decToReturn;
        }

        /**
         * Getter for ZoneInfo
         * @param zoneId string with IANA location
         * @return ZoneInfo
         */
        public static ZoneInfo get(final String zoneId) {
            return CACHE.get(zoneId);
        }

        /**
         * Getter for ZoneInfo
         * @return Collection of ZoneInfo
         */
        public static Collection<ZoneInfo> getAll() {
            return CACHE.values();
        }

        /**
         * Constructor
         */
        private ZoneDataService() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private TimingClass() {
        // intentionally blank
    }
}

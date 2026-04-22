package javajava;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regular Expressions things
 */
public final class RegularExpressionsClass {
    /**
     * Patterns Map
     */
    public static final Map<String, Map<String, String>> mapPatterns = Map.of(
            RegularExpressionsClass.STR_AGING_DATE, Map.of(RegularExpressionsClass.STR_REG_EXP, "[+-](?<years>\\d{4})-(?<months>(0\\d{1}|1[0-1]{1}))-(?<days>([0-2]{1}\\d{1}|30))"),
            RegularExpressionsClass.STR_AGING_TS, Map.of(RegularExpressionsClass.STR_REG_EXP, "[+-](?<yearsTS>\\d{4})-(?<monthsTS>(0\\d{1}|1[0-1]{1}))-(?<daysTS>([0-2]{1}\\d{1}|30))\\s(?<hoursTS>([0-1]\\d{1}|2[0-3]{1}))\\:(?<minutesTS>[0-5]{1}\\d{1})\\:(?<secondsTS>[0-5]{1}\\d{1})"),
            RegularExpressionsClass.STR_AGING_TIME, Map.of(RegularExpressionsClass.STR_REG_EXP, "(?<hours>([0-1]\\d{1}|2[0-3]{1}))\\:(?<minutes>[0-5]{1}\\d{1})\\:(?<seconds>[0-5]{1}\\d{1})"),
            "decimal", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d+\\.\\d+-?"),
            "long", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d{1,18}-?"),
            BasicStructuresClass.STR_JUST_DATE, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-[0-1-2]{1}\\d{1})"),
            RegularExpressionsClass.STR_MAVEN_PKG, Map.of(RegularExpressionsClass.STR_REG_EXP, "[0-9a-z]+\\.[0-9a-z\\-\\.]+\\:[0-9a-z\\-\\.]+",
                    "URL", "<a href=\"https://central.sonatype.com/artifact/%s/\" target=\"_blank\">%s</a>"),
            "numeric", Map.of(RegularExpressionsClass.STR_REG_EXP, "-?\\d+(\\.\\d+)?-?"),
            BasicStructuresClass.STR_TIMESTAMP, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd HH:mm:ss",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy HH:mm:ss",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy HH:mm:ss",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-[0-1-2]{1}[0-9]{1})\\s([0-1]\\d{1}|2[0-3]{1})\\:[0-5]{1}\\d{1}\\:[0-5]{1}\\d{1}"),
            BasicStructuresClass.STR_TS_MSEC, Map.of(BasicStructuresClass.STR_INPUT, "yyyy-MM-dd HH:mm:ss.SSS",
                    BasicStructuresClass.STR_OUTPUT_LONG, "EEEE, dd MMMM yyyy HH:mm:ss.SSS",
                    BasicStructuresClass.STR_OUTPUT_SHORT, "EEE, dd MMM yyyy HH:mm:ss.SSS",
                    RegularExpressionsClass.STR_REG_EXP, "(1|2)\\d{3}\\-((01|03|05|07|08|10|12)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|3[0-1]{1})|(04|06|09|11)\\-(0{1}[1-9]{1}|[1-2]{1}\\d{1}|30)|02\\-[0-1-2]{1}[0-9]{1})\\s([0-1]\\d{1}|2[0-3]{1})\\:[0-5]{1}\\d{1}\\:[0-5]{1}\\d{1}\\.\\d{3}")
            );
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
     * Regular Expression string
     */
    public static final String STR_REG_EXP = "Regular Expression";
    /**
     * string constant
     */
    public static final String STR_AGING_DATE = "agingDate";
    /**
     * string constant
     */
    public static final String STR_AGING_TIME = "agingTime";
    /**
     * string constant
     */
    public static final String STR_AGING_TS = "agingTimestamp";
    /**
     * string constant
     */
    public static final String STR_MAVEN_PKG = "MavenPackage";

    /**
     * Convert aging Date into human readable String
     * @param ageString input String
     * @return String in human readable format
     */
    public static String convertAgingDateIntoHumanReadableString(final String ageString) {
        final Pattern agePattern = Pattern.compile(mapPatterns.get(STR_AGING_DATE).get(STR_REG_EXP));
        final Matcher matcher = agePattern.matcher(ageString);
        final StringJoiner result = new StringJoiner(", ");
        if (matcher.matches()) {
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("years")), "year", "years"));
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("months")), "month", "months"));
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("days")), "day", "days"));
        } else {
            result.add(ageString);
        }
        return result.toString().replaceAll("^[,\\s]+", "");
    }

    /**
     * Convert aging Time into human readable String
     * @param ageString input String
     * @return String in human readable format
     */
    public static String convertAgingTimeIntoHumanReadableString(final String ageString) {
        final Pattern agePattern = Pattern.compile(mapPatterns.get(STR_AGING_TIME).get(STR_REG_EXP));
        final Matcher matcher = agePattern.matcher(ageString);
        final StringJoiner result = new StringJoiner(", ");
        if (matcher.matches()) {
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("hours")), "hour", "hours"));
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("minutes")), "minute", "minutes"));
            result.add(numberWithSuffixIfNonZero(Integer.parseInt(matcher.group("seconds")), "second", "seconds"));
        } else {
            result.add(ageString);
        }
        return result.toString().replaceAll("^[,\\s]+", "");
    }

    /**
     * Count occurrences with String
     * @param hystack string to count in
     * @param needleType type of pattern search
     * @return number of occurrences
     */
    public static int countOccurrences(final String hystack, final String needleType) {
        final Pattern pattern = switch(needleType) {
            case "NamedParameters" -> Pattern.compile(BasicStructuresClass.STR_PRMTR_RGX);
            case "ComplexPositionalTypeParameters" -> Pattern.compile("%(|[1-9]\\$)(|,\\d{1,3}|\\+|\\(|,)(|\\.[1-9]|\\d{1,2})[abcdefghnostx]");
            case "PositionalTypeParameters" -> Pattern.compile("%[ACEGHSTXacdefghostx]");
            default -> Pattern.compile(".*");
        };
        final Matcher matcher = pattern.matcher(hystack);
        int count = 0;
        while (matcher.find()) {
            count = count + matcher.groupCount();
        }
        return count;
    }

    /**
     * check if pattern exists into text
     * @param text to search within
     * @param pattern to search for
     * @return int
     */
    public static int doesExist(final String text, final String pattern) {
        final Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = regex.matcher(text);
        return matcher.find() ? 1 : 0;
    }

    /**
     * Converts Degrees, Minutes, Seconds to Decimal logic
     * @param part degrees+minutes+seconds in
     * @param isLon is Longitude
     * @return double numeric value
     */
    public static double dmsToDecimal(final String part, final boolean isLon) {
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
     * Extracts all occurrences of a given regex pattern from a text.
     * @param text The input string to search within.
     * @param regex The regular expression pattern.
     * @return A List of strings, where each string is a full match found.
     */
    public static List<String> extractMatches(final String text, final String regex) {
        final List<String> matches = new ArrayList<>();
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(matcher.group()); // group() or group(0) returns the entire matched sequence
        }
        return matches;
    }

    /**
     * Helper to find which named group was actually hit by the regex
     * @param result match result group
     * @return name of the active group
     */
    public static String getActiveGroup(final MatchResult result) {
        final List<String> capturedGroups = List.of(
                STR_MAVEN_PKG,
                STR_AGING_TS,
                STR_AGING_DATE,
                BasicStructuresClass.STR_TS_MSEC,
                BasicStructuresClass.STR_TIMESTAMP,
                BasicStructuresClass.STR_JUST_DATE);
        return capturedGroups.stream()
                .filter(groupName -> result.group(groupName) != null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No group matched"));
    }

    /**
     * Number with Suffix If Non-Zero
     * @param inNumber number to evaluate
     * @param strSingular singular suffix
     * @param strPlural plural suffix
     * @return number with suffix or empty if number is zero
     */
    private static String numberWithSuffixIfNonZero(final int inNumber, final String strSingular, final String strPlural) {
        return switch(inNumber) {
            case 0  -> "";
            case 1  -> inNumber + " " + strSingular;
            default -> inNumber + " " +  strPlural;
        };
    }

    /**
     * Replace patterns within large Text
     * @param inString original text
     * @return replaced text
     */
    public static String replacePatternsWithTimeZones(final String inString, final String inputTimeZone, final String outputTimeZone) {
        final String strRegExp = "(?<MavenPackage>" + mapPatterns.get(STR_MAVEN_PKG).get(STR_REG_EXP) + ")"
                + "|" + "(?<agingTimestamp>" + mapPatterns.get(STR_AGING_TS).get(STR_REG_EXP) + ")"
                + "|" + "(?<agingDate>" + mapPatterns.get(STR_AGING_DATE).get(STR_REG_EXP) + ")"
                + "|" + "(?<timestampWithMilliseconds>" + mapPatterns.get(BasicStructuresClass.STR_TS_MSEC).get(STR_REG_EXP) + ")"
                + "|" + "(?<timestamp>" + mapPatterns.get(BasicStructuresClass.STR_TIMESTAMP).get(STR_REG_EXP) + ")"
                + "|" + "(?<justDate>" + mapPatterns.get(BasicStructuresClass.STR_JUST_DATE).get(STR_REG_EXP) + ")";
        final Pattern pattern = Pattern.compile(strRegExp);
        final Matcher matcher = pattern.matcher(inString);
        return matcher.replaceAll(matchResult -> {
            try {
                // Determine which group matched
                final String matchedGroup = getActiveGroup(matchResult);
                final String text = matchResult.group(matchedGroup);
                switch (matchedGroup) {
                    case STR_MAVEN_PKG -> {
                        if (text.contains("compliance-snowflake")
                                || text.contains("danielgp-eu")) {
                            return text;
                        } else {
                            return String.format(mapPatterns.get(STR_MAVEN_PKG).get("URL"), text.replace(':', '/'), text);
                        }
                    }
                    case STR_AGING_TS -> {
                        final String strDate = text.substring(0, 11);
                        final String strTime = text.substring(12, 20);
                        return convertAgingDateIntoHumanReadableString(strDate)
                                + "<br/>" + convertAgingTimeIntoHumanReadableString(strTime);
                    }
                    case STR_AGING_DATE -> {
                        final String outString = convertAgingDateIntoHumanReadableString(text);
                        return outString.isEmpty() ? "TODAY" : outString;
                    }
                    case null, default -> {
                        final DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern(mapPatterns.get(matchedGroup).get(BasicStructuresClass.STR_INPUT));
                        // Convert based on the specific group rules
                        final ZonedDateTime sourceTime = BasicStructuresClass.STR_JUST_DATE.equals(matchedGroup) ?
                                LocalDate.parse(text, inputFormat).atStartOfDay(ZoneId.of(inputTimeZone))
                                : LocalDateTime.parse(text, inputFormat).atZone(ZoneId.of(inputTimeZone));
                        final DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern(mapPatterns.get(matchedGroup).get(BasicStructuresClass.STR_OUTPUT_SHORT));
                        final ZonedDateTime targetTime = sourceTime.withZoneSameInstant(ZoneId.of(outputTimeZone));
                        return targetTime.format(outputFormat);
                    }
                }
            } catch (IllegalStateException _) {
                return matchResult.group(); // Fallback if parsing fails
            }
        });
    }

    /**
     * Validation logic using Regular Expressions
     */
    public final class ValidationClass {

        /**
         * Check if String is actually Date
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Date
         */
        public static boolean isStringActuallySomething(final String inputString, final String mapIdentifier) {
            boolean bolReturn = false;
            if (inputString != null) {
                final Pattern pattern = Pattern.compile(mapPatterns.get(mapIdentifier).get(STR_REG_EXP));
                bolReturn = pattern.matcher(inputString).matches();
            }
            return bolReturn;
        }

        /**
         * Validation file name
         * @param given file name
         * @return true if file name is valid, false otherwise
         */
        public static boolean isFileNameValid(final String value) {
            boolean validFileName = true;
            if (value == null || value.isBlank()) {
                final String strFeedback = "File name must not be null or blank";
                LogExposureClass.LOGGER.error(strFeedback);
                validFileName = false;
            } else if(!value.matches("^[a-zA-Z0-9](?:[a-zA-Z0-9 ._-]*[a-zA-Z0-9])?\\.[a-zA-Z0-9_-]+$")) {
                final String strFeedback = "File name contains invalid characters";
                LogExposureClass.LOGGER.error(strFeedback);
                validFileName = false;
            }
            return validFileName;
        }

        // Private constructor to prevent instantiation
        private ValidationClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private RegularExpressionsClass() {
        // intentionally blank
    }

}

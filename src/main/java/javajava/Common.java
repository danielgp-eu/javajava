package javajava;

import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with common features
 */
public final class Common {
    /**
     * Database MySQL
     */
    public static final String STR_DB_MYSQL = "MySQL";
    /**
     * Database Snowflake
     */
    public static final String STR_DB_SNOWFLAKE = "Snowflake";
    /**
     * Database SQLite
     */
    public static final String STR_DB_SQLITE = "SQLite";
    /**
     * standard String
     */
    public static final String STR_NAME = "Name";
    /**
     * NULL string
     */
    public static final String STR_NULL = "NULL";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z\\s_\\-]{2,50}\\}";
    /**
     * standard String
     */
    public static final String STR_ROLES = "Roles";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_QTD_STR_VL = "\"%s\"";
    /**
     * standard Application class feedback
     */
    public static final String STR_I18N_AP_CL_WN = JavaJavaLocalization.getMessage("i18nAppClassWarning");
    /**
     * standard SQL statement unable
     */
    public static final String STR_I18N_STM_UNB = JavaJavaLocalization.getMessage("i18nSQLstatementUnableToGetX");
    /**
     * standard Unknown feature
     */
    public static final String STR_I18N_UNKN_FTS = JavaJavaLocalization.getMessage("i18nUnknFtrs");
    /**
     * standard Unknown
     */
    public static final String STR_I18N_UNKN = JavaJavaLocalization.getMessage("i18nUnknown");

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoNamedParameters(final String strOriginalQ) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.TRACE)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = extractMatches(strOriginalQ, STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            final String newParameter = StringManipulationClass.getNamedParameterFromPromptOne(currentPrmtName);
            strFinalQ = strFinalQ.replace(currentPrmtName, newParameter);
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.TRACE)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nSQLqueryFinalIs", strFinalQ);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return strFinalQ;
    }

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoParameters(final String strOriginalQ) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryOriginalIs"), strOriginalQ);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = extractMatches(strOriginalQ, STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryFinalIs"), strFinalQ);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return strFinalQ;
    }

    /**
     * Extracts all occurrences of a given regex pattern from a text.
     * @param text The input string to search within.
     * @return A List of strings, where each string is a full match found.
     */
    public static int countParametersWithinQuery(final String text) {
        final Pattern pattern = Pattern.compile(STR_PRMTR_RGX);
        final Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Counts number of parameters with in a string
     * @param inputString string to evaluate
     * @return number of parameters within given string
     */
    public static int countParametersWithinString(final String inputString) {
        final Pattern pattern = Pattern.compile("%(|[1-9]\\$)(|,\\d{1,3}|\\+|\\(|,)(|\\.[1-9]|\\d{1,2})[abcdefghnostx]");
        final Matcher matcher = pattern.matcher(inputString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
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
     * JAVA info
     * 
     * @return String
     */
    public static String getDetailsAboutSoftwareUser() {
        return getMapIntoJsonString(Map.of(
            "Country", System.getProperty("user.country"),
            "Country.Format", System.getProperty("user.country.format"),
            "Language", System.getProperty("user.language"),
            "Language.Format", System.getProperty("user.language.format"),
            "Home", System.getProperty("user.home").replace("\\", "\\\\"),
            "Name", System.getProperty("user.name"),
            "Timezone", System.getProperty("user.timezone")
        ));
    }

    /**
     * JAVA info
     * 
     * @return String
     */
    public static String getDetailsAboutSoftwarePlatformJava() {
        return getMapIntoJsonString(Map.of(
            "Date", System.getProperty("java.version.date"),
            "Release", System.getProperty("java.vendor.version"),
            "Runtime", System.getProperty("java.runtime.name"),
            "Version", System.getProperty("java.version"),
            "Vendor", System.getProperty("java.vendor"),
            "VM", System.getProperty("java.vm.name")
        ));
    }

    /**
     * Build a pair of Key and Value for JSON
     * @param strKey Key to be used
     * @param objValue Value to be used
     * @return String with a pair of key and value
     */
    private static String getJsonKeyAndValue(final String strKey, final Object objValue) {
        final List<String> unquotedValues = Arrays.asList("null", "true", "false");
        final boolean needsQuotesAround = 
            (objValue instanceof Integer)
            || (objValue instanceof Double)
            || (objValue.toString().startsWith("[") && objValue.toString().endsWith("]"))
            || (objValue.toString().startsWith("{") && objValue.toString().endsWith("}"))
            || isStringActuallyNumeric(objValue.toString())
            || hasMatchingSubstring(objValue.toString(), unquotedValues);
        String strRaw = "\"%s\":\"%s\"";
        if (needsQuotesAround) {
            strRaw = "\"%s\":%s";
        }
        return String.format(strRaw, strKey, objValue);
    }

    /**
     * Cycle inside Map and build a JSON string out of it
     *
     * @param arrayAttrib array with attribute values
     * @return String
     */
    public static String getMapIntoJsonString(final Map<String, Object> arrayAttrib) {
        final StringBuilder strJsonSubString = new StringBuilder(100);
        arrayAttrib.forEach((strKey, objValue) -> {
            if (!strJsonSubString.isEmpty()) {
                strJsonSubString.append(',');
            }
            strJsonSubString.append(getJsonKeyAndValue(strKey, objValue));
        });
        return String.format("{%s}", strJsonSubString);
    }

    /**
     * Checks if given string is included in a given List of Strings
     * @param str String to search into
     * @param substrings Strings to search for
     * @return boolean true if found, false otherwise
     */
    private static boolean hasMatchingSubstring(final String str, final List<String> substrings) {
        return substrings.stream().anyMatch(str::contains);
    }

    /**
     * Sensors Information
     * @param intPhysMedType number for NDIS Physical Medium Type
     * @return String
     */
    public static String getNetworkPhysicalMediumType(final int intPhysMedType) {
        return getMapIntoJsonString(Map.of(
            "Numeric", intPhysMedType,
            STR_NAME, switch (intPhysMedType) {
                case 0 -> "Unspecified (e.g., satellite feed)";
                case 1 -> "Wireless LAN (802.11)";
                case 2 -> "Cable Modem (DOCSIS)";
                case 3 -> "Phone Line (HomePNA)";
                case 4 -> "Power Line (data over electrical wiring)";
                case 5 -> "DSL (ADSL, G.Lite)";
                case 6 -> "Fibre Channel (high-speed storage interconnect)";
                case 7 -> "IEEE 1394 (FireWire)";
                case 8 -> "Wireless WAN (CDMA, GPRS)";
                case 9 -> "Native 802.11 (modern Wi-Fi interface)";
                case 10 -> "Bluetooth (short-range wireless)";
                case 11 -> "InfiniBand (high-speed interconnect)";
                case 12 -> "Ultra Wideband (UWB)";
                case 13 -> "Ethernet (802.3)";
                default -> "Unknown";
            }
        ));
    }

    /**
     * handle NameUnformatted
     * @param intRsParams number for parameters
     * @param strUnformatted original string
     * @param strReplacement replacements (1 to multiple)
     * @return String
     */
    public static String handleNameUnformattedMessage(final int intRsParams, final String strUnformatted, final Object... strReplacement) {
        return switch (intRsParams) {
            case 1 -> String.format(strUnformatted, strReplacement[0].toString());
            case 2 -> String.format(strUnformatted, strReplacement[0].toString(), strReplacement[1].toString());
            case 3 -> String.format(strUnformatted, strReplacement[0].toString(), strReplacement[1].toString(), strReplacement[2].toString());
            default -> {
                final String strFeedback = String.format(STR_I18N_UNKN_FTS, intRsParams, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * Check if String is actually Numeric
     *
     * @param inputString string to evaluate
     * @return True if given String is actually Numeric
     */
    private static Boolean isStringActuallyNumeric(final String inputString) {
        boolean bolReturn = false;
        if (inputString != null) {
            final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            bolReturn = pattern.matcher(inputString).matches();
        }
        return bolReturn;
    }

    // Private constructor to prevent instantiation
    private Common() {
        throw new UnsupportedOperationException(STR_I18N_AP_CL_WN);
    }
}

package javajava;

import org.apache.logging.log4j.Level;

import java.net.URL;
import java.util.*;
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
     * detects if current execution is from JAR or not
     * @return boolean
     */
    public static boolean isRunningFromJar() {
        // Get the URL of the current class's bytecode
        final URL classUrl = Common.class.getResource("Common.class");
        if (classUrl == null) {
            throw new IllegalStateException("Class resource not found");
        }
        // Check if the protocol is "jar" (JAR execution) or "file" (IDE execution)
        final String protocol = classUrl.getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * Execution Interrupted details captured to Error log
     * @param strError details
     */
    public static void setInputOutputExecutionLoggedToError(final String strError) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
            LoggerLevelProvider.LOGGER.error(strError);
        }
    }

    // Private constructor to prevent instantiation
    private Common() {
        // intentionally left blank
    }
}

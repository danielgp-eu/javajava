package javajava;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;

import localization.JavaJavaLocalizationClass;

/**
 * String Manipulation
 */
public final class StringManipulationClass {

    /**
     * Clean String From CurlyBraces
     * @param strOriginal Original string
     * @return String
     */
    public static String cleanStringFromCurlyBraces(final String strOriginal) {
        final StringBuilder strBuilder = new StringBuilder();
        for (final char c : strOriginal.toCharArray()) {
            if (c != '{' && c != '}') {
                strBuilder.append(c);
            }
        }
        return strBuilder.toString();
    }

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoNamedParameters(final String strOriginalQ) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.TRACE)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, CommonClass.STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            final String newParameter = getNamedParameterFromPromptOne(currentPrmtName);
            strFinalQ = strFinalQ.replace(currentPrmtName, newParameter);
        }
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.TRACE)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nSQLqueryFinalIs", strFinalQ);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        return strFinalQ;
    }

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoParameters(final String strOriginalQ) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryOriginalIs"), strOriginalQ);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, CommonClass.STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
        }
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryFinalIs"), strFinalQ);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        return strFinalQ;
    }

    /**
     * Convert String to BigDecimal
     * @param strNumber
     * @return
     */
    public static BigDecimal convertStringIntoBigDecimal(final String strNumber) {
        BigDecimal noToReturn = null;
        if (isStringActuallyNumeric(strNumber)) {
            noToReturn = new BigDecimal(strNumber).stripTrailingZeros();
        }
        return noToReturn;
    }

    /**
     * Extracts all occurrences of a given regex pattern from a text.
     * @param text The input string to search within.
     * @return A List of strings, where each string is a full match found.
     */
    public static int countParametersWithinQuery(final String text) {
        final Pattern pattern = Pattern.compile(CommonClass.STR_PRMTR_RGX);
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
     * get Named Parameter From Prompt One
     * @param strOriginal Original string
     * @return String
     */
    public static String getNamedParameterFromPromptOne(final String strOriginal) {
        return ":" + cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
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
                final String strFeedback = String.format(CommonClass.STR_I18N_UNKN_FTS, intRsParams, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(CommonClass.STR_I18N_UNKN)));
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * Checks if given string is included in a given List of Strings
     * @param str String to search into
     * @param substrings Strings to search for
     * @return boolean true if found, false otherwise
     */
    public static boolean hasMatchingSubstring(final String str, final List<String> substrings) {
        return substrings.stream().anyMatch(str::contains);
    }

    /**
     * Check if String is actually Numeric
     *
     * @param inputString string to evaluate
     * @return True if given String is actually Numeric
     */
    public static Boolean isStringActuallyNumeric(final String inputString) {
        boolean bolReturn = false;
        if (inputString != null) {
            final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            bolReturn = pattern.matcher(inputString).matches();
        }
        return bolReturn;
    }

    /**
     * Helper to remove surrounding double quotes safely
     * @param strInput initial String
     * @return String without double quotes enclosing
     */
    public static String stripQuotes(final String strInput) {
        return (strInput != null && strInput.length() >= 2 && strInput.startsWith("\"") && strInput.endsWith("\""))
                ? strInput.substring(1, strInput.length() - 1)
                : strInput;
    }

    // Private constructor to prevent instantiation
    private StringManipulationClass() {
        throw new UnsupportedOperationException(CommonClass.STR_I18N_AP_CL_WN);
    }
}

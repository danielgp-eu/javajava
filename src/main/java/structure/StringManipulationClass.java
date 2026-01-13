package structure;

import java.util.List;
import java.util.regex.Pattern;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * String Manipulation
 */
public final class StringManipulationClass {
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z\\s_\\-]{2,50}\\}";

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
        final String strFeedbackStrt = JavaJavaLocalizationClass.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
        LogExposureClass.LOGGER.debug(strFeedbackStrt);
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            strFinalQ = strFinalQ.replace(currentPrmtName, getNamedParameterFromPromptOne(currentPrmtName));
        }
        final String strFeedbackEnd = JavaJavaLocalizationClass.getMessage("i18nSQLqueryFinalIs", strFinalQ);
        LogExposureClass.LOGGER.debug(strFeedbackEnd);
        return strFinalQ;
    }

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoParameters(final String strOriginalQ) {
        final String strFeedbackStrt = JavaJavaLocalizationClass.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
        LogExposureClass.LOGGER.debug(strFeedbackStrt);
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        for (final String currentPrmtName : listMatches) {
            strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
        }
        final String strFeedbackEnd = JavaJavaLocalizationClass.getMessage("i18nSQLqueryFinalIs", strFinalQ);
        LogExposureClass.LOGGER.debug(strFeedbackEnd);
        return strFinalQ;
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
                final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, intRsParams, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
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
        // intentionally blank
    }
}

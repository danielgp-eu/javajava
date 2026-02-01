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
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z_\\s\\-]{2,50}\\}";
    /**
     * Single Question Mark Character
     */
    private static final String Q_MARK_PARAM = "SingleQuestionMarkCharacterParameter";
    /**
     * Named Character
     */
    private static final String NAMED_PARAM = "NamedParameter";

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    private static String convertPromptParameters(final String strOriginalQ, final String type) {
        final String strFeedbackStrt = JavaJavaLocalizationClass.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
        LogExposureClass.LOGGER.debug(strFeedbackStrt);
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, STR_PRMTR_RGX);
        String strFinalQ = strOriginalQ;
        if (Q_MARK_PARAM.equalsIgnoreCase(type)) {
            for (final String currentPrmtName : listMatches) {
                strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
            }
        } else if (NAMED_PARAM.equalsIgnoreCase(type)) {
            for (final String currentPrmtName : listMatches) {
                strFinalQ = strFinalQ.replace(currentPrmtName, getNamedParameterFromPromptOne(currentPrmtName));
            }
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
    public static String convertPromptParametersIntoNamedParameters(final String strOriginalQ) {
        return convertPromptParameters(strOriginalQ, NAMED_PARAM);
    }

    /**
     * Convert Prompt Parameters into Named Parameters
     * @param strOriginalQ query with prompt parameter
     * @return query with named parameters
     */
    public static String convertPromptParametersIntoParameters(final String strOriginalQ) {
        return convertPromptParameters(strOriginalQ, Q_MARK_PARAM);
    }

    /**
     * get Named Parameter From Prompt One
     * @param inString Original string
     * @return String
     */
    public static String encloseStringWithCharacter(final String inString) {
        final StringBuilder strBuilder = new StringBuilder();
        if (inString.matches("^\".*\"$")) {
            strBuilder.append(inString);
        } else if (inString.matches("^\".*[^\"]$")) {
            strBuilder.append(inString).append('\"');
        } else if (inString.matches("^[^\"].*\"$")) {
            strBuilder.append('\"').append(inString);
        } else {
            strBuilder.append('\"').append(inString).append('\"');
        }
        return strBuilder.toString();
    }

    /**
     * get Named Parameter From Prompt One
     * @param inString Original string
     * @return String
     */
    public static String encloseStringIfContainsSpace(final String inString) {
        String strReturn = inString;
        if (inString.contains(" ")) {
            strReturn = encloseStringWithCharacter(inString);
        }
        return strReturn;
    }

    /**
     * get Named Parameter From Prompt One
     * @param strOriginal Original string
     * @return String
     */
    public static String getNamedParameterFromPromptOne(final String strOriginal) {
        return ":" + CleaningClass.cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
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
            case 1 -> String.format(strUnformatted, strReplacement[0]);
            case 2 -> String.format(strUnformatted, strReplacement[0], strReplacement[1]);
            case 3 -> String.format(strUnformatted, strReplacement[0], strReplacement[1], strReplacement[2]);
            default -> String.format(LogExposureClass.STR_I18N_UNKN_FTS, intRsParams, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
        };
    }

    /**
     * Cleaning String
     */
    public final class CleaningClass {

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
        private CleaningClass() {
            // intentionally blank
        }

    }

    /**
     * Cleaning String
     */
    public final class EvaluatingClass {

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
         * @return True if given String is actually Integer
         */
        public static boolean isStringActuallyInteger(final String inputString) {
            boolean bolReturn = false;
            if (inputString != null) {
                final Pattern pattern = Pattern.compile("-?\\d{1,10}-?");
                bolReturn = pattern.matcher(inputString).matches();
            }
            return bolReturn;
        }

        /**
         * Check if String is actually Numeric
         *
         * @param inputString string to evaluate
         * @return True if given String is actually Numeric
         */
        public static boolean isStringActuallyNumeric(final String inputString) {
            boolean bolReturn = false;
            if (inputString != null) {
                final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?-?");
                bolReturn = pattern.matcher(inputString).matches();
            }
            return bolReturn;
        }

        // Private constructor to prevent instantiation
        private EvaluatingClass() {
            // intentionally blank
        }

    }

    // Private constructor to prevent instantiation
    private StringManipulationClass() {
        // intentionally blank
    }
}

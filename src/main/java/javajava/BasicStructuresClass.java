package javajava;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handling basic structures: numbers, lists, maps, strings
 */
public final class BasicStructuresClass {
    /**
     * string constant
     */
    public static final String STR_ACTV_PXLS = "Active Pixels";
    /**
     * String for internal ETL
     */
    public static final String STR_DOT_THREE = "DotAndThreeDigitNumber";
    /**
     * Manufacturer string
     */
    public static final String STR_MANUFACTURER = "Manufacturer";
    /**
     * Model string constant
     */
    public static final String STR_MODEL = "Model";
    /**
     * string constant
     */
    public static final String STR_MONITOR_NAME = "Monitor Name";
    /**
     * standard String
     */
    public static final String STR_NAME = "Name";
    /**
     * Named Character
     */
    public static final String STR_NAMED_PARAM = "NamedParameter";
    /**
     * string constant
     */
    public static final String STR_PHYSC_DIM = "Physical Dimensions";
    /**
     * string constant
     */
    public static final String STR_PRFRD_TM_CLCK = "Preferred Timing Clock";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z_\\s\\-]{2,50}\\}";
    /**
     * string constant
     */
    public static final String STR_RANGE_LMTS = "Range Limits";
    /**
     * String for Second
     */
    public static final String STR_SECOND = "Second";
    /**
     * String for internal ETL
     */
    public static final String STR_SLMN_TWO = "SemicolumnAndTwoDigitNumber";
    /**
     * string constant
     */
    public static final String STR_SRL_NUM = "Serial Number";
    /**
     * 
     */
    public static final String STR_TM_FRM_SP = "SpaceTwoDigitNumberAndSpaceAndSuffixOnlyIfGreaterThanZero";
    /**
     * String for internal ETL
     */
    public static final String STR_TWO = "TwoDigitNumber";
    /**
     * String for internal ETL
     */
    public static final String STR_TWO_NON_ZERO = "TwoDigitNumberOnlyIfGreaterThanZero";
    /**
     * Version string
     */
    public static final String STR_VERSION = "Version";

    /**
     * Safely computes percentage
     * @param numerator top number
     * @param denominator dividing number
     * @return float value
     */
    public static float computePercentageSafely(final long numerator, final long denominator) {
        long denominatorUsed = denominator;
        if (denominator == 0) {
            denominatorUsed = 100;
            final String strFeedback = String.format("Denominator is 0 hence Percentage calculation with Numerator %s is not possible and will return same numerator...", numerator);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        final double percentageExact = (float) numerator / denominatorUsed * 100;
        return (float) new BigDecimal(Double.toString(percentageExact))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Convert String to BigDecimal
     * @param strNumber string to evaluate
     * @return BigDecimal
     */
    public static BigDecimal convertStringIntoBigDecimal(final String strNumber) {
        BigDecimal noToReturn = null;
        final boolean isNumeric = StringEvaluationClass.isStringActuallyNumeric(strNumber);
        if (isNumeric) {
            noToReturn = new BigDecimal(strNumber).stripTrailingZeros();
        }
        return noToReturn;
    }

    /**
     * Convert String to Integer
     * @param strNumber string to evaluate
     * @return integer
     */
    public static int convertStringIntoInteger(final String strNumber) {
        int noToReturn = 0;
        final boolean isNumeric = StringEvaluationClass.isStringActuallyInteger(strNumber);
        if (isNumeric) {
            noToReturn = Integer.parseInt(strNumber);
        }
        return noToReturn;
    }

    /**
     * Extracts all occurrences of a given reg-ex pattern from a text.
     * @param inputString The input string to search within.
     * @return A List of strings, where each string is a full match found.
     */
    public static int countNamedParametersWithinQuery(final String inputString) {
        return countOccurrences(inputString, "NamedParameters");
    }

    /**
     * Count occurrences with String
     * @param hystack string to count in
     * @param needleType type of pattern search
     * @return number of occurrences
     */
    private static int countOccurrences(final String hystack, final String needleType) {
        final Pattern pattern = switch(needleType) {
            case "NamedParameters" -> Pattern.compile(STR_PRMTR_RGX);
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
     * Counts number of parameters with in a string
     * @param inputString string to evaluate
     * @return number of parameters within given string
     */
    public static int countPositionalTypeParametersWithinQuery(final String inputString) {
        return countOccurrences(inputString, "PositionalTypeParameters");
    }

    /**
     * List and Maps management
     */
    public static final class ListAndMapClass {

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
         * Get all words from a list of Strings with merged words glued by _
         * @param valList List of String with words glues by regexSep
         * @param regexSep separators for words detection
         * @return LinkedHashMap of Strings with counted occurrences
         */
        public static Map<String, Long> getWordCounts(final List<String> valList, final String regexSep) {
            final Map<String, Long> wordCounts = valList.stream()
                    .flatMap(s -> Arrays.stream(s.split(regexSep)))
                    .collect(Collectors.groupingBy(
                            word -> word,
                            Collectors.counting()
                    ));
            return sortMapByValueReversedAndKey(wordCounts);
        }

        /**
         * Merging keys based on list of rules
         * @param inputMap original map
         * @param mergeRules merging rules
         * @return Map of string list
         */
        public static Map<String, List<String>> mergeKeys(
                final Map<String, List<String>> inputMap,
                final Map<List<String>, String> mergeRules) {
            final Map<String, List<String>> result = new ConcurrentHashMap<>();
            // Keep track of all keys that will be merged (to exclude later)
            final Set<String> mergedKeys = new HashSet<>();
            final List<String> mergedValues = new ArrayList<>();
            // Apply each merge rule
            for (final Map.Entry<List<String>, String> entry : mergeRules.entrySet()) {
                final List<String> keysToMerge = entry.getKey();
                for (final String key : keysToMerge) {
                    mergedValues.addAll(inputMap.getOrDefault(key, List.of()));
                }
                if (!mergedValues.isEmpty()) {
                    result.put(entry.getValue(), mergedValues);
                }
                mergedKeys.addAll(keysToMerge);
                mergedValues.clear();
            }
            // Copy all keys that were not merged
            for (final Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
                if (!mergedKeys.contains(entry.getKey())) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }

        /**
         * Sort Map by Key
         * @param origMap original Map
         * @return Map of String and Object
         */
        public static Map<String, Object> sortMapByKey(final Map<String, Object> origMap) {
            return origMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, _) -> oldValue,
                            LinkedHashMap::new // preserve sorted order
                    ));
        }

        /**
         * Sort Map by Value reversed then by Key
         * @param origMap original Map
         * @return Map of String and Long
         */
        private static Map<String, Long> sortMapByValueReversedAndKey(final Map<String, Long> origMap) {
            return origMap.entrySet().stream()
                    .sorted(
                            Comparator.comparing(Map.Entry<String, Long>::getValue).reversed()
                                    .thenComparing(Map.Entry::getKey)
                    )
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, _) -> e1, // merge function (not used here)
                            LinkedHashMap::new // preserve sorted order
                    ));
        }

        // Private constructor to prevent instantiation
        private ListAndMapClass() {
            // intentional empty
        }

    }

    /**
     * Project Properties Reader
     */
    public static final class PropertiesReaderClass {

        /**
         * get variable
         * @param strVariables variables to pick
         * @return Properties
         */
        public static Properties getVariableFromProjectProperties(final String propertyFileName, final String... strVariables) {
            final Properties svProperties = new Properties();
            try(InputStream inputStream = PropertiesReaderClass.class.getResourceAsStream(propertyFileName)) {
                final Properties inProperties = new Properties();
                inProperties.load(inputStream);
                final List<String> arrayVariables = Arrays.asList(strVariables);
                arrayVariables.forEach(crtVariable -> svProperties.put(crtVariable, inProperties.getProperty(crtVariable)));
                if (!propertyFileName.startsWith("/META-INF/maven/")) {
                    final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), svProperties);
                    LogExposureClass.LOGGER.debug(strFeedback);
                }
            } catch (IOException ei) {
                final Path ptPrjProps = Path.of(propertyFileName);
                final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, ptPrjProps.getParent(), ptPrjProps.getFileName());
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
            return svProperties;
        }

    }

    /**
     * Cleaning things
     */
    public static final class StringCleaningClass {

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
        private StringCleaningClass() {
            // intentionally blank
        }

    }

    /**
     * Evaluating things
     */
    public static final class StringEvaluationClass {

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
        private StringEvaluationClass() {
            // intentionally blank
        }

    }

    /**
     * Transforming things
     */
    public static final class StringTransformationClass {
        /**
         * Single Question Mark Character
         */
        private static final String Q_MARK_PARAM = "SingleQuestionMarkCharacterParameter";

        /**
         * Convert Prompt Parameters into Named Parameters
         * @param strOriginalQ query with prompt parameter
         * @return query with named parameters
         */
        private static String convertPromptParameters(final String strOriginalQ, final String type) {
            final String strFeedbackStrt = LocalizationClass.getMessage("i18nSQLqueryOriginalIs", strOriginalQ);
            LogExposureClass.LOGGER.debug(strFeedbackStrt);
            final List<String> listMatches = BasicStructuresClass.ListAndMapClass.extractMatches(strOriginalQ, STR_PRMTR_RGX);
            String strFinalQ = strOriginalQ;
            if (Q_MARK_PARAM.equalsIgnoreCase(type)) {
                for (final String currentPrmtName : listMatches) {
                    strFinalQ = strFinalQ.replace(currentPrmtName, Character.toString(63));
                }
            } else if (STR_NAMED_PARAM.equalsIgnoreCase(type)) {
                for (final String currentPrmtName : listMatches) {
                    strFinalQ = strFinalQ.replace(currentPrmtName, convertSinglePromptParameterIntoNamedParameter(currentPrmtName));
                }
            }
            final String strFeedbackEnd = LocalizationClass.getMessage("i18nSQLqueryFinalIs", strFinalQ);
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
            return strFinalQ;
        }

        /**
         * Convert Prompt Parameters into Named Parameters
         * @param strOriginalQ query with prompt parameter
         * @return query with named parameters
         */
        public static String convertPromptParametersIntoNamedParameters(final String strOriginalQ) {
            return convertPromptParameters(strOriginalQ, STR_NAMED_PARAM);
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
         * @param strOriginal Original string
         * @return String
         */
        private static String convertSinglePromptParameterIntoNamedParameter(final String strOriginal) {
            return ":" + StringCleaningClass.cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
        }

        /**
         * get Named Parameter From Prompt One
         * @param inString Original string
         * @return String
         */
        private static String encloseStringWithCharacter(final String inString, final char inChar) {
            final StringBuilder strBuilder = new StringBuilder();
            if (inString.matches(String.format("^%s.*%s$", inChar, inChar))) { // is already enclosed
                strBuilder.append(inString);
            } else if (inString.matches(String.format("^%s.*[^%s]$", inChar, inChar))) { // has only start enclosed
                strBuilder.append(inString).append('\"');
            } else if (inString.matches(String.format("^[^%s].*%s$", inChar, inChar))) { // has only end enclosed
                strBuilder.append('\"').append(inString);
            } else { // does not have neither start nor end enclosed
                strBuilder.append('\"').append(inString).append('\"');
            }
            return strBuilder.toString();
        }

        /**
         * get Named Parameter From Prompt One
         * @param inString Original string
         * @return String
         */
        public static String encloseStringIfContainsSpace(final String inString, final char inChar) {
            String strReturn = inString;
            if (inString.contains(" ")) {
                strReturn = encloseStringWithCharacter(inString, inChar);
            }
            return strReturn;
        }

        /**
         * Ensures no password exposure
         * @param inProps input Properties
         * @return Properties with certain things obfuscated
         */
        public static Properties obfuscateProperties(final Properties inProps) {
            final Properties outProps = new Properties();
            final String strKeyToObfuscate = "password";
            inProps.forEach((strKey, strValue) -> {
                if (strKey.equals(strKeyToObfuscate)) {
                    outProps.put(strKey, "*U*N*D*I*S*C*L*O*S*E*D*");
                } else {
                    outProps.put(strKey, strValue);
                }
            });
            return outProps;
        }

        // Private constructor to prevent instantiation
        private StringTransformationClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private BasicStructuresClass() {
        // intentionally blank
    }
}

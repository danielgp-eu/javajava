package javajava;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * get Named Parameter From Prompt One
     * @param strOriginal Original string
     * @return String
     */
    public static String getNamedParameterFromPromptOne(final String strOriginal) {
        return ":" + cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
    }

    /**
     * Get all words from a list of Strings with merged words glued by _
     * @param valList List of String with words glues by _
     * @return LinkedHashMap of Strings with counted occurrences
     */
    public static Map<String, Long> getWordCounts(final List<String> valList) {
        final Map<String, Long> wordCounts = valList.stream()
                .flatMap(s -> Arrays.stream(s.split("_")))
                .collect(Collectors.groupingBy(
                       word -> word,
                       Collectors.counting()
                ));
        // Sort by value, then key
        return wordCounts.entrySet().stream()
                .sorted(
                        Comparator.comparing(Map.Entry<String, Long>::getValue).reversed()
                                .thenComparing(Map.Entry::getKey)
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // merge function (not used here)
                        LinkedHashMap::new // preserve sorted order
                ));
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
        for (final var entry : mergeRules.entrySet()) {
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
        for (final var entry : inputMap.entrySet()) {
            if (!mergedKeys.contains(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
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
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

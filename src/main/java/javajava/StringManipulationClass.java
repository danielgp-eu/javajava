package javajava;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

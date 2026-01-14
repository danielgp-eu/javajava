package structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * List and Maps management
 */
public final class ListAndMapClass {

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

package javajava;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import json.JsoningClass;

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
            strJsonSubString.append(JsoningClass.getJsonKeyAndValue(strKey, objValue));
        });
        return String.format("{%s}", strJsonSubString);
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
        // Sort by value, then key
        return wordCounts.entrySet().stream()
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

    // Private constructor to prevent instantiation
    private ListAndMapClass() {
        // intentional empty
    }

}

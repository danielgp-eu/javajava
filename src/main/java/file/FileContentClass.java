package file;

import org.apache.logging.log4j.Level;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File content logic
 */
public final class FileContentClass {

    /**
     * Get file content into String
     * 
     * @param strFileName file name
     * @return String
     */
    public static String getFileContentIntoString(final String strFileName) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return strReturn;
    }

    /**
     * Build message for file operation error
     * @param strFileName file name
     * @param strStagTrace stag trace
     * @return message for file operation error
     */
    private static String getFileErrorMessage(final String strFileName, final String strStagTrace) {
        return String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, strStagTrace);
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName file name
     * @return input stream
     */
    public static InputStream getIncludedFileContentIntoInputStream(final String strFileName) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); // NOPMD by E303778 on 30.04.2025, 15:47
        final InputStream inputStream = classLoader.getResourceAsStream(strFileName); // NOPMD by E303778 on 30.04.2025, 15:47
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoStreamSuccess"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return inputStream;
    }

    /**
     * Getting list of values from a column grouped by another column
     * @param strFileName target file name to be written to
     * @param intColToEval number of column to evaluate (build values list from it)
     * @param intColToGrpBy number of column to group list of values by
     * @return Map with String and List or String
     */
    public static Map<String, List<String>> getListOfValuesFromColumnsGroupedByAnotherColumnValuesFromCsvFile(
            final String strFileName,
            final Integer intColToEval,
            final Integer intColToGrpBy) {
        Map<String, List<String>> grouped = null;
        try {
            // Group values by category
            grouped = Files.lines(Path.of(strFileName))
                    .skip(1)
                    .map(line -> line.split("\",\"")) // split by comma
                    .collect(Collectors.groupingBy(
                            cols -> cols[intColToGrpBy], // key = Category
                            Collectors.mapping(cols -> cols[intColToEval].replaceAll("\"", ""), Collectors.toList()) // values
                    ));
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = getFileErrorMessage(strFileName, Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return grouped;
    }

    /**
     * storing into a CSV file a LinkedHashMap
     * @param strFileName target file name to be written to
     * @param strPrefixValue prefix column value
     * @param strHeader header values
     * @param listHsMp LinkedHashMap
     */
    public static void writeLinkedHashMapToCsvFile(final String strFileName, final String strPrefixValue, final String strHeader, final Map<String, Long> listHsMp) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listHsMp.entrySet().stream()
                        .map(e -> strPrefixValue + "," + e.getKey() + "," + e.getValue())
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of(strHeader), // header
                        listHsMp.entrySet().stream()
                                .map(e -> strPrefixValue + "," + e.getKey() + "," + e.getValue())
                ).toList();
            }
            Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = getFileErrorMessage(strFileName, Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Write list of single values to File
     * 
     * @param listStrings List of Strings
     * @param strFileName file name to write to
     */
    public static void writeListToTextFile(final List<String> listStrings, final String strFileName) {
        FileHandlingClass.removeFileIfExists(strFileName);
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            listStrings.forEach(strLine -> {
                try {
                    bwr.write(strLine);
                    bwr.newLine();
                } catch (IOException er) {
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                        LoggerLevelProvider.LOGGER.error(strFeedback);
                    }
                }
            });
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingSuccess"), strFileName);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Write list of Properties to CSV File
     *
     * @param propertiesList list of Properties
     * @param strFileName target File
     * @param strClmnSeparator column separator character
     */
    public static void writePropertiesListToCSV(final List<Properties> propertiesList, final String strFileName, final String strClmnSeparator) {
        // Collect all unique keys
        final Set<String> allKeys = new LinkedHashSet<>();
        for (final Properties properties : propertiesList) {
            allKeys.addAll(properties.stringPropertyNames());
        }
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            // Write the header
            bwr.write(String.join(strClmnSeparator, allKeys));
            bwr.newLine();
            final List<String> row = new ArrayList<>();
            // Write each row
            for (final Properties properties : propertiesList) {
                row.clear();
                for (final String key : allKeys) {
                    row.add(properties.getProperty(key, "")); // Supply default value "" if key is absent
                }
                bwr.write(String.join(strClmnSeparator, row));
                bwr.newLine();
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * storing into a CSV file a LinkedHashMap
     * @param strFileName target file name to be written to
     * @param strPrefixValue prefix column value
     * @param strHeader header values
     * @param listStrings List of String
     */
    public static void writeStringListToCsvFile(final String strFileName, final String strPrefixValue, final String strHeader, final List<String> listStrings) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listStrings.stream()
                        .map(value -> strPrefixValue + "," + value)
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of(strHeader), // header
                        listStrings.stream()
                                .map(value -> strPrefixValue + "," + value)
                ).toList();
            }
            Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = getFileErrorMessage(strFileName, Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Constructor
     */
    private FileContentClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }

}

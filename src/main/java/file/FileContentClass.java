package file;

import javajava.CommonClass;
import javajava.LoggerLevelProviderClass;
import localization.JavaJavaLocalizationClass;
import org.apache.logging.log4j.Level;

import java.io.*;
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
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            CommonClass.setInputOutputExecutionLoggedToError(String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace())));
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
        return String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, strStagTrace);
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName file name
     * @return input stream
     */
    public static InputStream getIncludedFileContentIntoInputStream(final String strFileName) {
        InputStream inStream = null;
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        try(InputStream inputStream = FileContentClass.class.getResourceAsStream(strFileName)) {
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), strFileName);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
            inStream = inputStream;
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(String.format("IO exception on getting %s resource... %s", strFileName, Arrays.toString(ex.getStackTrace())));
        }
        return inStream;
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName file name
     * @return input stream
     */
    public static String getIncludedFileContentIntoString(final String strFileName) {
        String strContent = null;
        try (InputStream iStream = getIncludedFileContentIntoInputStream(strFileName);
                InputStreamReader inputStreamReader = new InputStreamReader(iStream);
                BufferedReader bReader = new BufferedReader(inputStreamReader)) {
            strContent = bReader.readAllAsString();
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(String.format(JavaJavaLocalizationClass.getMessage("i18nError"), Arrays.toString(ex.getStackTrace())));
        }
        return strContent;
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
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace())));
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
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace())));
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
                    if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
                        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                        LoggerLevelProviderClass.LOGGER.error(strFeedback);
                    }
                }
            });
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingSuccess"), strFileName);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace())));
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
            CommonClass.setInputOutputExecutionLoggedToError(getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace())));
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
        } catch (IOException ex) {
            CommonClass.setInputOutputExecutionLoggedToError(getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace())));
        }
    }

    /**
     * Constructor
     */
    private FileContentClass() {
        throw new UnsupportedOperationException(CommonClass.STR_I18N_AP_CL_WN);
    }

}

package file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * File writing methods
 */
public final class FileContentWriteClass {
    /**
     * Column Separator for CSV file writing methods
     */
    private static char chCsvColSeparator = ',';
    /**
     * Line Prefix for CSV content writing methods
     */
    private static String strCsvLinePrefix = "";

    /**
     * Setter for Column Separator for CSV file writing methods
     * @param inCsvColSeparator char
     */
    public static void setCsvColumnSeparator(final char inCsvColSeparator) {
        chCsvColSeparator = inCsvColSeparator;
    }

    /**
     * Setter for Line prefix for CSV file writing methods
     * @param inCsvLinePrefix String
     */
    public static void setCsvLinePrefix(final String inCsvLinePrefix) {
        strCsvLinePrefix = inCsvLinePrefix + chCsvColSeparator;
    }

    /**
     * storing into a CSV file a LinkedHashMap
     * @param strFileName target file name to be written to
     * @param strHeader header values
     * @param listHsMp LinkedHashMap
     */
    public static void writeLinkedHashMapToCsvFile(final String strFileName, final String strHeader, final Map<String, Long> listHsMp) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listHsMp.entrySet().stream()
                        .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of(strHeader), // header
                        listHsMp.entrySet().stream()
                                .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                ).toList();
            }
            Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Write list of single values to File
     * 
     * @param listStrings List of Strings
     * @param strFileName file name to write to
     */
    public static void writeListToTextFile(final String strFileName, final List<String> listStrings) {
        FileHandlingClass.removeFileIfExists(strFileName);
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            listStrings.forEach(strLine -> {
                try {
                    bwr.write(strLine);
                    bwr.newLine();
                } catch (IOException er) {
                    final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                    LogExposureClass.LOGGER.error(strFeedback);
                }
            });
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingSuccess"), strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Write list of Properties to CSV File
     *
     * @param strFileName target File
     * @param propertiesList list of Properties
     */
    public static void writePropertiesListToCsvFile(final String strFileName, final List<Properties> propertiesList) {
        // Collect all unique keys
        final Set<String> allKeys = new LinkedHashSet<>();
        for (final Properties properties : propertiesList) {
            allKeys.addAll(properties.stringPropertyNames());
        }
        final String strClmnSeparator = String.valueOf(chCsvColSeparator);
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
            final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * storing into a CSV file a LinkedHashMap
     * @param strFileName target file name to be written to
     * @param strHeader header values
     * @param listStrings List of String
     */
    public static void writeStringListToCsvFile(final String strFileName, final String strHeader, final List<String> listStrings) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listStrings.stream()
                        .map(value -> strCsvLinePrefix + value)
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of(strHeader), // header
                        listStrings.stream()
                                .map(value -> strCsvLinePrefix + value)
                ).toList();
            }
            Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private FileContentWriteClass() {
        // intentionally blank
    }

}

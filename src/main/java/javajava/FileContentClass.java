package javajava;

import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    public static void getFileContentAsSellingPointReceiptIntoCsvFile(final String inFileName, final String outFileName) {
        final List<String> lstOutput = new ArrayList<>(); // content will be here
        lstOutput.add("CIF;Value;Z;Receipt;ID;Date;Hour;SerialNumber;TD"); // adding the CSV Header to the list
        String strOutLine = null;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inFileName))) {
            String line;
            Integer intLineNo = 0;
            Integer intLineFeedbackLimit = 999999999;
            Boolean bolIsReceipt = false;
            while ((line = reader.readLine()) != null) {
                intLineNo++;
                LoggerLevelProvider.LOGGER.debug(intLineNo);
                if (line.endsWith("MASTER  TASTE     S.R.L.-D") || line.endsWith("MASTER  TASTE     S.R.L.")) {
                    strOutLine = "";
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug("Line start");
                    }
                }
                if (line.startsWith("             CIF:")) {
                    strOutLine = line.substring(18); // CIF
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                }
                if (line.startsWith("TOTAL LEI")) {
                    strOutLine = strOutLine + ";" + line.replaceAll("TOTAL LEI", "").trim(); // Value
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                }
                /*if (line.startsWith("                  #")) {
                    strOutLine = strOutLine +  ";" + line.substring(18); // Index
                }*/
                if (line.startsWith("Z:")) {
                    strOutLine = strOutLine + ";" + line.substring(2, 6); // Z
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                    strOutLine = strOutLine + ";" + line.substring(10, 14); // Receipt
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                }
                if (line.startsWith("ID BF:")) {
                    strOutLine = strOutLine + ";" + line.substring(6).replace("`", "").trim(); // ID
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                    bolIsReceipt = true;
                }
                if (line.startsWith("      DATA:")) {
                    strOutLine = strOutLine + ";" + TimingClass.convertTimeFormat(line.substring(12, 22)
                            , "dd-MM-yyyy", "yyyy-MM-dd"); // Date
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                    strOutLine = strOutLine + ";" + line.substring(28); // Hour
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                }
                if (line.startsWith("S/N:")) {
                    strOutLine = strOutLine + ";" + line.substring(4, 16); // SerialNumber
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO) && (intLineNo > intLineFeedbackLimit)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                    strOutLine = strOutLine + ";" + line.substring(34); // TD
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                        LoggerLevelProvider.LOGGER.debug(strOutLine);
                    }
                    if (bolIsReceipt) {
                        lstOutput.add(strOutLine); // end of Receipt so current CSV line can be safely added to the List
                        bolIsReceipt = false;
                    }
                }
            }
            writeListToTextFile(lstOutput, outFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build message for file operation error
     * @param strFileName
     * @param strStagTrace
     * @return
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
     * @param listHsMp LinkedHashMap
     */
    public static void storeIntoCsvFileLinkedHashMap(final String strFileName, final String strPrefixValue, final Map<String, Long> listHsMp) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listHsMp.entrySet().stream()
                        .map(e -> strPrefixValue + "," + e.getKey() + "," + e.getValue())
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of("DataType,Word,Occurrences"), // header
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
     * storing into a CSV file a LinkedHashMap
     * @param strFileName target file name to be written to
     * @param strPrefixValue prefix column value
     * @param listStrings List of String
     */
    public static void storeIntoCsvFileList(final String strFileName, final String strPrefixValue, final List<String> listStrings) {
        try {
            final List<String> strLines;
            final File strFile = new File(strFileName);
            if (strFile.exists()) {
                strLines = listStrings.stream()
                        .map(value -> strPrefixValue + "," + value)
                        .toList();
            } else {
                strLines = Stream.concat(
                        Stream.of("DataType,Column"), // header
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
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(ex.getStackTrace()));
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

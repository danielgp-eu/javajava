package javajava;

import org.apache.logging.log4j.Level;

import java.io.*;
import java.math.BigDecimal;
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

    public static void getFileContentAsSellingPointReceiptIntoCsvFile(final String inFileName, final String strCompanyName, final String outFileName) {
        final List<String> lstOutput = new ArrayList<>(); // content will be here
        final String[] arrayColumns = new String[] {"Company", "Address", "City", "County", "CIF",
                "PaidValue", "ValueCard", "ValueModernPayment", "ValueCash", "ValueRest", "ReceiptValue", "ReceiptVAT",
                "Z", "ReceiptNo", "ReceiptID",
                "ReceiptTimestamp", "Timestamp", "Year", "YearMonth", "ISO_YearWeek", "Date", "Hour",
                "SerialNumber", "ReceiptTD"};
        lstOutput.add(String.join(";", arrayColumns)); // adding the CSV Header to the list
        String strOutLine = null;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inFileName))) {
            String line;
            int intLineNo = 0;
            int intReceiptLineNo = 0;
            boolean bolIsReceipt = false;
            String strTotalValue = "";
            String strCardValue = "";
            String strModernPaymentValue = "";
            String strCashValue = "";
            String strRestValue = "";
            String strTrimmedLine;
            String strDate;
            BigDecimal decimalReceiptValue;
            while ((line = reader.readLine()) != null) {
                intLineNo++;
                intReceiptLineNo++;
                strTrimmedLine = line.trim();
                if (strTrimmedLine.replaceAll("  ", " ").startsWith(strCompanyName)) {
                    strOutLine = strTrimmedLine; // Company
                    intReceiptLineNo = 1;
                    bolIsReceipt = false;
                }
                if (List.of(2, 3, 4).contains(intReceiptLineNo) ) {
                    strOutLine = strOutLine + ";" + strTrimmedLine; // 2 = Address, 3 = City, 4 = County
                }
                if (strTrimmedLine.startsWith("CIF:")) {
                    strOutLine = strOutLine + ";" + line.replaceAll("CIF:", "").trim(); // CIF
                }
                if (line.startsWith("TOTAL LEI")) {
                    strTotalValue = line.replaceAll("TOTAL LEI", "").trim();
                    strOutLine = strOutLine + ";" + strTotalValue; // Value
                }
                if (line.startsWith("CARD")) {
                    strCardValue = line.replaceAll("CARD", "").trim(); // ValueCard
                }
                if (line.startsWith("PLATA MODERNA")) {
                    strModernPaymentValue = line.replaceAll("PLATA MODERNA", "").trim(); // ValueCard
                }
                if (line.startsWith("NUMERAR LEI")) {
                    strCashValue = line.replaceAll("NUMERAR LEI", "").trim(); // ValueCash
                }
                if (line.startsWith("REST")) {
                    strRestValue = line.replaceAll("REST", "").trim(); // ValueCard
                }
                if (line.startsWith("TOTAL TVA BON")) {
                    strOutLine = strOutLine + ";" + strCardValue; // ValueCard
                    strOutLine = strOutLine + ";" + strModernPaymentValue; // ValueModernPayment
                    strOutLine = strOutLine + ";" + strCashValue; // ValueCash
                    strOutLine = strOutLine + ";" + strRestValue; // ValueRest
                    decimalReceiptValue = (new BigDecimal(strTotalValue)).subtract(new BigDecimal(strRestValue));
                    strOutLine = strOutLine + ";" + decimalReceiptValue; // ReceiptValue
                    strOutLine = strOutLine + ";" + line.replaceAll("TOTAL TVA BON", "").trim(); // ReceiptVAT
                    strCardValue = "";
                    strModernPaymentValue = "";
                    strCashValue = "";
                    strRestValue = "";
                }
                if (line.startsWith("Z:")) {
                    strOutLine = strOutLine + ";" + line.substring(2, 6); // Z
                    strOutLine = strOutLine + ";" + line.substring(10, 14); // Receipt
                }
                if (line.startsWith("ID BF:")) {
                    strOutLine = strOutLine + ";" + line.substring(6).replace("`", "").trim(); // ID
                    bolIsReceipt = true;
                }
                if (strTrimmedLine.startsWith("DATA:")) {
                    strOutLine = strOutLine + ";" + line.trim(); // ReceiptTimestamp
                    strDate = TimingClass.convertTimeFormat(line.substring(12, 22)
                            , "dd-MM-yyyy", "yyyy-MM-dd");
                    strOutLine = strOutLine + ";" + strDate + " " + line.substring(28); // Timestamp
                    strOutLine = strOutLine + ";" + strDate.substring(0, 4); // Year
                    strOutLine = strOutLine + ";" + TimingClass.getYearMonthWithFullName(strDate); // YearMonth
                    strOutLine = strOutLine + ";" + TimingClass.getIsoYearWeek(strDate); // YearWeek
                    strOutLine = strOutLine + ";" + strDate; // Date
                    strOutLine = strOutLine + ";" + line.substring(28); // Hour
                }
                if (line.startsWith("S/N:")) {
                    strOutLine = strOutLine + ";" + line.substring(4, 16); // SerialNumber
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
            final String strFeedback = e.getLocalizedMessage();
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
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

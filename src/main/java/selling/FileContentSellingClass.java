package selling;

import javajava.*;
import org.apache.logging.log4j.Level;

import database.DatabaseConnectivity;
import database.DatabaseResultSettingClass;
import database.DatabaseSpecificMySql;
import file.FileContentClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * File content management for Selling
 */
public final class FileContentSellingClass {
    /**
     * String for Cheie3
     */
    /* default */ final static String STR_CHEIE3 = "Cheie3";
    /**
     * String for OraComparatieInt
     */
    /* default */ final static String STR_ORA_CMP_INT = "OraComparatieInt";
    /**
     * String for HourComparableInt
     */
    /* default */ final static String STR_HR_CMP_INT = "HourComparableInt";

    /**
     * List for output result
     */
    /* default */ final static List<String> LIST_OUT_RESULT = new ArrayList<>();

    private static void buildRecordString(final String strDateIso8601,
                                          final LocalDateTime dtBank,
                                          final Properties crtRowBank,
                                          final int intBankKeyCounter,
                                          final int inBankOverallRow,
                                          final Properties crtRowCashReg,
                                          final int intCashRegCntr,
                                          final int inCashRegRow,
                                          final String strScenario) {
        LocalDateTime dtCashRegister = null;
        String strTimestampCr = "";
        String crtRowCrCheie3BF = "";
        String crtRowCrHourC = "";
        String crtRowCrSrlN = "";
        if (!strScenario.contains("FĂRĂ")) {
            dtCashRegister = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                    crtRowCashReg.get("HourComparable").toString());
            strTimestampCr = dtCashRegister.toString().replace("T", " ");
            crtRowCrCheie3BF = crtRowCashReg.get("Cheie3BF").toString();
            crtRowCrHourC = crtRowCashReg.get("HourComparable").toString();
            crtRowCrSrlN = crtRowCashReg.get("SerialNumber").toString();
        }
        final String strDuration = switch(strScenario) {
            case "Înregistrare Bancă DUPĂ înregistrare Casă Fiscală" -> TimingClass.convertNanosecondsIntoSomething(
                    Duration.between(dtCashRegister, dtBank), "TimeClockClassic");
            case "Înregistrare Bancă ÎNAINTE DE înregistrare Casă Fiscală" -> TimingClass.convertNanosecondsIntoSomething(
                    Duration.between(dtBank, dtCashRegister), "TimeClockClassic");
            default -> "";
        };
        final String strCheie3 = crtRowBank.get(STR_CHEIE3).toString();
        final String strFeedback = String.join(";", crtRowBank.get("OriginalOrder").toString(),
                strCheie3 + "==>" + crtRowBank.get("OraComparatie").toString(),
                strCheie3.substring(strCheie3.length() - 7),
                crtRowCrCheie3BF,
                crtRowCrHourC,
                crtRowCrSrlN,
                strScenario,
                String.valueOf(inBankOverallRow + 1),
                TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"),
                String.valueOf(intBankKeyCounter),
                String.valueOf(intCashRegCntr),
                dtBank.toString().replace("T", " "),
                strTimestampCr,
                strDuration);
        final String strFeedbackForLog = strFeedback + " ----> " + inCashRegRow;
        LoggerLevelProvider.LOGGER.debug(strFeedbackForLog);
        LIST_OUT_RESULT.add(strFeedback); // new line in CSV
    }

    /**
     * Read File Content as Selling Point Receipt into a CSV file
     *
     * @param inFileName input file name
     * @param strCompanyName company name identifier
     * @param outFileName output file name
     */
    public static void consolidateSellingPointReceiptIntoCsvFile(final String inFileName,
                                                                 final String strCompanyName,
                                                                 final String outFileName) {
        final List<String> lstOutput = new ArrayList<>(); // content will be here
        final String[] arrayColumns;
        arrayColumns = new String[] {"Company", "Address", "City", "County", "CIF",
                "PaidValue", "ValueCard", "ValueModernPayment", "ValueCash", "ValueRest", "ReceiptValue", "ReceiptVAT",
                "Z", "ReceiptNo", "ReceiptID",
                "ReceiptTimestamp", "Timestamp", "Year", "YearMonth", "ISO_YearWeek", "Date", "Hour",
                "SerialNumber", "ReceiptTD"};
        lstOutput.add(String.join(";", arrayColumns)); // adding the CSV Header to the list
        final StringBuilder strBuilder = new StringBuilder(50);
        try (BufferedReader reader = Files.newBufferedReader(Path.of(inFileName))) {
            String line;
            int intReceiptLineNo = 0;
            boolean bolIsReceipt = false;
            String strTotalValue;
            BigDecimal decTotalValue = BigDecimal.ZERO;
            String strCardValue = "";
            String strMdrnPmntVl = "";
            String strCashValue = "";
            String strRestValue = "";
            String strTrimmedLine;
            String strDate;
            BigDecimal decRcptVal;
            boolean bolThereIsLine = true;
            while (bolThereIsLine) {
                line = reader.readLine();
                if (line == null) {
                    bolThereIsLine = false;
                } else {
                    intReceiptLineNo++;
                    strTrimmedLine = line.trim();
                    if (strTrimmedLine.replaceAll("  ", " ").startsWith(strCompanyName)) {
                        strBuilder.setLength(0);
                        strBuilder.append(strTrimmedLine); // Company
                        intReceiptLineNo = 1;
                        bolIsReceipt = false;
                        decTotalValue = BigDecimal.ZERO;
                        strTotalValue = "";
                    }
                    if (List.of(2, 3, 4).contains(intReceiptLineNo)) {
                        strBuilder.append(';').append(strTrimmedLine); // 2 = Address, 3 = City, 4 = County
                    }
                    if (strTrimmedLine.startsWith("CIF:")) {
                        strBuilder.append(';').append(line.replaceAll("CIF:", "").trim()); // CIF
                    }
                    if (line.startsWith("TOTAL LEI")) {
                        strTotalValue = line.replaceAll("TOTAL LEI", "").trim();
                        decTotalValue = new BigDecimal(strTotalValue).stripTrailingZeros();
                        strBuilder.append(';').append(strTotalValue); // Value
                    }
                    if (line.startsWith("CARD")) {
                        strCardValue = line.replaceAll("CARD", "").trim(); // ValueCard
                    }
                    if (line.startsWith("PLATA MODERNA")) {
                        strMdrnPmntVl = line.replaceAll("PLATA MODERNA", "").trim(); // ValueCard
                    }
                    if (line.startsWith("NUMERAR LEI")) {
                        strCashValue = line.replaceAll("NUMERAR LEI", "").trim(); // ValueCash
                    }
                    if (line.startsWith("REST")) {
                        strRestValue = line.replaceAll("REST", "").trim(); // ValueCard
                    }
                    if (line.startsWith("TOTAL TVA BON")) {
                        strBuilder.append(';')
                                .append(strCardValue) // ValueCard
                                .append(';')
                                .append(strMdrnPmntVl) // ValueModernPayment
                                .append(';')
                                .append(strCashValue) // ValueCash
                                .append(';')
                                .append(strRestValue); // ValueRest
                        decRcptVal = decTotalValue.subtract(new BigDecimal(strRestValue));
                        strBuilder.append(';')
                                .append(decRcptVal) // ReceiptValue
                                .append(';')
                                .append(line.replaceAll("TOTAL TVA BON", "").trim()); // ReceiptVAT
                        strCardValue = "";
                        strMdrnPmntVl = "";
                        strCashValue = "";
                        strRestValue = "";
                    }
                    if (line.startsWith("Z:")) {
                        strBuilder.append(';')
                                .append(line.substring(2, 6)) // Z
                                .append(';')
                                .append(line.substring(10, 14)); // Receipt
                    }
                    if (line.startsWith("ID BF:")) {
                        strBuilder.append(';').append(line.substring(6).replace("`", "").trim()); // ID
                        bolIsReceipt = true;
                    }
                    if (strTrimmedLine.startsWith("DATA:")) {
                        strBuilder.append(';').append(line.trim()); // ReceiptTimestamp
                        strDate = TimingClass.convertTimeFormat(line.substring(12, 22)
                                , "dd-MM-yyyy", "yyyy-MM-dd");
                        strBuilder.append(';')
                                .append(strDate)
                                .append(' ')
                                .append(line.substring(28)) // Timestamp
                                .append(';')
                                .append(strDate.substring(0, 4)) // Year
                                .append(';')
                                .append(TimingClass.getYearMonthWithFullName(strDate)) // YearMonth
                                .append(';')
                                .append(TimingClass.getIsoYearWeek(strDate)) // YearWeek
                                .append(';')
                                .append(strDate) // Date
                                .append(';')
                                .append(line.substring(28)); // Hour
                    }
                    if (line.startsWith("S/N:")) {
                        strBuilder.append(';')
                                .append(line.substring(4, 16)) // SerialNumber
                                .append(';')
                                .append(line.substring(34)); // TD
                        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                            final String strFeedback = strBuilder.toString();
                            LoggerLevelProvider.LOGGER.debug(strFeedback);
                        }
                        if (bolIsReceipt) {
                            lstOutput.add(strBuilder.toString()); // end of Receipt so current CSV line can be safely added to the List
                            bolIsReceipt = false;
                        }
                    }
                }
            }
            FileContentClass.writeListToTextFile(lstOutput, outFileName);
        } catch (IOException e) {
            final String strFeedback = e.getLocalizedMessage();
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Pair Bank records with Cash Register ones
     *
     * @param strFileNameOut name of the output file
     */
    public static void pairMySqlBankAndCashRegisterRecords(final String strFileNameOut) {
        final Properties propMySql = DatabaseSpecificMySql.getConnectionPropertiesForMySQL();
        final Properties qryProperties = new Properties();
        final Properties propsBank = new Properties();
        propsBank.put("strWhich", "Purpose is to get all Bank records");
        propsBank.put("strKind", "Values");
        propsBank.put("strQueryToUse", """
SELECT
      OriginalOrder
    , Cheie3
    , OraComparatie
    , CAST(OraComparatie AS UNSIGNED) AS OraComparatieInt
FROM
    mt.bt
WHERE
    Relevant = 'Yes'
ORDER BY
      Cheie3
    , OraComparatie
LIMIT 31;""");
        final List<Integer> listRowFound = new ArrayList<>();
        LIST_OUT_RESULT.add(String.join(";", "OrdineBT", "Cheie4BT", "Valoare", "Cheie3BF", "OraBF", "SerialNumber"
                , "Scenariu", "Nr", "TS", "ContorCheieBT", "ContorCheieBF", "TimestampBT", "TimestampBF", "DifTimp")); // header of CSV
        try (Connection objConnection = DatabaseSpecificMySql.getMySqlConnection(propMySql, "mt");
             Statement objStatement = DatabaseConnectivity.createSqlStatement("MySQL", objConnection)) {
            final List<Properties> listBank = DatabaseResultSettingClass.getResultSetStandardized(objStatement, propsBank, qryProperties);
            final int intBankRecs = listBank.size();
            int intBankKeyCounter = 0;
            int intCrCounter = 0;
            final String strFeedback = String.format("Dimensiunea listei cu înregistrări de bancă este %s", intBankRecs);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
            String strCheie3memory = "<empty>";
            LocalDateTime dtBank;
            List<Properties> listCr = null;
            Properties crtRowBank;
            Properties crtRowCr;
            final String strQueryT = """
SELECT
      Cheie3BF
    , HourComparable
    , CAST(HourComparable AS UNSIGNED) AS HourComparableInt
    , SerialNumber
FROM
    mt.bf
WHERE
    Cheie3BF = '{Cheie3}'
ORDER BY
      HourComparable;""";
            final Properties propsCrKey = new Properties();
            for(int i = 0; i < intBankRecs; i++) { // cycle through Bank records
                final int intCrtRow = i + 1;
                crtRowBank = listBank.get(i);
                intBankKeyCounter++;
                final String strFeedbackRow = String.format("Bank row %s => %s vs. %s", i, strCheie3memory, crtRowBank.get(STR_CHEIE3).toString());
                LoggerLevelProvider.LOGGER.debug(strFeedbackRow);
                if (!strCheie3memory.equalsIgnoreCase(crtRowBank.get(STR_CHEIE3).toString())) {
                    strCheie3memory = crtRowBank.get(STR_CHEIE3).toString();
                    intBankKeyCounter = 1;
                    propsCrKey.clear();
                    propsCrKey.put("strWhich", "Purpose is to get relevant Cash Register records");
                    propsCrKey.put("strKind", "Values");
                    final String strQueryCr = strQueryT.replace("{Cheie3}", strCheie3memory);
                    propsCrKey.put("strQueryToUse", strQueryCr);
                    listCr = DatabaseResultSettingClass.getResultSetStandardized(objStatement, propsCrKey, qryProperties);
                    intCrCounter = 0;
                }
                final int intCheieFirstPos = strCheie3memory.indexOf('_');
                final String strDateIso8601 = strCheie3memory.substring(intCheieFirstPos + 1, intCheieFirstPos + 12);
                dtBank = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                        crtRowBank.get("OraComparatie").toString());
                if (!listCr.isEmpty()) {
                    int intCrSize = listCr.size();
                    final String strFeedbackCr = String.format("Dimensiunea listei cu înregistrări de casă fiscală este %s", intCrSize);
                    LoggerLevelProvider.LOGGER.debug(strFeedbackCr);
                    for(int j = 0; j < intCrSize; j++) { // cycle through Cash Register records
                        crtRowCr = listCr.get(j);
                        final String strFeedbackR = String.format("Cash Register row %s => %s vs. %s", j,
                                crtRowBank.get(STR_ORA_CMP_INT).toString(),
                                crtRowCr.get(STR_HR_CMP_INT).toString());
                        LoggerLevelProvider.LOGGER.debug(strFeedbackR);
                        intCrCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)
                            && (Integer.parseInt(crtRowBank.get(STR_ORA_CMP_INT).toString()) >= Integer.parseInt(crtRowCr.get(STR_HR_CMP_INT).toString()))) {
                            listRowFound.add(i + 1);
                            buildRecordString(strDateIso8601, dtBank,
                                    crtRowBank, intBankKeyCounter, i,
                                    crtRowCr, intCrCounter, j,
                                    "Înregistrare Bancă DUPĂ înregistrare Casă Fiscală");
                            listCr.remove(j);
                            intCrCounter++;
                            intCrSize--;
                        }
                    }
                    intCrCounter = intCrCounter - intCrSize;
                    for(int j = 0; j < intCrSize; j++) { // cycle through Cash Register records
                        crtRowCr = listCr.get(j);
                        final String strFeedbackR = String.format("Cash Register row %s => %s vs. %s", j,
                                crtRowBank.get(STR_ORA_CMP_INT).toString(),
                                crtRowCr.get(STR_HR_CMP_INT).toString());
                        LoggerLevelProvider.LOGGER.debug(strFeedbackR);
                        intCrCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)
                            && (Integer.parseInt(crtRowBank.get(STR_ORA_CMP_INT).toString()) < Integer.parseInt(crtRowCr.get(STR_HR_CMP_INT).toString()))) {
                            listRowFound.add(i + 1);
                            buildRecordString(strDateIso8601, dtBank,
                                    crtRowBank, intBankKeyCounter, i,
                                    crtRowCr, intCrCounter, j,
                                    "Înregistrare Bancă ÎNAINTE DE înregistrare Casă Fiscală");
                            listCr.remove(j);
                            intCrCounter++;
                            intCrSize--;
                        }
                    }
                    intCrCounter = intCrCounter - intCrSize;
                }
                if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                    buildRecordString(strDateIso8601, dtBank,
                            crtRowBank, intBankKeyCounter, i,
                            null, intCrCounter, 0,
                            "Înregistrare Bancă FĂRĂ înregistrare Casă Fiscală");
                }
            }
            FileContentClass.writeListToTextFile(LIST_OUT_RESULT, strFileNameOut);
        } catch(SQLException e){
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLerror"), Arrays.toString(e.getStackTrace()), StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Constructor
     */
    private FileContentSellingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }

}

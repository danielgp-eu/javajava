package javajava;

import org.apache.logging.log4j.Level;

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

public class FileContentSellingClass {

    final static List<String> listOutResult = new ArrayList<>();

    private static void buildRecordString(final String strDateIso8601, final LocalDateTime dtBank,
                                          final Properties crtRowBank, final int intBankKeyCounter, final int inBankOverallRow,
                                          final Properties crtRowCashRegister, final int intCashRegisterCounter, final int inCashRegisterRow,
                                          final String strScenario) {
        LocalDateTime dtCashRegister = null;
        String strTimestampCashRegister = "";
        String crtRowCashRegisterCheie3BF = "";
        String crtRowCashRegisterHourComparable = "";
        String crtRowCashRegisterSerialNumber = "";
        if (!strScenario.contains("FĂRĂ")) {
            dtCashRegister = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                    crtRowCashRegister.get("HourComparable").toString());
            strTimestampCashRegister = dtCashRegister.toString().replace("T", " ");
            crtRowCashRegisterCheie3BF = crtRowCashRegister.get("Cheie3BF").toString();
            crtRowCashRegisterHourComparable = crtRowCashRegister.get("HourComparable").toString();
            crtRowCashRegisterSerialNumber = crtRowCashRegister.get("SerialNumber").toString();
        }
        final String strDuration = switch(strScenario) {
            case "Înregistrare Bancă DUPĂ înregistrare Casă Fiscală" -> TimingClass.convertNanosecondsIntoSomething(
                    Duration.between(dtCashRegister, dtBank), "TimeClockClassic");
            case "Înregistrare Bancă ÎNAINTE DE înregistrare Casă Fiscală" -> TimingClass.convertNanosecondsIntoSomething(
                    Duration.between(dtBank, dtCashRegister), "TimeClockClassic");
            default -> "";
        };
        final String strFeedback = String.join(";", crtRowBank.get("OriginalOrder").toString(),
                crtRowBank.get("Cheie3").toString() + "==>" + crtRowBank.get("OraComparatie").toString(),
                crtRowBank.get("Cheie3").toString().substring(crtRowBank.get("Cheie3").toString().length() - 7),
                crtRowCashRegisterCheie3BF,
                crtRowCashRegisterHourComparable,
                crtRowCashRegisterSerialNumber,
                strScenario,
                String.valueOf((inBankOverallRow + 1)),
                TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"),
                String.valueOf(intBankKeyCounter),
                String.valueOf(intCashRegisterCounter),
                dtBank.toString().replace("T", " "),
                strTimestampCashRegister,
                strDuration);
        final String strFeedbackForLog = strFeedback + " ----> " + inCashRegisterRow;
        LoggerLevelProvider.LOGGER.debug(strFeedbackForLog);
        listOutResult.add(strFeedback); // new line in CSV
    }

    /**
     * Read File Content as Selling Point Receipt into a CSV file
     *
     * @param inFileName input file name
     * @param strCompanyName company name identifier
     * @param outFileName output file name
     */
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
            FileContentClass.writeListToTextFile(lstOutput, outFileName);
        } catch (IOException e) {
            final String strFeedback = e.getLocalizedMessage();
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

    public static void pairMySqlBankAndCashRegisterRecords(final String strFileNameOut) {
        Properties propMySql = DatabaseSpecificMySql.getConnectionPropertiesForMySQL();
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
        listOutResult.add(String.join(";", "OrdineBT", "Cheie4BT", "Valoare", "Cheie3BF", "OraBF", "SerialNumber"
                , "Scenariu", "Nr", "TS", "ContorCheieBT", "ContorCheieBF", "TimestampBT", "TimestampBF", "DifTimp")); // header of CSV
        try (Connection objConnection = DatabaseSpecificMySql.getMySqlConnection(propMySql, "mt");
             Statement objStatement = DatabaseConnectivity.createSqlStatement("MySQL", objConnection)) {
            final List<Properties> listBank = DatabaseResultSettingClass.getResultSetStandardized(objStatement, propsBank, qryProperties);
            int intBankRecs = listBank.size();
            int intBankKeyCounter = 0;
            int intCashRegisterCounter = 0;
            final String strFeedback = String.format("Dimensiunea listei cu înregistrări de bancă este %s", intBankRecs);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
            String strCheie3memory = "<empty>";
            String strValue = String.valueOf(0);
            LocalDateTime dtBank = null;
            List<Properties> listCashRegister = null;
            Properties crtRowBank;
            Properties crtRowCashRegister;
            final String strQueryCashRegisterTemplate = """
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
            for(int i = 0; i < intBankRecs; i++) { // cycle through Bank records
                final int intCrtRow = i + 1;
                crtRowBank = listBank.get(i);
                intBankKeyCounter++;
                final String strFeedbackRow = String.format("Bank row %s => %s vs. %s", i, strCheie3memory, crtRowBank.get("Cheie3").toString());
                LoggerLevelProvider.LOGGER.debug(strFeedbackRow);
                if (!strCheie3memory.equalsIgnoreCase(crtRowBank.get("Cheie3").toString())) {
                    strCheie3memory = crtRowBank.get("Cheie3").toString();
                    strValue = strCheie3memory.substring(strCheie3memory.length() - 7);
                    intBankKeyCounter = 1;
                    final Properties propsCashRegisterKey = new Properties();
                    propsCashRegisterKey.put("strWhich", "Purpose is to get relevant Cash Register records");
                    propsCashRegisterKey.put("strKind", "Values");
                    final String strQueryCashRegister = strQueryCashRegisterTemplate.replace("{Cheie3}", strCheie3memory);
                    propsCashRegisterKey.put("strQueryToUse", strQueryCashRegister);
                    listCashRegister = DatabaseResultSettingClass.getResultSetStandardized(objStatement, propsCashRegisterKey, qryProperties);
                    intCashRegisterCounter = 0;
                }
                final String strDateIso8601 = strCheie3memory.substring(strCheie3memory.indexOf("_") + 1,strCheie3memory.indexOf("_") + 12);
                dtBank = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                        crtRowBank.get("OraComparatie").toString());
                if (!listCashRegister.isEmpty()) {
                    int intCashregisterRecs = listCashRegister.size();
                    final String strFeedbackCashReg = String.format("Dimensiunea listei cu înregistrări de casă fiscală este %s", intCashregisterRecs);
                    LoggerLevelProvider.LOGGER.debug(strFeedbackCashReg);
                    for(int j = 0; j < intCashregisterRecs; j++) { // cycle through Cash Register records
                        crtRowCashRegister = listCashRegister.get(j);
                        final String strFeedbackR = String.format("Cash Register row %s => %s vs. %s", j,
                                crtRowBank.get("OraComparatieInt").toString(),
                                crtRowCashRegister.get("HourComparableInt").toString());
                        LoggerLevelProvider.LOGGER.debug(strFeedbackR);
                        intCashRegisterCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                            if (Integer.parseInt(crtRowBank.get("OraComparatieInt").toString()) >= Integer.parseInt(crtRowCashRegister.get("HourComparableInt").toString())) {
                                listRowFound.add((i + 1));
                                buildRecordString(strDateIso8601, dtBank,
                                        crtRowBank, intBankKeyCounter, i,
                                        crtRowCashRegister, intCashRegisterCounter, j,
                                        "Înregistrare Bancă DUPĂ înregistrare Casă Fiscală");
                                listCashRegister.remove(j);
                                intCashRegisterCounter++;
                                intCashregisterRecs--;
                            }
                        }
                    }
                    intCashRegisterCounter = intCashRegisterCounter - intCashregisterRecs;
                    for(int j = 0; j < intCashregisterRecs; j++) { // cycle through Cash Register records
                        crtRowCashRegister = listCashRegister.get(j);
                        final String strFeedbackR = String.format("Cash Register row %s => %s vs. %s", j,
                                crtRowBank.get("OraComparatieInt").toString(),
                                crtRowCashRegister.get("HourComparableInt").toString());
                        LoggerLevelProvider.LOGGER.debug(strFeedbackR);
                        intCashRegisterCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                            if (Integer.parseInt(crtRowBank.get("OraComparatieInt").toString()) < Integer.parseInt(crtRowCashRegister.get("HourComparableInt").toString())) {
                                listRowFound.add((i + 1));
                                buildRecordString(strDateIso8601, dtBank,
                                        crtRowBank, intBankKeyCounter, i,
                                        crtRowCashRegister, intCashRegisterCounter, j,
                                        "Înregistrare Bancă ÎNAINTE DE înregistrare Casă Fiscală");
                                listCashRegister.remove(j);
                                intCashRegisterCounter++;
                                intCashregisterRecs--;
                            }
                        }
                    }
                    intCashRegisterCounter = intCashRegisterCounter - intCashregisterRecs;
                }
                if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                    buildRecordString(strDateIso8601, dtBank,
                            crtRowBank, intBankKeyCounter, i,
                            null, intCashRegisterCounter, 0,
                            "Înregistrare Bancă FĂRĂ înregistrare Casă Fiscală");
                }
            }
            FileContentClass.writeListToTextFile(listOutResult, strFileNameOut);
        } catch(SQLException e){
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLerror"), Arrays.toString(e.getStackTrace()), StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }
}

package javajava;

import org.apache.logging.log4j.Level;
import picocli.CommandLine;

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
 * Example class
 */
@CommandLine.Command(
        name = "top",
        subcommands = {
            GetInformationFromTextWithSellingPointReceiptsIntoCsvFile.class,
            PairBankRecordsWithSellingReceipts.class
        }
)
public class ExampleMasterTaste {

    /**
     * log Application Start
     */
    private static void logApplicationStart() {
        final String strFeedback = JavaJavaLocalization.getMessage("i18nNewExec") + "-".repeat(80);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            LoggerLevelProvider.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        JavaJavaLocalization.setLocaleByString(JavaJavaLocalization.getUserLocale());
        logApplicationStart();
        final int exitCode = new CommandLine(new Example()).execute(args);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN) && (exitCode != 0)) {
            String strFeedbackExit = String.format("Exiting with code %s", exitCode);
            LoggerLevelProvider.LOGGER.info(strFeedbackExit);
        }
        final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nEntOp"), args[0]));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            LoggerLevelProvider.LOGGER.info(strFeedback);
        }
    }
}

/**
 * Gets information from Text for Selling Point Sale Into CSV file
 */
@CommandLine.Command(name = "GetInformationFromTextWithSellingPointReceiptsIntoCsvFile", description = "Gets information from text into CSV file")
class GetInformationFromTextWithSellingPointReceiptsIntoCsvFile implements Runnable {

    /**
     * String for FileName
     */
    @CommandLine.Option(
            names = {"-flIn", "--inFileName"},
            description = "File Name to be analyzed",
            arity = "1",
            required = true)
    private String strFileNameIn;

    @Override
    public void run() {
        final String strFileNameOut = strFileNameIn + ".csv";
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format("I will process %s file and write results into %s as a CSV file", strFileNameIn, strFileNameOut);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        FileContentClass.getFileContentAsSellingPointReceiptIntoCsvFile(strFileNameIn, "MASTER TASTE", strFileNameOut);
    }
}

/**
 * Pair bank records with selling receipts and store result-set into CSV file
 */
@CommandLine.Command(name = "PairBankRecordsWithSellingReceipts", description = "Pair bank records with selling receipts and store result-set into CSV file")
class PairBankRecordsWithSellingReceipts implements Runnable {

    /**
     * String for FileName
     */
    @CommandLine.Option(
            names = {"-flOut", "--outFileName"},
            description = "File Name to store result-set",
            arity = "1",
            required = true)
    private String strFileNameOut;

    @Override
    public void run() {
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
    , OraComparatie;""");
        final List<Integer> listRowFound = new ArrayList<>();
        final List<String> listOutResult = new ArrayList<>();
        listOutResult.add(String.join(";", "OrdineBT", "Cheie4BT", "Valoare", "Cheie3BF", "OraBF", "SerialNumber"
                , "Scenariu", "Nr", "TS", "ContorCheieBT", "ContorCheieBF", "TimestampBT", "TimestampBF", "DifTimp")); // header of CSV
        try (Connection objConnection = DatabaseSpecificMySql.getMySqlConnection(propMySql, "mt");
             Statement objStatement = DatabaseConnectivity.createSqlStatement("MySQL", objConnection)) {
            final List<Properties> listBank = DatabaseResultSettingClass.getResultSetStandardized(objStatement, propsBank, qryProperties);
            int intBankRecs = listBank.size();
            int intBankKeyCounter = 0;
            int intCashRegisterCounter = 0;
            LoggerLevelProvider.LOGGER.debug(String.format("Dimensiunea listei cu înregistrări de bancă este %s", intBankRecs));
            String strCheie3memory = "<empty>";
            String strValue = String.valueOf(0);
            LocalDateTime dtBank = null;
            LocalDateTime dtCashRegister;
            List<Properties> listCashRegister = null;
            Properties crtRowBank;
            Properties crtRowCashRegister;
            String strFeedback;
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
                LoggerLevelProvider.LOGGER.debug("Bank row " + i + " => " + strCheie3memory + " vs. " + crtRowBank.get("Cheie3").toString());
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
                    LoggerLevelProvider.LOGGER.debug(String.format("Dimensiunea listei cu înregistrări de casă fiscală este %s", intCashregisterRecs));
                    for(int j = 0; j < intCashregisterRecs; j++) { // cycle through Cash Register records
                        crtRowCashRegister = listCashRegister.get(j);
                        LoggerLevelProvider.LOGGER.debug("Cash Register row " + j + " => " + crtRowBank.get("OraComparatieInt").toString()
                                + " vs. " + crtRowCashRegister.get("HourComparableInt").toString());
                        intCashRegisterCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                            if (Integer.parseInt(crtRowBank.get("OraComparatieInt").toString()) >= Integer.parseInt(crtRowCashRegister.get("HourComparableInt").toString())) {
                                listRowFound.add((i + 1));
                                dtCashRegister = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                                        crtRowCashRegister.get("HourComparable").toString());
                                final Duration objDuration = Duration.between(dtCashRegister, dtBank);
                                strFeedback = String.join(";", crtRowBank.get("OriginalOrder").toString(),
                                        strCheie3memory + "==>" + crtRowBank.get("OraComparatie").toString(),
                                        strValue,
                                        crtRowCashRegister.get("Cheie3BF").toString(),
                                        crtRowCashRegister.get("HourComparable").toString(),
                                        crtRowCashRegister.get("SerialNumber").toString(),
                                        "Înregistrare Bancă DUPĂ înregistrare Casă Fiscală",
                                        String.valueOf((i + 1)),
                                        TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"),
                                        String.valueOf(intBankKeyCounter),
                                        String.valueOf(intCashRegisterCounter),
                                        dtBank.toString().replace("T", " "),
                                        dtCashRegister.toString().replace("T", " "),
                                        TimingClass.convertNanosecondsIntoSomething(objDuration, "TimeClockClassic"));
                                LoggerLevelProvider.LOGGER.debug(strFeedback + " ----> " + j);
                                listOutResult.add(strFeedback); // line in CSV
                                listCashRegister.remove(j);
                                intCashRegisterCounter++;
                                intCashregisterRecs--;
                            }
                        }
                    }
                    intCashRegisterCounter = intCashRegisterCounter - intCashregisterRecs;
                    for(int j = 0; j < intCashregisterRecs; j++) { // cycle through Cash Register records
                        crtRowCashRegister = listCashRegister.get(j);
                        LoggerLevelProvider.LOGGER.debug("Cash Register row " + j + " => " + crtRowBank.get("OraComparatieInt").toString()
                                + " vs. " + crtRowCashRegister.get("HourComparableInt").toString());
                        intCashRegisterCounter++;
                        if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                            if (Integer.parseInt(crtRowBank.get("OraComparatieInt").toString()) < Integer.parseInt(crtRowCashRegister.get("HourComparableInt").toString())) {
                                listRowFound.add((i + 1));
                                dtCashRegister = TimingClass.getLocalDateTimeFromStrings(strDateIso8601,
                                        crtRowCashRegister.get("HourComparable").toString());
                                final Duration objDuration = Duration.between(dtBank, dtCashRegister);
                                strFeedback = String.join(";", crtRowBank.get("OriginalOrder").toString(),
                                        strCheie3memory + "==>" + crtRowBank.get("OraComparatie").toString(),
                                        strValue,
                                        crtRowCashRegister.get("Cheie3BF").toString(),
                                        crtRowCashRegister.get("HourComparable").toString(),
                                        crtRowCashRegister.get("SerialNumber").toString(),
                                        "Înregistrare Bancă ÎNAINTE DE înregistrare Casă Fiscală",
                                        String.valueOf((i + 1)),
                                        TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"),
                                        String.valueOf(intBankKeyCounter),
                                        String.valueOf(intCashRegisterCounter),
                                        dtBank.toString().replace("T", " "),
                                        dtCashRegister.toString().replace("T", " "),
                                        TimingClass.convertNanosecondsIntoSomething(objDuration, "TimeClockClassic"));
                                LoggerLevelProvider.LOGGER.debug(strFeedback + " ----> " + j);
                                listOutResult.add(strFeedback); // line in CSV
                                listCashRegister.remove(j);
                                intCashRegisterCounter++;
                                intCashregisterRecs--;
                            }
                        }
                    }
                    intCashRegisterCounter = intCashRegisterCounter - intCashregisterRecs;
                }
                if (listRowFound.stream().noneMatch(n -> n == intCrtRow)) {
                    strFeedback = String.join(";", crtRowBank.get("OriginalOrder").toString(),
                            strCheie3memory + "==>" + crtRowBank.get("OraComparatie").toString(),
                            strValue,
                            "",
                            "",
                            "",
                            "Înregistrare Bancă FĂRĂ înregistrare Casă Fiscală",
                            String.valueOf((i + 1)),
                            TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"),
                            String.valueOf(intBankKeyCounter),
                            "0",
                            dtBank.toString().replace("T", " "),
                            "",
                            "");
                    LoggerLevelProvider.LOGGER.debug(strFeedback);
                    listOutResult.add(strFeedback); // line in CSV
                }
            }
            FileContentClass.writeListToTextFile(listOutResult, "C:/Users/Daniel Popiniuc/Downloads/1/pairings.csv");
        } catch(SQLException e){
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLerror"), Arrays.toString(e.getStackTrace()), StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }
}
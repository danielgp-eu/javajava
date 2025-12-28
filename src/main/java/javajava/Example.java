package javajava;

import environment.EnvironmentCapturingClass;
import org.apache.logging.log4j.Level;

import database.DatabaseSpecificMySql;
import database.DatabaseSpecificSnowflake;
import picocli.CommandLine;
import selling.FileContentSellingClass;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        GetInformationFromDatabase.class,
        GetInformationFromTextWithSellingPointReceiptsIntoCsvFile.class,
        GetSpecificInformationFromSnowflake.class,
        GetSubFoldersFromFolder.class,
        LogEnvironmentDetails.class,
        PairBankRecordsWithSellingReceipts.class
    }
)
/**
 * Example class
 */
public final class Example implements Runnable {

    /**
     * log Application Start
     */
    private static void logApplicationStart() {
        final String strFeedback = JavaJavaLocalization.getMessage("i18nNewExec") + "-".repeat(80);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            LoggerLevelProvider.LOGGER.debug(strFeedback);
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
            final String strFeedbackExit = String.format("Exiting with code %s", exitCode);
            LoggerLevelProvider.LOGGER.info(strFeedbackExit);
        }
        final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nEntOp"), args[0]));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            LoggerLevelProvider.LOGGER.info(strFeedback);
        }
    }

    @Override
    public void run() {
        // intentionally left commented
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
        FileContentSellingClass.consolidateSellingPointReceiptIntoCsvFile(strFileNameIn, "MASTER TASTE", strFileNameOut);
    }
}

/**
 * Gets information from Database into Log file
 */
@CommandLine.Command(name = "GetInformationFromDatabase", description = "Gets information from Database into Log file")
class GetInformationFromDatabase implements Runnable {

    /**
     * Known Database Types
     */
	/* default */ static final List<String> LST_DB_TYPES = Arrays.asList(
        "MySQL",
        "Snowflake"
    );

    /**
     * Known Information Types
     */
	/* default */ static final List<String> LST_INFO_TYPES = Arrays.asList(
        "Columns",
        "Databases",
        "Schemas",
        "TablesAndViews",
        "Views",
        "ViewsLight"
    );

    /**
     * String for Database Type
     */
    @CommandLine.Option(
        names = { "-dbTp", "--databaseType" },
        description = "Type of Database",
        arity = "1",
        required = true,
        completionCandidates = DatabaseTypes.class)
    private String strDbType;

    /**
     * String for Information Type
     */
    @CommandLine.Option(
        names = { "-infTp", "--informationType" },
        description = "Type of Information",
        arity = "1..*",
        required = true,
        completionCandidates = InfoTypes.class)
    private String strInfoType;

    /**
     * Listing available options
     */
    /* default */ static class DatabaseTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_DB_TYPES.iterator();
        }
    }

    /**
     * Listing available options
     */
    /* default */ static class InfoTypes implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return LST_INFO_TYPES.iterator();
        }
    }

    /**
     * Action logic
     *
     * @param strDatabaseType type of Database (predefined values)
     */
    private static void performAction(final String strDatabaseType, final String strLclInfoType) {
        Properties properties = null;
        switch (strDatabaseType) {
            case "MySQL":
                properties = DatabaseSpecificMySql.getConnectionPropertiesForMySQL();
                DatabaseSpecificMySql.performMySqlPreDefinedAction(strLclInfoType, properties);
                break;
            case "Snowflake":
                DatabaseSpecificSnowflake.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strMsg = String.format(JavaJavaLocalization.getMessage("i18nUnknParamFinal"), strDatabaseType, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                LoggerLevelProvider.LOGGER.error(strMsg);
                break;
        }
    }

    @Override
    public void run() {
        if (!LST_DB_TYPES.contains(strDbType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --databaseType: " + strDbType + ". Valid values are: " + LST_DB_TYPES
            );
        }
        if (!LST_INFO_TYPES.contains(strInfoType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --informationType: " + strInfoType + ". Valid values are: " + LST_INFO_TYPES
            );
        }
        performAction(strDbType, strInfoType);
    }
}

/**
 * Gets specific information from Snowflake into Log file
 */
@CommandLine.Command(name = "GetSpecificInformationFromSnowflake", description = "Gets specific information from Snowflake into Log file")
class GetSpecificInformationFromSnowflake implements Runnable {

    /**
     * Known Information Types
     */
	/* default */ static final List<String> LST_INFO_TYPES = Arrays.asList(
            "CurrentUserAssignedRoles",
            "Warehouses"
    );

    /**
     * String for Information Type
     */
    @CommandLine.Option(
            names = { "-infTp", "--informationType" },
            description = "Type of Information",
            arity = "1..*",
            required = true,
            completionCandidates = GetInformationFromDatabase.DatabaseTypes.class)
    private String strInfoType;

    /**
     * Action logic
     *
     * @param strLclInfoType type of specific Snowflake information (predefined values)
     */
    private static void performAction(final String strLclInfoType) {
        final Properties properties = null;
        switch(strLclInfoType) {
            case "CurrentUserAssignedRoles":
                DatabaseSpecificSnowflake.performSnowflakePreDefinedAction("AvailableRoles", properties);
                break;
            case "Warehouses":
                DatabaseSpecificSnowflake.performSnowflakePreDefinedAction("AvailableWarehouses", properties);
                break;
            default:
                final String strMsg = String.format(JavaJavaLocalization.getMessage("i18nUnknParamFinal"), strLclInfoType, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                LoggerLevelProvider.LOGGER.error(strMsg);
                break;
        }
    }

    @Override
    public void run() {
        if (!LST_INFO_TYPES.contains(strInfoType)) {
            throw new CommandLine.ParameterException(
                    new CommandLine(this),
                    "Invalid value for --informationType: " + strInfoType + ". Valid values are: " + LST_INFO_TYPES
            );
        }
        performAction(strInfoType);
    }

}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "GetSubFoldersFromFolder", description = "Captures sub-folder from a Given Folder into Log file")
class GetSubFoldersFromFolder implements Runnable {

    @Override
    public void run() {
        FileHandlingClass.getSubFolderFromFolder("C:\\www\\Config\\");
    }

    /**
     * Private constructor to prevent instantiation
     */
    public GetSubFoldersFromFolder() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "LogEnvironmentDetails", description = "Captures execution environment details into Log file")
class LogEnvironmentDetails implements Runnable {

    @Override
    public void run() {
        final String strFeedback = EnvironmentCapturingClass.getCurrentEnvironmentDetails();
        LoggerLevelProvider.LOGGER.info(strFeedback);
    }
}

/**
 * Pair bank records with selling receipts and store result-set into CSV file
 */
@CommandLine.Command(name = "PairBankRecordsWithSellingReceipts", description = "Pair bank records with selling receipts and store result-set into CSV file")
class PairBankRecordsWithSellingReceipts implements Runnable {

    /**
     * List for Output result
     */
    /* default */ final static List<String> LST_OUT_RESULT = new ArrayList<>();
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
        FileContentSellingClass.pairMySqlBankAndCashRegisterRecords(strFileNameOut);
    }
}

package javajava;

import environment.EnvironmentCapturingClass;
import file.FileHandlingClass;
import file.FileLocatingClass;

import org.apache.logging.log4j.Level;
import org.apache.maven.shared.utils.StringUtils;

import database.DatabaseSpecificMySql;
import database.DatabaseSpecificSnowflakeClass;
import picocli.CommandLine;
import shell.ShellFeedbackClass;
import shell.ShellingClass;
import localization.JavaJavaLocalizationClass;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        ArchiveFolders.class,
        CleanOlderFilesFromFolder.class,
        GetInformationFromDatabase.class,
        GetSpecificInformationFromSnowflake.class,
        GetSubFoldersFromFolder.class,
        LogEnvironmentDetails.class
    }
)
/**
 * Example class
 */
public final class Example implements Runnable {
    /**
     * Database MySQL
     */
    public static final String STR_ONE_OR_MANY = "1..*";

    /**
     * Constructor empty
     */
    private Example() {
        // no init required
    }

    /**
     * log Application Start
     */
    private static void logApplicationStart() {
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nNewExec") + "-".repeat(80);
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
        	LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        final String userLocale = JavaJavaLocalizationClass.getUserLocale();
        JavaJavaLocalizationClass.setLocaleByString(userLocale);
        logApplicationStart();
        final int exitCode = new CommandLine(new Example()).execute(args);
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN) && (exitCode != 0)) {
            final String strFeedbackExit = String.format("Exiting with code %s", exitCode);
            LoggerLevelProviderClass.LOGGER.info(strFeedbackExit);
        }
        final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nEntOp"), args[0]));
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN)) {
        	LoggerLevelProviderClass.LOGGER.info(strFeedback);
        }
    }

    @Override
    public void run() {
        // intentionally left commented
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ArchiveFolders", description = "Archive sub-folders from a given folder")
class ArchiveFolders implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-aExe", "--archivingExecutable"},
            description = "Archiving executable (includes full path)",
            arity = "1",
            required = true)
    private String strArchivingExec;

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fldNm", "--folderName"},
            description = "Folder Name to be inspected",
            arity = Example.STR_ONE_OR_MANY,
            required = true)
    private String[] strFolderNames;

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fldDst", "--folderDestination"},
            description = "Destination Folder where archives will be created",
            arity = "1",
            required = true)
    private String strDestFolder;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-ap", "--archivePrefix"},
            description = "Prefix to apply to archive name")
    private String strArchivePrefix;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-as", "--archiveSuffix"},
            description = "Suffix to apply to archive name")
    private String strArchiveSuffix;

    /**
     * String for archive name prefix
     */
    @CommandLine.Option(
            names = {"-pwd", "--archivePassword"},
            description = "Password for archive encryption")
    private String strArchivePwd;

    @Override
    public void run() {
        final Properties propFolder = new Properties();
        for (final String strFolder : strFolderNames) {
            propFolder.clear();
            final Properties folderProps = FileLocatingClass.getFolderStatisticsRecursive(strFolder, propFolder);
            final Path path = Paths.get(strFolder);
            final String strArchiveName = StringUtils.stripEnd(strDestFolder, File.separator) + File.separator
                + ShellFeedbackClass.buildArchivingName(strArchivePrefix, path.getFileName().toString(), strArchiveSuffix);
            final String strArchiveDir = StringUtils.stripEnd(strFolder, File.separator);
            ShellingClass.archiveFolderAs7zUltra(strArchivingExec, strArchiveDir, strArchiveName, strArchivePwd);
            ShellFeedbackClass.exposeArchivedContent(strArchiveName, strFolder, folderProps);
        }
    }

    /**
     * Constructor
     */
    protected ArchiveFolders() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CleanOlderFilesFromFolder", description = "Clean files older than a given number of days")
class CleanOlderFilesFromFolder implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fldNm", "--folderName"},
            description = "Folder Name to be inspected",
            arity = Example.STR_ONE_OR_MANY,
            required = true)
    private String[] strFolderNames;
    /**
     * String for FileName
     */
    @CommandLine.Option(
            names = {"-dLmt", "--daysOlderLimit"},
            description = "Limit number of days to remove files from",
            arity = "1",
            required = true)
    private int intDaysOlderLimit;

    @Override
    public void run() {
        for (final String strFolder : strFolderNames) {
            FileHandlingClass.removeFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
        }
    }

    /**
     * Constructor
     */
    protected CleanOlderFilesFromFolder() {
        super();
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
                DatabaseSpecificSnowflakeClass.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strMsg = String.format(JavaJavaLocalizationClass.getMessage("i18nUnknParamFinal"), strDatabaseType, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(CommonClass.STR_I18N_UNKN)));
                LoggerLevelProviderClass.LOGGER.error(strMsg);
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

    /**
     * Constructor
     */
    protected GetInformationFromDatabase() {
        super();
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
                DatabaseSpecificSnowflakeClass.performSnowflakePreDefinedAction("AvailableRoles", properties);
                break;
            case "Warehouses":
                DatabaseSpecificSnowflakeClass.performSnowflakePreDefinedAction("AvailableWarehouses", properties);
                break;
            default:
                final String strMsg = String.format(JavaJavaLocalizationClass.getMessage("i18nUnknParamFinal"), strLclInfoType, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(CommonClass.STR_I18N_UNKN)));
                LoggerLevelProviderClass.LOGGER.error(strMsg);
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

    /**
     * Constructor
     */
    protected GetSpecificInformationFromSnowflake() {
        super();
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
        super();
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
        LoggerLevelProviderClass.LOGGER.info(strFeedback);
    }

    /**
     * Private constructor to prevent instantiation
     */
    public LogEnvironmentDetails() {
        super();
    }

}

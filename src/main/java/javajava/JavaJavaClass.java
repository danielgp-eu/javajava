package javajava;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import picocli.CommandLine;
import picocli.CommandLine.Mixin;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            AnalyzeColumnsFromCsvFile.class,
            AnalyzePomFile.class,
            ArchiveFolders.class,
            CaptureChecksumsOfFilesFromFolderIntoCsvFile.class,
            CaptureEnvironmentDetailsIntoJsonFile.class,
            CaptureImportsFromJavaSourceFilesIntoCsvFile.class,
            CaptureWindowsApplicationsInstalledIntoCsvFile.class,
            CleanOlderFilesFromFolder.class,
            ExperimentalFeature.class,
            GetInformationFromDatabase.class,
            GetSubFoldersFromFolder.class,
            JsonSplit.class
    }
)
public final class JavaJavaClass {

    /**
     * Constructor empty
     */
    private JavaJavaClass() {
        super();
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.setAutoLocale(true);
        CommonInteractiveClass.initializeLocalization();
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with 
        final int iExitCode = new CommandLine(new JavaJavaClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "AnalyzeColumnsFromCsvFile",
                     description = "Analyze columns from CSV file")
class AnalyzeColumnsFromCsvFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass options = new CommonInteractiveClass.InFileNameOptionMixinClass();

    /**
     *
     * @param strFileName input File
     * @param intColToEval number of column to evaluate (starting from 0)
     * @param intColToGrpBy number of column to group by (starting from 0)
     */
    private static void storeWordFrequencyIntoCsvFile(final String strFileName,
                                                      final Integer intColToEval,
                                                      final Integer intColToGrpBy) {
        // Group values by category
        final Map<String, List<String>> groupedColumns = FileOperationsClass.ContentReadingClass.getListOfValuesFromColumnGroupedByAnotherColumnValuesFromCsvFile(strFileName, intColToEval, intColToGrpBy);
        // Define merge rules
        final Map<List<String>, String> mergeRules = Map.of(
                List.of("ARRAY", "OBJECT", "VARIANT"), "COMPOSITE__STRUCTURED",
                List.of("FLOAT", "NUMBER"), "COMPOSITE__NUMERIC",
                List.of("DATETIME", "TIMESTAMP", "TIMESTAMP_LTZ", "TIMESTAMP_NTZ", "TIMESTAMP_TZ"), "COMPOSITE__TIMESTAMP",
                List.of("BINARY", "TEXT", "VARCHAR"), "COMPOSITE__TEXT"
        );
        final Map<String, List<String>> grpCols = BasicStructuresClass.ListAndMapClass.mergeKeys(groupedColumns, mergeRules);
        final String strFeedback = "=".repeat(20) + strFileName + "=".repeat(20);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingClass.setCsvColumnSeparator(',');
        grpCols.forEach((keyDataType, valList) -> {
            final String strColFileName = strFileName.replace(".csv", "__columns.csv");
            final String strFeedbackFile = "Writing file " + strColFileName;
            LogExposureClass.LOGGER.info(strFeedbackFile);
            FileOperationsClass.ContentWritingClass.setCsvLinePrefix(keyDataType);
            FileOperationsClass.ContentWritingClass.writeStringListToCsvFile(strColFileName, "DataType,Column", valList);
            final String strFeedbackWrt = String.format("Writing file for %s which has %s values", keyDataType, valList.size());
            LogExposureClass.LOGGER.info(strFeedbackWrt);
            final Map<String, Long> sorted = BasicStructuresClass.ListAndMapClass.getWordCounts(valList, "(_| )");
            FileOperationsClass.ContentWritingClass.writeLinkedHashMapToCsvFile(strFileName.replace(".csv", "__words.csv"), "DataType,Word,Occurrences", sorted);
        });
    }

    @Override
    public void run() {
        final String[] inFiles = options.getInFileNames();
        for (final String strFileName : inFiles) {
            storeWordFrequencyIntoCsvFile(strFileName, 3, 4);
        }
    }

    /**
     * Constructor
     */
    protected AnalyzeColumnsFromCsvFile() {
        // intentionally blank
    }

}

/**
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "AnalyzePomFile",
                     description = "Exposes information about a given POM")
class AnalyzePomFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass options = new CommonInteractiveClass.InFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strFeedbackThis = String.format("For this project relevant POM information is: {%s}", ProjectClass.Application.getApplicationDetails());
        LogExposureClass.LOGGER.info(strFeedbackThis);
        final String[] inFiles = options.getInFileNames();
        for (final String strFileName : inFiles) {
            ProjectClass.setExternalPomFile(strFileName);
            ProjectClass.loadProjectModel();
            final String strFeedback = String.format("For given POM file %s relevant information is: {%s}", strFileName, ProjectClass.Application.getApplicationDetails());
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    public AnalyzePomFile() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ArchiveFolders",
                     description = "Archive sub-folders from a given folder")
class ArchiveFolders implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-aExe", "--archivingExecutable"},
            description = "Archiving executable (including full path, optional)")
    private String strArchivingExec;

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass options = new CommonInteractiveClass.FolderNameOptionMixinClass();

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
        if (strArchivingExec != null) {
            ArchivingClass.setArchivingExecutable(strArchivingExec);
        }
        ArchivingClass.setArchivePrefix(strArchivePrefix);
        ArchivingClass.setArchiveSuffix(strArchiveSuffix);
        if (strArchivePwd != null) {
            ArchivingClass.setArchivePwd(strArchivePwd);
        }
        final String[] inFolders = options.getFolderNames();
        for (final String strFolder : inFolders) {
            propFolder.clear();
            final Properties folderProps = FileStatisticsClass.getFolderStatisticsRecursive(strFolder, propFolder);
            ArchivingClass.setArchivingDir(strFolder);
            ArchivingClass.setArchiveNameWithinDestinationFolder(strDestFolder);
            ArchivingClass.archiveFolderAs7z();
            ArchivingClass.exposeArchivedStatistics(folderProps);
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
@CommandLine.Command(name = "CaptureChecksumsOfFilesFromFolderIntoCsvFile",
                     description = "Get statistics for all files within a given folder")
class CaptureChecksumsOfFilesFromFolderIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass options = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inAlgorithms = {"SHA-256", "SHA3-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        final String[] inFolders = options.getFolderNames();
        final String outCsvFile = optionOut.getOutFileName();
        for (final String strFolder : inFolders) {
            final LocalDateTime startComputeTime = LocalDateTime.now();
            FileStatisticsClass.captureFileStatisticsFromFolder(strFolder, outCsvFile);
            final Duration objDuration = Duration.between(startComputeTime, LocalDateTime.now());
            final String strFeedback = String.format("For the folder %s calculated checksums are stored in the file %s operation completed in %s (which means %s | %s)", strFolder, outCsvFile, objDuration.toString(), TimingClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime"), TimingClass.convertNanosecondsIntoSomething(objDuration, "TimeClock"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected CaptureChecksumsOfFilesFromFolderIntoCsvFile() {
        super();
    }
}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "CaptureEnvironmentDetailsIntoJsonFile",
                     description = "Captures execution environment details into Log file")
class CaptureEnvironmentDetailsIntoJsonFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String strEnvDetails = EnvironmentCapturingAssembleClass.getCurrentEnvironmentDetails();
        final String strOutFileName = optionOut.getOutFileName();
        final String strFeedback = String.format("Environment details are %s and will intend to write it to %s file", strEnvDetails, strOutFileName);
        LogExposureClass.LOGGER.info(strFeedback);
        FileOperationsClass.ContentWritingClass.writeRawTextToFile(strOutFileName, strEnvDetails);
    }

    /**
     * Private constructor to prevent instantiation
     */
    public CaptureEnvironmentDetailsIntoJsonFile() {
        super();
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureImportsFromJavaSourceFilesIntoCsvFile",
                     description = "Get import inventory from all Java source files within a given folder")
class CaptureImportsFromJavaSourceFilesIntoCsvFile implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass options = new CommonInteractiveClass.FolderNameOptionMixinClass();
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String[] inFolders = options.getFolderNames();
        final String outCsvFile = optionOut.getOutFileName();
        for (final String strFolder : inFolders) {
            FileOperationsClass.ContentReadingClass.extractImportStatementsFromJavaSourceFilesIntoCsvFile(Path.of(strFolder), Path.of(outCsvFile));
        }
    }

    /**
     * Constructor
     */
    protected CaptureImportsFromJavaSourceFilesIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureWindowsApplicationsInstalledIntoCsvFile",
                     description = "Run the experimental new feature")
class CaptureWindowsApplicationsInstalledIntoCsvFile implements Runnable {
    /**
     * adds the options defined in 
     * CommonInteractiveClass.OutFileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.OutFileNameOptionMixinClass optionOut = new CommonInteractiveClass.OutFileNameOptionMixinClass();

    @Override
    public void run() {
        final String outCsvFile = optionOut.getOutFileName();
        ShellingClass.PowerShellExecutionClass.captureWindowsApplicationsIntoCsvFile(outCsvFile);
    }

    /**
     * Constructor
     */
    protected CaptureWindowsApplicationsInstalledIntoCsvFile() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CleanOlderFilesFromFolder",
                     description = "Clean files older than a given number of days")
class CleanOlderFilesFromFolder implements Runnable {

    /**
     * adds the options defined in 
     * CommonInteractiveClass.FolderNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.FolderNameOptionMixinClass options = new CommonInteractiveClass.FolderNameOptionMixinClass();
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
        FileOperationsClass.DeletingClass.OlderClass.setCleanedFolderStatistics(true);
        final String[] inFolders = options.getFolderNames();
        for (final String strFolder : inFolders) {
            FileOperationsClass.DeletingClass.OlderClass.setOrResetCleanedFolderStatistics();
            FileOperationsClass.DeletingClass.OlderClass.deleteFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
            final Map<String, Long> statsClndFldr = FileOperationsClass.DeletingClass.OlderClass.getCleanedFolderStatistics();
            final String strFeedback = String.format("Folder %s has been cleaned eliminating %s files and freeing %s bytes in terms of disk space...", strFolder, statsClndFldr.get("Files"), statsClndFldr.get("Size"));
            LogExposureClass.LOGGER.info(strFeedback);
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
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ExperimentalFeature",
                     description = "Run the experimental new feature")
class ExperimentalFeature implements Runnable {

    @Override
    public void run() {
        // no-op
    }

    /**
     * Constructor
     */
    protected ExperimentalFeature() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "GetInformationFromDatabase",
                     description = "Gets information from Database into Log file")
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
                properties = DatabaseOperationsClass.SpecificMySql.getConnectionPropertiesForMySQL();
                DatabaseOperationsClass.SpecificMySql.performMySqlPreDefinedAction(strLclInfoType, properties);
                break;
            case "Snowflake":
                DatabaseOperationsClass.SpecificSnowflakeClass.performSnowflakePreDefinedAction(strLclInfoType, properties);
                break;
            default:
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nUnknParamFinal"), strDatabaseType, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
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
 * Captures sub-folder from a Given Folder into Log file
 */
@CommandLine.Command(name = "GetSubFoldersFromFolder",
                     description = "Captures sub-folders from a Given Folder into Log file")
class GetSubFoldersFromFolder implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fldNm", "--folderName"},
            description = "Folder Name to use",
            arity = "1",
            required = true)
    private String strFolderName;

    @Override
    public void run() {
        FileStatisticsClass.RetrievingClass.getSubFoldersFromFolder(strFolderName);
    }

    /**
     * Private constructor to prevent instantiation
     */
    public GetSubFoldersFromFolder() {
        super();
    }

}

/**
 * JSON splitter
 */
@CommandLine.Command(name = "JsonSplit", 
                     description = "Splits a given JSON file into multiple smaller files") 
class JsonSplit implements Runnable {
    /**
     * JSON actual file size
     */
    private static long fileSize;
    /**
     * Size limit for split
     */
    private static final long SIZE_THRESHOLD = 5_368_709_120L; // 5GB value see https://convertlive.com/u/convert/gigabytes/to/bytes#5
    /**
     * Size percentage difference between actual & splitSize/SIZE_THRESHOLD
     */
    private static float sizeDifference;
    /**
     * balances threshold size
     */
    private static long sizeThreshold;
    /**
     * adds the options defined in 
     * CommonInteractiveClass.FileNameOptionMixinClass to this command
     */
    @Mixin
    private final CommonInteractiveClass.InFileNameOptionMixinClass options = new CommonInteractiveClass.InFileNameOptionMixinClass();
    /**
     * String for folder name
     */
    @CommandLine.Option(
            names = {"-fDst", "--folderDestination"},
            description = "Destination Folder where splited files will be created",
            arity = "1",
            required = true)
    private static String strDestFolder;
    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-sz", "--splitSize"},
            description = "Threshold size value beyound which split will be performed")
    private static long splitSize;
    /**
     * String for file name
     */
    @CommandLine.Option(
            names = {"-fld", "--field"},
            description = "Field name to use for split and bucketing",
            arity = "1",
            required = true)
    private static String strField;
    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-bl", "--bucketLength"},
            description = "Length of final characters to be overwritten as part of the bucketing logic (use -1 for no bucketing)")
    private static int bucketLength;

    @Override
    public void run() {
        final String[] inFiles = options.getInFileNames();
        for (final String strFileName : inFiles) {
            setFileSize(strFileName);
            if (fileSize <= 0) {
                final Properties propertiesReturn = FileStatisticsClass.RetrievingClass.checkFileExistanceAndReadability(fileSize, strFileName);
                final String strFeedback = String.format("There is something not right with given file name... %s", propertiesReturn);
                LogExposureClass.LOGGER.error(strFeedback);
            } else {
                setSplitSizeThreshold();
                setFileSizeDifferenceCompareToThreshold();
                if (fileSize <= sizeThreshold) {
                    final String strFeedback = String.format("File %s has a size of %s bytes which compare to split file threshold of %s bytes is %s%% smaller, hence split is NOT neccesary!", strFileName, fileSize, sizeThreshold, sizeDifference);
                    LogExposureClass.LOGGER.info(strFeedback);
                } else {
                    performJsonSplit(strFileName);
                }
            }
        }
    }

    private static void performJsonSplit(final String strFileName) {
        final String strFeedback = String.format("File %s has a size of %s bytes which compared to split file threshold of %s bytes is %s%% bigger, hence split IS required and will be performed!", strFileName, fileSize, sizeThreshold, Math.abs(sizeDifference));
        LogExposureClass.LOGGER.info(strFeedback);
        JsonOperationsClass.JsonArrayClass.setInputJsonFile(strFileName);
        JsonOperationsClass.JsonArrayClass.setDestinationFolder(strDestFolder);
        JsonOperationsClass.JsonArrayClass.setRelevantField(strField);
        if (bucketLength != 0) {
            JsonOperationsClass.JsonArrayClass.setBucketLength(bucketLength);
        }
        final String destPattern = JsonOperationsClass.JsonArrayClass.buildDestinationFileName("x").replaceAll("x.json", ".*.json");
        FileOperationsClass.DeletingClass.deleteFilesMatchingPatternFromFolder(strDestFolder, destPattern); // clean slate to avoid inheriting old content
        JsonOperationsClass.JsonArrayClass.splitJsonIntoSmallerGrouped(); // actual logic
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSize(final String strFileName) {
        fileSize = FileStatisticsClass.RetrievingClass.getFileSizeIfFileExistsAndIsReadable(strFileName);
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSizeDifferenceCompareToThreshold() {
        final float sizePercentage = BasicStructuresClass.computePercentageSafely(fileSize, sizeThreshold);
        sizeDifference = (float) new BigDecimal(Double.toString(100 - sizePercentage))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Setter for sizeThreshold
     */
    public static void setSplitSizeThreshold() {
        sizeThreshold = SIZE_THRESHOLD;
        if (splitSize != 0) {
            sizeThreshold = splitSize;
            final String strFeedback = String.format("A custom split size threshold value has been provided %s and will be used which will ignore default value of %s bytes...", splitSize, SIZE_THRESHOLD);
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor empty
     */
    protected JsonSplit() {
        // intentionally blank
    }
}

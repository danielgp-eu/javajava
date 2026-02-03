package interactive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import archive.ArchivingClass;
import environment.EnvironmentCapturingAssembleClass;
import file.FileDeletingClass;
import file.FileOperationsClass;
import file.FileStatisticsClass;
import json.JsonOperationsClass;
import log.LogExposureClass;
import picocli.CommandLine;
import project.ProjectClass;
import shell.ShellingClass;
import structure.ListAndMapClass;
import structure.NumberClass;
import time.TimingClass;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            AnlyzePom.class,
            ArchiveFolders.class,
            CaptureImportsFromJavaSourceFilesIntoCsv.class,
            CaptureWindowsApplicationsInstalledIntoCsv.class,
            ChecksumsForFilesWithinFolder.class,
            CleanOlderFilesFromFolder.class,
            GetSubFoldersFromFolder.class,
            JsonSplit.class,
            LogEnvironmentDetails.class
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
@CommandLine.Command(name = "AnalyzeColumnsFromCsvFile", description = "Analyze columns from CSV file")
class AnalyzeColumnsFromCsvFile implements Runnable {

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
                List.of("TIMESTAMP_LTZ", "TIMESTAMP_NTZ", "TIMESTAMP_TZ"), "COMPOSITE__TIMESTAMP",
                List.of("BINARY", "TEXT", "VARCHAR"), "COMPOSITE__TEXT"
        );
        final Map<String, List<String>> grpCols = ListAndMapClass.mergeKeys(groupedColumns, mergeRules);
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
            final Map<String, Long> sorted = ListAndMapClass.getWordCounts(valList, "(_| )");
            FileOperationsClass.ContentWritingClass.writeLinkedHashMapToCsvFile(strFileName.replace(".csv", "__words.csv"), "DataType,Word,Occurrences", sorted);
        });
    }

    @Override
    public void run() {
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_dev.csv", 3, 4);
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_qa.csv", 3, 4);
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_prod.csv", 3, 4);
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
@CommandLine.Command(name = "AnlyzePom",
                     description = "Exposes information about a given POM")
class AnlyzePom implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fNm", "--fileName"},
            description = "POM file(s) to analyze and expose information from",
            arity = "1..*",
            required = true)
    private String[] strFileNames;

    @Override
    public void run() {
        final String strFeedbackThis = String.format("For this project relevant POM information is: {%s}", ProjectClass.Application.getApplicationDetails());
        LogExposureClass.LOGGER.info(strFeedbackThis);
        for (final String strFileName : strFileNames) {
            ProjectClass.setExternalPomFile(strFileName);
            ProjectClass.loadProjectModel();
            final String strFeedback = String.format("For given POM file %s relevant information is: {%s}", strFileName, ProjectClass.Application.getApplicationDetails());
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Private constructor to prevent instantiation
     */
    public AnlyzePom() {
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
     * String for FolderName
     */
    @CommandLine.Option(
            names = {CommonInteractiveClass.FOLDER_CMD_SHORT, CommonInteractiveClass.FOLDER_CMD_LONG},
            description = CommonInteractiveClass.FOLDER_DESC,
            arity = CommonInteractiveClass.ARITY_ONE_OR_MORE,
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
        if (strArchivingExec != null) {
            ArchivingClass.setArchivingExecutable(strArchivingExec);
        }
        ArchivingClass.setArchivePrefix(strArchivePrefix);
        ArchivingClass.setArchiveSuffix(strArchiveSuffix);
        if (strArchivePwd != null) {
            ArchivingClass.setArchivePwd(strArchivePwd);
        }
        for (final String strFolder : strFolderNames) {
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
@CommandLine.Command(name = "CaptureWindowsApplicationsInstalledIntoCsv",
                     description = "Run the experimental new feature")
class CaptureWindowsApplicationsInstalledIntoCsv implements Runnable {

    @Override
    public void run() {
        ShellingClass.PowerShellExecutionClass.captureWindowsApplicationsIntoCsvFile();
    }

    /**
     * Constructor
     */
    protected CaptureWindowsApplicationsInstalledIntoCsv() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "CaptureImportsFromJavaSourceFilesIntoCsv",
                     description = "Get import inventory from all Java source files within a given folder")
class CaptureImportsFromJavaSourceFilesIntoCsv implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {CommonInteractiveClass.FOLDER_CMD_SHORT, CommonInteractiveClass.FOLDER_CMD_LONG},
            description = CommonInteractiveClass.FOLDER_DESC,
            arity = CommonInteractiveClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strFolderNames;

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-csv", "--csvFileName"},
            description = "CSV file to store retrieved imports into",
            arity = "1",
            required = true)
    private String strCsvFileName;

    @Override
    public void run() {
        for (final String strFolder : strFolderNames) {
            FileOperationsClass.ContentReadingClass.extractImportStatementsFromJavaSourceFilesIntoCsvFile(Path.of(strFolder), Path.of(strCsvFileName));
        }
    }

    /**
     * Constructor
     */
    protected CaptureImportsFromJavaSourceFilesIntoCsv() {
        super();
    }
}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ChecksumsForFilesWithinFolder",
                     description = "Get statistics for all files within a given folder")
class ChecksumsForFilesWithinFolder implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {CommonInteractiveClass.FOLDER_CMD_SHORT, CommonInteractiveClass.FOLDER_CMD_LONG},
            description = CommonInteractiveClass.FOLDER_DESC,
            arity = CommonInteractiveClass.ARITY_ONE_OR_MORE,
            required = true)
    private String[] strFolderNames;

    @Override
    public void run() {
        final String[] inAlgorithms = {"SHA-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        for (final String strFolder : strFolderNames) {
            final LocalDateTime startComputeTime = LocalDateTime.now();
            final Map<String, Map<String, String>> fileStats = FileStatisticsClass.getFileStatisticsFromFolder(strFolder);
            final Duration objDuration = Duration.between(startComputeTime, LocalDateTime.now());
            final String strFeedback = String.format("For the folder %s calculated checksums are %s operation completed in %s (which means %s | %s)", strFolder, fileStats.toString(), objDuration.toString(), TimingClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime"), TimingClass.convertNanosecondsIntoSomething(objDuration, "TimeClock"));
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected ChecksumsForFilesWithinFolder() {
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
     * String for FolderName
     */
    @CommandLine.Option(
            names = {CommonInteractiveClass.FOLDER_CMD_SHORT, CommonInteractiveClass.FOLDER_CMD_LONG},
            description = CommonInteractiveClass.FOLDER_DESC,
            arity = CommonInteractiveClass.ARITY_ONE_OR_MORE,
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
        FileDeletingClass.OlderClass.setCleanedFolderStatistics(true);
        for (final String strFolder : strFolderNames) {
            FileDeletingClass.OlderClass.setOrResetCleanedFolderStatistics();
            FileDeletingClass.OlderClass.deleteFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
            final Map<String, Long> statsClndFldr = FileDeletingClass.OlderClass.getCleanedFolderStatistics();
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
     * String for file name
     */
    @CommandLine.Option(
            names = {"-file", "--fileName"},
            description = "File Name to be split",
            arity = "1",
            required = true)
    private static String strFileName;
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
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-bl", "--bucketLength"},
            description = "Length of final characters to be overwritten as part of the bucketing logic (use -1 for no bucketing)")
    private static int bucketLength;

    @Override
    public void run() {
        setFileSize();
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
                performJsonSplit();
            }
        }
    }

    private static void performJsonSplit() {
        final String strFeedback = String.format("File %s has a size of %s bytes which compared to split file threshold of %s bytes is %s%% bigger, hence split IS required and will be performed!", strFileName, fileSize, sizeThreshold, Math.abs(sizeDifference));
        LogExposureClass.LOGGER.info(strFeedback);
        JsonOperationsClass.JsonArrayClass.setInputJsonFile(strFileName);
        JsonOperationsClass.JsonArrayClass.setDestinationFolder(strDestFolder);
        JsonOperationsClass.JsonArrayClass.setRelevantField("ProjectID");
        if (bucketLength != 0) {
            JsonOperationsClass.JsonArrayClass.setBucketLength(bucketLength);
        }
        final String destPattern = JsonOperationsClass.JsonArrayClass.buildDestinationFileName("x").replaceAll("x.json", ".*.json");
        FileDeletingClass.deleteFilesMatchingPatternFromFolder(strDestFolder, destPattern); // clean slate to avoid inheriting old content
        JsonOperationsClass.JsonArrayClass.splitJsonIntoSmallerGrouped(); // actual logic
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSize() {
        fileSize = FileStatisticsClass.RetrievingClass.getFileSizeIfFileExistsAndIsReadable(strFileName);
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSizeDifferenceCompareToThreshold() {
        final float sizePercentage = NumberClass.computePercentageSafely(fileSize, sizeThreshold);
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

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "LogEnvironmentDetails",
                     description = "Captures execution environment details into Log file")
class LogEnvironmentDetails implements Runnable {

    @Override
    public void run() {
        final String strFeedback = EnvironmentCapturingAssembleClass.getCurrentEnvironmentDetails();
        LogExposureClass.LOGGER.info(strFeedback);
    }

    /**
     * Private constructor to prevent instantiation
     */
    public LogEnvironmentDetails() {
        super();
    }

}

package interactive;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import archive.ArchivingClass;
import file.FileOperationsClass;
import log.LogExposureClass;
import picocli.CommandLine;
import shell.PowerShellExecutionClass;
import time.TimingClass;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        ArchiveFolders.class,
        CaptureImportsFromJavaSourceFilesIntoCsv.class,
        CaptureWindowsApplicationsInstalledIntoCsv.class,
        ChecksumsForFilesWithinFolder.class,
        CleanOlderFilesFromFolder.class
    }
)
public final class BackupAndCleaningClass {

    /**
     * Constructor empty
     */
    private BackupAndCleaningClass() {
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
        final int iExitCode = new CommandLine(new BackupAndCleaningClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
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
            final Properties folderProps = FileOperationsClass.FolderStatisticsClass.getFolderStatisticsRecursive(strFolder, propFolder);
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
        PowerShellExecutionClass.captureWindowsApplicationsIntoCsvFile();
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
        FileOperationsClass.FolderStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        for (final String strFolder : strFolderNames) {
            final LocalDateTime startComputeTime = LocalDateTime.now();
            final Map<String, Map<String, String>> fileStats = FileOperationsClass.FolderStatisticsClass.getFileStatisticsFromFolder(strFolder);
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
        FileOperationsClass.DeletingOlderClass.setCleanedFolderStatistics(true);
        for (final String strFolder : strFolderNames) {
            FileOperationsClass.DeletingOlderClass.setOrResetCleanedFolderStatistics();
            FileOperationsClass.DeletingOlderClass.deleteFilesOlderThanGivenDays(strFolder, intDaysOlderLimit);
            final Map<String, Long> statsClndFldr = FileOperationsClass.DeletingOlderClass.getCleanedFolderStatistics();
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
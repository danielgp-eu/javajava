package interactive;

import java.util.Properties;

import file.FileHandlingClass;
import file.FileLocatingClass;
import picocli.CommandLine;
import shell.ArchivingClass;
import shell.ShellingClass;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        ArchiveFolders.class,
        CaptureWindowsApplicationsInstalledIntoCsv.class,
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
            arity = "1..*",
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
        ArchivingClass.setArchivingExecutable(strArchivingExec);
        ArchivingClass.setArchivePrefix(strArchivePrefix);
        ArchivingClass.setArchiveSuffix(strArchiveSuffix);
        if (strArchivePwd != null) {
            ArchivingClass.setArchivePwd(strArchivePwd);
        }
        for (final String strFolder : strFolderNames) {
            propFolder.clear();
            final Properties folderProps = FileLocatingClass.getFolderStatisticsRecursive(strFolder, propFolder);
            ArchivingClass.setArchivingDir(strFolder);
            ArchivingClass.setArchiveNameWithinDestinationFolder(strDestFolder);
            ArchivingClass.archiveFolderAs7zUltra();
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
        ShellingClass.captureWindowsApplicationsIntoCsvFile();
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
@CommandLine.Command(name = "CleanOlderFilesFromFolder",
                     description = "Clean files older than a given number of days")
class CleanOlderFilesFromFolder implements Runnable {

    /**
     * String for FolderName
     */
    @CommandLine.Option(
            names = {"-fldNm", "--folderName"},
            description = "Folder Name to be inspected",
            arity = "1..*",
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
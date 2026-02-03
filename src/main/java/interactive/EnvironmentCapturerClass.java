package interactive;

import environment.EnvironmentCapturingAssembleClass;
import file.FileOperationsClass;
import log.LogExposureClass;
import picocli.CommandLine;
import project.ProjectClass;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            AnlyzePom.class,
            GetSubFoldersFromFolder.class,
            LogEnvironmentDetails.class
    }
)
public final class EnvironmentCapturerClass {

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
        final int iExitCode = new CommandLine(new EnvironmentCapturerClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

    /**
     * Constructor empty
     */
    private EnvironmentCapturerClass() {
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
        FileOperationsClass.RetrievingClass.getSubFoldersFromFolder(strFolderName);
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

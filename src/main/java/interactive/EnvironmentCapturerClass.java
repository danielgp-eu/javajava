package interactive;

import environment.EnvironmentCapturingAssembleClass;
import file.FileHandlingClass;
import log.LogExposureClass;
import picocli.CommandLine;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
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
@CommandLine.Command(name = "GetSubFoldersFromFolder",
                     description = "Captures sub-folders from a Given Folder into Log file")
class GetSubFoldersFromFolder implements Runnable {

    @Override
    public void run() {
        FileHandlingClass.getSubFoldersFromFolder("C:\\www\\Config\\");
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

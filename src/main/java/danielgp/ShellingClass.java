package danielgp;
/* I/O classes */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/* Time classes */
import java.time.LocalDateTime;
/* LOGGing classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(ShellingClass.class);
    /**
     * holding the Use account currently logged on
     */
    public static String LOGGED_ACCOUNT = null; // NOPMD by Daniel Popiniuc on 17.04.2025, 16:26

    /**
     * Building Process for shell execution
     * 
     * @param strCommand
     * @param strParameters
     */
    private static ProcessBuilder buildProcessForExecution(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        return builder;
    }

    /**
     * capture Process output
     * 
     * @param process
     * @param strOutLineSep
     * @return
     * @throws IOException
     */
    private static String captureProcessOutput(final Process process, final String strOutLineSep) throws IOException {
        final String strReturn;
        final StringBuilder processOutput = new StringBuilder();
        try (BufferedReader processOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String readLine;
            while ((readLine = processOutReader.readLine()) != null) {
                processOutput.append(readLine).append(strOutLineSep);
            }
            strReturn = processOutput.toString();
        }
        return strReturn;
    }

    /**
     * Executes a shells command without any output captured
     * 
     * @param strCommand
     * @param strParameters
     */
    public static void executeShellUtility(final String strCommand, final String strParameters) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            String strFeedback = String.format("I intend to execute following command %s w/o output captured!", builder.command().toString());
            LOGGER.debug(strFeedback);
            final Process process = builder.start();
            final int exitCode = process.waitFor();
            process.destroy();
            strFeedback = String.format("Process execution finished with exit code %d", exitCode);
            LOGGER.debug(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format("Process Execution failed: %s", e.getStackTrace().toString()); 
            LOGGER.error(strFeedback);
        }
        TimingClass.logDuration(startTimeStamp, "Shell execution w/o output captured completed", "debug");
    }

    /**
     * Executes a shells command with capturing the output to a String
     * 
     * @param strCommand
     * @param strParameters
     * @param strOutLineSep
     * @return String
     */
    public static String executeShellUtility(final String strCommand, final String strParameters, final String strOutLineSep) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        String strReturn = "";
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            String strFeedback = String.format("I intend to execute following command %s WITH output captured!", builder.command().toString());
            LOGGER.debug(strFeedback);
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            strReturn = captureProcessOutput(process, strOutLineSep);
            final int exitCode = process.waitFor();
            process.destroy();
            strFeedback = String.format("Process execution finished with exit code %d", exitCode);
            LOGGER.debug(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format("Process Execution failed: %s", e.getStackTrace().toString()); 
            LOGGER.error(strFeedback);
        }
        TimingClass.logDuration(startTimeStamp, "Shell execution WITH output captured completed", "debug");
        return strReturn;
    }

    /**
     * Getting current logged account name
     * 
     * @return
     */
    public static String getCurrentUserAccount() {
        if (LOGGED_ACCOUNT == null) {
            loadCurrentUserAccount();
        }
        return LOGGED_ACCOUNT;
    }

    /**
     * load current logged account name
     * 
     * @return
     */
    private static void loadCurrentUserAccount() {
        String strUser = executeShellUtility("WHOAMI", "/UPN", "");
        if (strUser.startsWith("ERROR: Unable to get User Principal Name (UPN)")) {
            strUser = executeShellUtility("WHOAMI", "", "");
        }
        LOGGED_ACCOUNT = strUser;
    }

    // Private constructor to prevent instantiation
    private ShellingClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

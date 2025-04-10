package danielgp;
/* I/O classes */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * holding the Use account currently logged on
     */
    public static String LOGGED_ACCOUNT;


    /**
     * Executes a shells command without any output captured
     * 
     * @param strCommand
     * @param strParameters
     */
    public static void executeShellUtility(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        try {
            String strFeedback = String.format("I intend to execute following command %s", builder.command().toString());
            LogHandlingClass.LOGGER.info(strFeedback);
            final Process process = builder.start();
            final int exitCode = process.waitFor();
            process.destroy();
            strFeedback = String.format("Process execution finished with error code %s", exitCode);
            LogHandlingClass.LOGGER.info(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format("Error encountered on shell execution: %s", e.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
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
        String strReturn = "";
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        builder.redirectErrorStream(true);
        try {
            String strFeedback = String.format("I intend to execute following command %s", builder.command().toString());
            LogHandlingClass.LOGGER.info(strFeedback);
            final Process process = builder.start();
            final StringBuilder processOutput = new StringBuilder();
            try (BufferedReader processOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String readLine;
                while ((readLine = processOutReader.readLine()) != null) {
                    processOutput.append(readLine).append(strOutLineSep);
                }
                strReturn = processOutput.toString();
            }
            final int exitCode = process.waitFor();
            process.destroy();
            strFeedback = String.format("Process execution finished with error code %s", exitCode);
            LogHandlingClass.LOGGER.info(strFeedback);
        } catch (IOException | InterruptedException e) {
            final String strFeedback = String.format("Interrupted Execution: %s", e.getStackTrace().toString()); 
            LogHandlingClass.LOGGER.error(strFeedback);
        }
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

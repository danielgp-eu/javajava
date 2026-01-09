package shell;

import file.FileHandlingClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Shell execution methods
 */
public final class ShellingClass {

    /**
     * Building Process for shell execution
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    private static ProcessBuilder buildProcessForExecution(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = new ProcessBuilder();
        if (strParameters.isEmpty()) {
            builder.command(strCommand);
        } else {
            builder.command(strCommand, strParameters);
        }
        LogExposure.exposeProcessBuilder(builder.command().toString());
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        return builder;
    }

    /**
     * capture Process output
     *
     * @param process process in scope
     * @param strOutLineSep line separator for the output
     * @return String
     */
    private static String captureProcessOutput(final Process process, final String strOutLineSep) {
        String strReturn = null;
        final StringBuilder processOutput = new StringBuilder();
        try (BufferedReader processOutReader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            processOutReader.lines().forEach(strCrtLine -> processOutput.append(strCrtLine)
                    .append(strOutLineSep));
            strReturn = processOutput.toString();
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionCaptureFailure"), Arrays.toString(ex.getStackTrace())));
        }
        return strReturn;
    }

    /**
     * Executes a shells command with/without output captured
     * @param builder ProcessBuilder
     * @param strOutLineSep line separator for the output
     * @return String
     */
    public static String executeShell(final ProcessBuilder builder, final String strOutLineSep) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        LogExposure.exposeProcessBuilder(builder.command().toString());
        String strReturn = "";
        try {
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            if (!strOutLineSep.isBlank()) {
                strReturn = captureProcessOutput(process, strOutLineSep);
            }
            final int exitCode = process.waitFor();
            process.destroy();
            LogExposure.exposeProcessExecutionCompletion(strOutLineSep, startTimeStamp, exitCode);
        } catch (IOException ex) {
        	LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionFailed"), Arrays.toString(ex.getStackTrace())));
        } catch(InterruptedException ei) {
            LogExposure.exposeExecutionInterrupedLoggedToError(Arrays.toString(ei.getStackTrace()));
            throw (IllegalStateException)new IllegalStateException().initCause(ei);
        }
        return strReturn;
    }

    /**
     * Executes a shells command without any output captured
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    public static void executeShellUtility(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        executeShell(builder, " ");
    }

    /**
     * Executes a shells command with capturing the output to a String
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     * @param strOutLineSep line separator for the output
     * @return String
     */
    public static String executeShellUtility(final String strCommand, final String strParameters, final String strOutLineSep) {
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        return executeShell(builder, strOutLineSep);
    }

    /**
     * Getting current logged account name
     * @return String
     */
    public static String getCurrentUserAccount() {
        String strUser = executeShellUtility("WHOAMI", "/UPN", "");
        if (strUser.startsWith("ERROR:")) {
            LogExposure.exposeMessageToErrorLog(JavaJavaLocalizationClass.getMessage("i18nUserPrincipalNameError"));
            strUser = executeShellUtility("WHOAMI", "", "");
        }
        return strUser;
    }

    /**
     * Constructor
     */
    private ShellingClass() {
        // intentionally left blank
    }
}

package javajava;

import org.apache.logging.log4j.Level;

import file.FileHandlingClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * holding the Use account currently logged on
     */
    public static String loggedAccount;

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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCommandIntention"), builder.command().toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
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
        try (BufferedReader processOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            processOutReader.lines().forEach(strCrtLine -> {
                processOutput.append(strCrtLine)
                        .append(strOutLineSep);
            });
            strReturn = processOutput.toString();
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCaptureFailure"), Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
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
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            final Process process = builder.start();
            final int exitCode = process.waitFor();
            process.destroy();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFinished"), exitCode);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFailed"), Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        } catch(InterruptedException ei) {
            setExecutionInterrupedLoggedToError(Arrays.toString(ei.getStackTrace()));
            throw (IllegalStateException)new IllegalStateException().initCause(ei);
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = TimingClass.logDuration(startTimeStamp, JavaJavaLocalization.getMessage("i18nProcessExecutionWithoutCaptureCompleted"));
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
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
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        String strReturn = "";
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        try {
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            strReturn = captureProcessOutput(process, strOutLineSep);
            final int exitCode = process.waitFor();
            process.destroy();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFinished"), exitCode);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFailed"), Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        } catch(InterruptedException ei) {
            setExecutionInterrupedLoggedToError(Arrays.toString(ei.getStackTrace()));
            throw (IllegalStateException)new IllegalStateException().initCause(ei);
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = TimingClass.logDuration(startTimeStamp, JavaJavaLocalization.getMessage("i18nProcessExecutionWithCaptureCompleted"));
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return strReturn;
    }

    /**
     * Getting current logged account name
     * @return String
     */
    public static String getCurrentUserAccount() {
        if (loggedAccount == null) {
            loadCurrentUserAccount();
        }
        return loggedAccount;
    }

    /**
     * load current logged account name
     */
    private static void loadCurrentUserAccount() {
        String strUser = executeShellUtility("WHOAMI", "/UPN", "");
        if (strUser.startsWith("ERROR:")) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nUserPrincipalNameError");
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.ERROR)) {
                LoggerLevelProvider.LOGGER.warn(strFeedback);
            }
            strUser = executeShellUtility("WHOAMI", "", "");
        }
        loggedAccount = strUser;
    }

    /**
     * Execution Interrupted details captured to Error log
     * @param strTraceDetails details
     */
    private static void setExecutionInterrupedLoggedToError(final String strTraceDetails) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nAppInterruptedExecution"), strTraceDetails);
            LoggerLevelProvider.LOGGER.error(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private ShellingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

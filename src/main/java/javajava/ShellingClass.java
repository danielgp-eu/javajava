package javajava;

import org.apache.logging.log4j.Level;
import org.apache.maven.shared.utils.StringUtils;

import file.FileHandlingClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
            processOutReader.lines().forEach(strCrtLine -> processOutput.append(strCrtLine)
                    .append(strOutLineSep));
            strReturn = processOutput.toString();
        } catch (IOException ex) {
            Common.setInputOutputExecutionLoggedToError(JavaJavaLocalization.getMessage("i18nProcessExecutionCaptureFailure"),
                    Arrays.toString(ex.getStackTrace()));
        }
        return strReturn;
    }

    /**
     * build Archive name with optional Suffix and Prefix
     * @param strPrefix archive prefix
     * @param strName archive name
     * @param strSuffix archive suffix
     * @return String
     */
    public static String buildArchivingName(final String strPrefix, final String strName, final String strSuffix) {
        final StringBuilder strArchiveName = new StringBuilder();
        if (strPrefix != null) {
            strArchiveName.append(strPrefix);
        }
        strArchiveName.append(strName);
        if (strSuffix != null) {
            strArchiveName.append(strSuffix);
        }
        strArchiveName.append(".7z");
        return strArchiveName.toString();
    }

    /**
     * Archive folder content as 7z using Ultra compression level
     * @param strArchivingExec archiving executable
     * @param strFolder folder to archive
     * @param strArchiveName archive name
     */
    public static void archiveFolderAs7zUltra(final String strArchivingExec, 
            final String strFolder, 
            final String strArchiveName, 
            final String strArchivePwd) {
        final String strArchiveDir = "-ir!" + StringUtils.stripEnd(strFolder, File.separator) + File.separator + "*";
        final ProcessBuilder builder; 
        if (strArchivePwd == null) {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchiveDir, "-mx9", "-ms4g", "-ms4g", "-mmt=on");
        } else {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchiveDir, "-mx9", "-ms4g", "-ms4g", "-mmt=on", "-p" + strArchivePwd);
        }
        executeShell(builder, " ");
    }

    /**
     * Executes a shells command with output captured
     * @param builder ProcessBuilder
     * @param strOutLineSep line separator for the output
     * @return String
     */
    private static String executeShell(final ProcessBuilder builder, final String strOutLineSep) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCommandIntention"), builder.command().toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        String strReturn = "";
        try {
            builder.redirectErrorStream(true);
            final Process process = builder.start();
            if (!strOutLineSep.isBlank()) {
                strReturn = captureProcessOutput(process, strOutLineSep);
            }
            final int exitCode = process.waitFor();
            process.destroy();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionFinished"), exitCode);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (IOException e) {
            Common.setInputOutputExecutionLoggedToError(JavaJavaLocalization.getMessage("i18nProcessExecutionFailed"),
                    Arrays.toString(e.getStackTrace()));
        } catch(InterruptedException ei) {
            Common.setExecutionInterrupedLoggedToError(Arrays.toString(ei.getStackTrace()));
            throw (IllegalStateException)new IllegalStateException().initCause(ei);
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            String strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
            if (strOutLineSep.isBlank()) {
                strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
            }
            final String strFeedback = TimingClass.logDuration(startTimeStamp, JavaJavaLocalization.getMessage(strCaptureMessage));
            LoggerLevelProvider.LOGGER.debug(strFeedback);
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
            final String strFeedback = JavaJavaLocalization.getMessage("i18nUserPrincipalNameError");
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.ERROR)) {
                LoggerLevelProvider.LOGGER.warn(strFeedback);
            }
            strUser = executeShellUtility("WHOAMI", "", "");
        }
        return strUser;
    }

    /**
     * Constructor
     */
    private ShellingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

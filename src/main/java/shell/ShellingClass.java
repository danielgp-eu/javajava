package shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import file.FileHandlingClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import time.TimingClass;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * Process Capture Need
     */
    private static boolean needProcCapture;
    /**
     * Timestamp started
     */
    private static LocalDateTime startTimestamp;
    /**
     * Process error
     */
    private static String strProcErr;
    /**
     * Process standard output
     */
    private static String strProcOut;

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
        LogExposureClass.exposeProcessBuilder(builder.command().toString());
        builder.directory(FileHandlingClass.getCurrentUserFolder());
        return builder;
    }

    /**
     * Executes a shells command with/without output captured
     * @param builder ProcessBuilder
     * @param strOutLineSep line separator for the output
     */
    public static void executeShell(final ProcessBuilder builder, final String strOutLineSep) {
        startTimestamp = LocalDateTime.now();
        LogExposureClass.exposeProcessBuilder(builder.command().toString());
        try {
            final Process process = builder.start();
            // Read stdout and stderr asynchronously with CompletableFuture
            final CompletableFuture<String> stdoutFuture = CompletableFuture.supplyAsync(() ->
                getStandardReaderIntoString(process.inputReader(), strOutLineSep) // inputReader() = stdout
            );
            final CompletableFuture<String> stderrFuture = CompletableFuture.supplyAsync(() ->
                getStandardReaderIntoString(process.errorReader(), strOutLineSep) // errorReader() = stderr
            );
            final int exitCode = process.waitFor();
            CompletableFuture.allOf(stdoutFuture, stderrFuture).join();
            setProcessResults(stdoutFuture, stderrFuture, exitCode);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionFailed"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        } catch(InterruptedException ei) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nAppInterruptedExecution"), Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Executes a shells command without any output captured
     *
     * @param strCommand command to execute
     * @param strParameters command parameters
     */
    public static void executeShellUtility(final String strCommand, final String strParameters) {
        final ProcessBuilder builder = buildProcessForExecution(strCommand, strParameters);
        setProcessCaptureNeed(false);
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
        setProcessCaptureNeed(true);
        executeShell(builder, strOutLineSep);
        return strProcOut;
    }

    /**
     * Getting current logged account name
     * @return String
     */
    public static String getCurrentUserAccount() {
        setProcessCaptureNeed(true);
        executeShellUtility("WHOAMI", "/UPN", "");
        String strUser = strProcOut;
        if (strUser.startsWith("ERROR:")) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nUserPrincipalNameError");
            LogExposureClass.LOGGER.error(strFeedback);
            executeShellUtility("WHOAMI", "", "");
            strUser = strProcOut;
        }
        return strUser;
    }

    /**
     * Getter for Process Output
     * @return String Process Output content
     */
    public static String getProcessError() {
        return strProcErr;
    }

    /**
     * Getter for Process Output
     * @return String Process Output content
     */
    public static String getProcessOutput() {
        return strProcOut;
    }

    /**
     * Setter for Process output and error
     * @param stdoutFuture Process output
     * @param stderrFuture Process error
     * @param exitCode process execution exit code
     */
    private static void setProcessResults(
            final CompletableFuture<String> stdoutFuture,
            final CompletableFuture<String> stderrFuture,
            final int exitCode) {
        final String strCaptureMessage;
        if (needProcCapture) {
            try {
                strProcOut = stdoutFuture.get();
                strProcErr = stderrFuture.get();
            } catch (InterruptedException ei) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nAppInterruptedExecution"), Arrays.toString(ei.getStackTrace()));
                LogExposureClass.LOGGER.warn(strFeedback);
                /* Clean up whatever needs to be handled before interrupting  */
                Thread.currentThread().interrupt();
            } catch (ExecutionException ee) {
                final String strFeedback = String.format("Execution exception tracing %s", Arrays.toString(ee.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
                throw (IllegalStateException)new IllegalStateException().initCause(ee);
            }
            strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
        } else {
            strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
        }
        final String strFeedback = TimingClass.logDuration(startTimestamp,
                String.format(JavaJavaLocalizationClass.getMessage(strCaptureMessage), exitCode));
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * collect Standard Reader into String
     * @param reader BufferedReader content
     * @param strOutLineSep line separators
     * @return String
     */
    private static String getStandardReaderIntoString(final BufferedReader reader, final String strOutLineSep) {
        return reader.lines().collect(Collectors.joining(strOutLineSep)).trim();
    }

    /**
     * Setter for Process Capture
     * @param inProcCapture boolean
     */
    public static void setProcessCaptureNeed(final boolean inProcCapture) {
        needProcCapture = inProcCapture;
    }

    /**
     * Constructor
     */
    private ShellingClass() {
        // intentionally left blank
    }

}

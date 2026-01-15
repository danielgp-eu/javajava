package shell;

import file.FileHandlingClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import time.TimingClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Shell execution methods
 */
public final class ShellingClass {
    /**
     * Process Capture Need
     */
    private static boolean needProcCapture;
    /**
     * Windows OS string
     */
    private static String STR_OS_WIN = "Windows";
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
     * Capture Windows installed application into a CSV file 
     */
    public static void captureWindowsApplicationsIntoCsvFile() {
        final String crtOperatingSys = System.getProperty("os.name");
        if (crtOperatingSys.startsWith(STR_OS_WIN)) {
            final String[] arrayCommand = {"powershell.exe", "-Command", "\"Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | Select-Object Publisher, DisplayName, DisplayVersion, EngineVersion, InstallDate, EstimatedSize, URLInfoAbout | Export-Csv -Encoding utf8 -Path WindowsApps.csv -UseCulture -NoTypeInformation -Force\""};
            final ProcessBuilder builder = new ProcessBuilder(arrayCommand);
            setProcessCaptureNeed(false);
            executeShell(builder, System.lineSeparator());
        }
    }

    /**
     * Executes a shells command with/without output captured
     * @param builder ProcessBuilder
     * @param strOutLineSep line separator for the output
     */
    public static void executeShell(final ProcessBuilder builder, final String strOutLineSep) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        LogExposureClass.exposeProcessBuilder(builder.command().toString());
        String strReturn = "";
        try {
            // builder.redirectErrorStream(true);
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
            final String strCaptureMessage;
            if (needProcCapture) {
                try {
                    strProcOut = stdoutFuture.get();
                } catch (ExecutionException e) {
                    final String strFeedback = String.format("Execution exception tracing %s", Arrays.toString(e.getStackTrace()));
                    LogExposureClass.LOGGER.error(strFeedback);
                    throw (IllegalStateException)new IllegalStateException().initCause(e);
                }
                strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
            } else {
                strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
            }
            final String strFeedback = TimingClass.logDuration(startTimeStamp,
                    String.format(JavaJavaLocalizationClass.getMessage(strCaptureMessage),
                            exitCode));
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionFailed"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        } catch(InterruptedException ei) {
            final String strFeedback = String.format("Interrupted exception tracing %s", Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
            throw (IllegalStateException)new IllegalStateException().initCause(ei);
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
    public static String getProcessOutput() {
        return strProcOut;
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

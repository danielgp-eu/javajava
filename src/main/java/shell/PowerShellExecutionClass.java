package shell;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import file.ProjectClass;
import log.LogExposureClass;

/**
 * Power Shell execution
 */
public final class PowerShellExecutionClass {
    /**
     * Power Shell file
     */
    private static String psPath;

    private static String[] buildWindowsApplicationCommandSafely() {
        final String userHome = System.getProperty("user.home");
        final String strCmd = String.format(
            "Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | " +
            "Select-Object Publisher, DisplayName, DisplayVersion, EngineVersion, InstallDate, EstimatedSize, URLInfoAbout | " +
            "Export-Csv -Encoding utf8 -Path '%s\\WindowsApps.csv' -UseCulture -NoTypeInformation -Force",
            userHome
        );
        final String[] arrayCommand = { psPath, "-Command", strCmd };
        final String strFeedback = String.format("PowerShell command to be executed is: %s", Arrays.toString(arrayCommand));
        LogExposureClass.LOGGER.debug(strFeedback);
        return arrayCommand;
    }

    /**
     * Capture Windows installed application into a CSV file 
     */
    public static void captureWindowsApplicationsIntoCsvFile() {
        final String crtOperatingSys = System.getProperty("os.name");
        if (crtOperatingSys.startsWith("Windows")) {
            try {
                final String[] varsToPick = {"osWindowsSystem32Path", "powerShellBinary"};
                final Properties svProperties = ProjectClass.getVariableFromProjectProperties(varsToPick);
                setPowerShellFile(svProperties.get("powerShellBinary").toString());
                validatePathEnvironmentVariable();
                final String[] arrayCommand = buildWindowsApplicationCommandSafely();
                final ProcessBuilder builder = new ProcessBuilder(arrayCommand);
                builder.directory(new File(svProperties.get("osWindowsSystem32Path").toString()));
                ShellingClass.setProcessCaptureNeed(false);
                ShellingClass.executeShell(builder, System.lineSeparator());
            } catch (SecurityException se) {
                final String strFeedback = String.format("Security violation:  %s", Arrays.toString(se.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Validate that the executable exists and is not writable by non-admin users
     * @param psBinary Power Shell binary
     */
    private static void setPowerShellFile(final String psBinary) {
        final File psFileLocal = new File(psBinary);
        if (psFileLocal.exists() || psFileLocal.canExecute()) {
            psPath = psBinary;
        } else {
            final String strFeedback = String.format("Security violation: PowerShell executable not found or not executable: %s...", psBinary);
            LogExposureClass.LOGGER.error(strFeedback);
            throw new SecurityException(strFeedback);
        }
    }

    /**
     * Validate PATH environment variable (optional hardening)
     */
    private static void validatePathEnvironmentVariable() {
        final String pathEnv = System.getenv().get("PATH");
        if (pathEnv != null) {
            final String[] arraysPathFolders = pathEnv.split(";");
            final List<String> arrayPaths = Arrays.asList(arraysPathFolders);
            arrayPaths.forEach(crtPath -> {
                final File pathDir = new File(crtPath);
                if (pathDir.exists() && pathDir.canWrite()) {
                    final String strFeedback = String.format("Security violation: Writable directory detected in PATH: %s...", crtPath);
                    LogExposureClass.LOGGER.error(strFeedback);
                    throw new SecurityException(strFeedback);
                }
            });
        }
    }

    /**
     * Constructor
     */
    private PowerShellExecutionClass() {
        // intentionally left blank
    }

}

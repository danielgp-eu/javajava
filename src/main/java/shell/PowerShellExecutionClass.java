package shell;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import log.LogExposureClass;

/**
 * Power Shell execution
 */
public final class PowerShellExecutionClass {
    /**
     * Power Shell file
     */
    private static String psPath;
    /**
     * Windows Sys32 path
     */
    private static final String STR_OS_PATH = "C:\\Windows\\System32\\";
    /**
     * Windows OS string
     */
    private static final String STR_OS_WIN = "Windows";
    /**
     * Power Shell path
     */
    private static final String STR_PS_PATH = "WindowsPowerShell\\v1.0\\";

    private static String[] buildWindowsApplicationCommandSafely() {
        final String userHome = System.getProperty("user.home");
        final String strCmd = String.format(
            "Get-ItemProperty HKLM:\\Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\* | " +
            "Select-Object Publisher, DisplayName, DisplayVersion, EngineVersion, InstallDate, EstimatedSize, URLInfoAbout | " +
            "Export-Csv -Encoding utf8 -Path '%s\\WindowsApps.csv' -UseCulture -NoTypeInformation -Force",
            userHome
        );
        final String[] arrayCommand = { psPath, "-Command", strCmd };
        final String strFeedback = String.format("PowerShell command to be executed is: %s", arrayCommand.toString());
        LogExposureClass.LOGGER.debug(strFeedback);
        return arrayCommand;
    }

    /**
     * Capture Windows installed application into a CSV file 
     */
    public static void captureWindowsApplicationsIntoCsvFile() {
        final String crtOperatingSys = System.getProperty("os.name");
        if (crtOperatingSys.startsWith(STR_OS_WIN)) {
            try {
                setPowerShellFile();
                validatePathEnvironmentVariable();
                final String[] arrayCommand = buildWindowsApplicationCommandSafely();
                final ProcessBuilder builder = new ProcessBuilder(arrayCommand);
                builder.directory(new File(STR_OS_PATH));
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
     */
    private static void setPowerShellFile() {
        final String psPathFromPieces = STR_OS_PATH + STR_PS_PATH + "powershell.exe";
        LogExposureClass.LOGGER.debug(psPathFromPieces);
        final File psFileLocal = new File(psPathFromPieces);
        if (psFileLocal.exists() || psFileLocal.canExecute()) {
            psPath = psPathFromPieces;
        } else {
            throw new SecurityException("PowerShell executable not found or not executable: " + psPath);
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
                    throw new SecurityException("Writable directory detected in PATH: " + crtPath);
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

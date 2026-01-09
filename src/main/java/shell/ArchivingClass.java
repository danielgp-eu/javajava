package shell;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.maven.shared.utils.StringUtils;

import javajava.CommonClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposure;

/**
 * Archiving wrapper
 */
public final class ArchivingClass {
    /**
     * Archive Name variable
     */
    private static String strArchiveName;
    /**
     * Archive Prefix variable
     */
    private static String strArchivePrefix;
    /**
     * Archive Password variable
     */
    private static String strArchivePwd;
    /**
     * Archive Suffix variable
     */
    private static String strArchiveSuffix;
    /**
     * Archive Executable variable
     */
    private static String strArchivingExec;
    /**
     * Archive Folder variable
     */
    private static String strArchivingDir;

    /**
     * Archive folder content as 7z using Ultra compression level
     */
    public static void archiveFolderAs7zUltra() {
        final String strArchDir = "-ir!" + strArchivingDir + "*";
        final ProcessBuilder builder;
        if (strArchivePwd == null) {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchDir, "-mx9", "-ms4g", "-mmt=on");
        } else {
            builder = new ProcessBuilder(strArchivingExec, "a", "-t7z", strArchiveName, strArchDir, "-mx9", "-ms4g", "-mmt=on", "-p" + strArchivePwd);
            LogExposure.exposeProcessBuilder(builder.command().toString().replaceFirst("-p" + strArchivePwd, "**H*I*D*D*E*N**P*A*S*S*W*O*R*D**"));
        }
        ShellingClass.executeShell(builder, " ");
    }

    /**
     * Log Archived content
     * @param folderProps folder Properties
     */
    public static void exposeArchivedStatistics(final Properties folderProps) {
        if (LogExposure.isCurrentLogLevelLessThanWarning()) {
            final File fileA = new File(strArchiveName);
            if (fileA.exists() && fileA.isFile()) {
                final long fileArchSize = fileA.length();
                final long fileOrigSize = Long.parseLong(folderProps.getOrDefault("SIZE_BYTES", "0").toString());
                final float percentage = CommonClass.getPercentageSafely(fileArchSize, fileOrigSize);
                LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFolderStatisticsArchived"),
                        strArchivingDir,
                        folderProps,
                        strArchiveName,
                        fileArchSize,
                        percentage));
            }
        }
    }

    /**
     * Setter for Archive Name
     * @param inArchiveName String
     */
    public static void setArchiveName(final String inArchiveName) {
        final StringBuilder sbArchiveName = new StringBuilder();
        if (strArchivePrefix != null) {
        	sbArchiveName.append(strArchivePrefix);
        }
        sbArchiveName.append(inArchiveName);
        if (strArchiveSuffix != null) {
        	sbArchiveName.append(strArchiveSuffix);
        }
        sbArchiveName.append(".7z");
        strArchiveName = sbArchiveName.toString();
    }

    /**
     * Setter for Archive Name from Folder Name
     * @param inFolderName String
     */
    public static void setArchiveNameFromFolderName(final String inFolderName) {
        final Path path = Paths.get(inFolderName);
        setArchiveName(path.getFileName().toString());
    }

    /**
     * Setter for Archive Name
     * @param inArchivePrefix String
     */
    public static void setArchivePrefix(final String inArchivePrefix) {
        strArchivePrefix = inArchivePrefix;
    }

    /**
     * Setter for Archive Name
     * @param inArchivePwd String
     */
    public static void setArchivePwd(final String inArchivePwd) {
        strArchivePwd = inArchivePwd;
    }

    /**
     * Setter for Archive Suffix
     * @param inArchiveSuffix String
     */
    public static void setArchiveSuffix(final String inArchiveSuffix) {
        strArchiveSuffix = inArchiveSuffix;
    }

    /**
     * Setter for Archive Executable
     * @param inArchivingExec String
     */
    public static void setArchivingExecutable(final String inArchivingExec) {
        strArchivingExec = inArchivingExec;
    }

    /**
     * Setter for Archive Folder
     * @param inArchivingDir String
     */
    public static void setArchivingDir(final String inArchivingDir) {
        strArchivingDir = StringUtils.stripEnd(inArchivingDir, File.separator);
    }

    /**
     * Constructor
     */
    private ArchivingClass() {
        // intentionally blank
    }

}

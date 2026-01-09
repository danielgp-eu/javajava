package shell;

import javajava.CommonClass;
import javajava.LoggerLevelProviderClass;
import javajava.TimingClass;
import localization.JavaJavaLocalizationClass;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * class for exposing shell actions
 */
public final class ShellFeedbackClass {

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
     * Log Archived content
     * @param strArchiveName archive name
     */
    public static void exposeArchivedContent(final String strArchiveName, final String strFolder, final Properties folderProps) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN)) {
            final File fileA = new File(strArchiveName);
            if (fileA.exists() && fileA.isFile()) {
                final long fileArchSize = fileA.length();
                final long fileOrigSize = Long.parseLong(folderProps.getOrDefault("SIZE_BYTES", "0").toString());
                final float percentage = CommonClass.getPercentageSafely(fileArchSize, fileOrigSize);
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFolderStatisticsArchived"),
                        strFolder,
                        folderProps,
                        strArchiveName,
                        fileArchSize,
                        percentage);
                LoggerLevelProviderClass.LOGGER.info(strFeedback);
            }
        }
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    public static void exposeProcessBuilder(final String strCommand) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final boolean bolFeedbackNeeded = !strCommand.contains("7za") || !strCommand.contains(", -p");
            if (bolFeedbackNeeded) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionCommandIntention"), strCommand);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
        }
    }

    /**
     * Log Process Builder execution completion
     * @param strOutLineSep line separator for output
     * @param startTimeStamp starting time for statistics
     */
    public static void exposeProcessExecutionCompletion(final String strOutLineSep,
            final LocalDateTime startTimeStamp,
            final int exitCode) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            String strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
            if (strOutLineSep.isBlank()) {
                strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
            }
            final String strFeedback = TimingClass.logDuration(startTimeStamp,
                String.format(JavaJavaLocalizationClass.getMessage(strCaptureMessage), exitCode));
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private ShellFeedbackClass() {
        // intentionally left blank
    }

}

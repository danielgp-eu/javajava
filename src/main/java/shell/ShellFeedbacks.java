package shell;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Properties;

import org.apache.logging.log4j.Level;

import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;
import javajava.TimingClass;

/**
 * class for exposing shell actions
 */
public class ShellFeedbacks {

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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            final File fileA = new File(strArchiveName);
            if (fileA.exists() && fileA.isFile()) {
                final long fileArchSize = fileA.length();
                final long fileOrigSize = Long.parseLong(folderProps.getOrDefault("SIZE_BYTES", "0").toString());
                double percentage = 0;
                if (fileOrigSize != 0) {
                    final double percentageExact = (double) fileArchSize / fileOrigSize * 100;
                    percentage = new BigDecimal(Double.toString(percentageExact))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                }
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFolderStatisticsArchived"),
                        strFolder,
                        folderProps,
                        strArchiveName,
                        fileArchSize,
                        percentage);
                LoggerLevelProvider.LOGGER.info(strFeedback);
            }
        }
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    protected static void exposeProcessBuilder(final String strCommand) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            boolean bolFeedbackNeeded = !strCommand.contains("7za") || !strCommand.contains(", -p");
            if (bolFeedbackNeeded) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nProcessExecutionCommandIntention"), strCommand);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        }
    }

    /**
     * Log Process Builder execution completion
     * @param strOutLineSep line separator for output
     * @param startTimeStamp starting time for statistics
     */
    protected static void exposeProcessExecutionCompletion(final String strOutLineSep,
            final LocalDateTime startTimeStamp,
            final int exitCode) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            String strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
            if (strOutLineSep.isBlank()) {
                strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
            }
            final String strFeedback = TimingClass.logDuration(startTimeStamp,
                String.format(JavaJavaLocalization.getMessage(strCaptureMessage), exitCode));
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

}

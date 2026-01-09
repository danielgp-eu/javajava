package log;

import java.time.LocalDateTime;

import org.apache.logging.log4j.Level;

import localization.JavaJavaLocalizationClass;
import time.TimingClass;

/**
 * exposing things to Log
 */
public final class LogExposure {

    /**
     * Execution Interrupted details captured to Debug log
     * @param strMsg details
     */
    public static void exposeMessageToDebugLog(final String strMsg) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            LoggerLevelProviderClass.LOGGER.debug(strMsg);
        }
    }

    /**
     * Execution Interrupted details captured to Error log
     * @param strMsg details
     */
    public static void exposeMessageToErrorLog(final String strMsg) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
            LoggerLevelProviderClass.LOGGER.error(strMsg);
        }
    }

    /**
     * Execution Interrupted details captured to Info log
     * @param strMsg details
     */
    public static void exposeMessageToInfoLog(final String strMsg) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN)) {
            LoggerLevelProviderClass.LOGGER.info(strMsg);
        }
    }

    /**
     * Execution Interrupted details captured to Error log
     * @param strTraceDetails details
     */
    public static void exposeExecutionInterrupedLoggedToError(final String strTraceDetails) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nAppInterruptedExecution"), strTraceDetails);
            LoggerLevelProviderClass.LOGGER.error(strFeedback);
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
     * @param exitCode execution exit code
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
     * Is the current Log Level less than Warning
     * @return Boolean
     */
    public static boolean isCurrentLogLevelLessThanWarning() {
        return LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN);
    }

    private LogExposure () {
        // intentionally blank
    }

}

package log;

import java.time.LocalDateTime;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import localization.JavaJavaLocalizationClass;
import time.TimingClass;

/**
 * exposing things to Log
 */
public final class LogExposureClass {
    /**
     * Logger
     */
    public static final Logger LOGGER = LogManager.getLogger("io.github.danielgp-eu.javajava");

    /**
     * Execution Interrupted details captured to Error log
     * @param strTraceDetails details
     */
    public static void exposeExecutionInterrupedLoggedToError(final String strTraceDetails) {
        if (getLogLevel().isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nAppInterruptedExecution"), strTraceDetails);
            LOGGER.error(strFeedback);
        }
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    public static void exposeProcessBuilder(final String strCommand) {
        if (getLogLevel().isLessSpecificThan(Level.INFO)) {
            final boolean bolFeedbackNeeded = !strCommand.contains("7za") || !strCommand.contains(", -p");
            if (bolFeedbackNeeded) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionCommandIntention"), strCommand);
                LOGGER.debug(strFeedback);
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
        if (getLogLevel().isLessSpecificThan(Level.INFO)) {
            String strCaptureMessage = "i18nProcessExecutionWithCaptureCompleted";
            if (strOutLineSep.isBlank()) {
                strCaptureMessage = "i18nProcessExecutionWithoutCaptureCompleted";
            }
            final String strFeedback = TimingClass.logDuration(startTimeStamp,
                String.format(JavaJavaLocalizationClass.getMessage(strCaptureMessage), exitCode));
            LOGGER.debug(strFeedback);
        }
    }

    /**
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    /**
     * Is the current Log Level less than Warning
     * @return Boolean
     */
    public static boolean isCurrentLogLevelLessThanWarning() {
        return getLogLevel().isLessSpecificThan(Level.WARN);
    }

    private LogExposureClass () {
        super();
    }

}

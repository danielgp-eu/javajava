package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import localization.JavaJavaLocalizationClass;

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
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    private LogExposureClass () {
        super();
    }

}

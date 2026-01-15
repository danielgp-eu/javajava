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
     * standard Unknown feature
     */
    public static final String STR_I18N_UNKN_FTS = JavaJavaLocalizationClass.getMessage("i18nUnknFtrs");
    /**
     * standard Unknown
     */
    public static final String STR_I18N_UNKN = JavaJavaLocalizationClass.getMessage("i18nUnknown");

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

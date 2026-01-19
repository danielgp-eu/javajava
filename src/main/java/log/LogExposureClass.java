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
     * Process Capture Need
     */
    /* default */ private static boolean needProcExposure = true;
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
        if (getLogLevel().isLessSpecificThan(Level.INFO) && needProcExposure) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nProcessExecutionCommandIntention"), strCommand);
            LOGGER.debug(strFeedback);
        } 
    }

    /**
     * Build message for file operation error
     * @param strFileName file name
     * @param strStagTrace stag trace
     * @return message for file operation error
     */
    public static String getFileErrorMessage(final String strFileName, final String strStagTrace) {
        return String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, strStagTrace);
    }

    /**
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    /**
     * Setter for Process Exposure
     * @param inProcExposure true or false for exposing process parameters to Log
     */
    public static void setProcessExposureNeed(final boolean inProcExposure) {
        needProcExposure = inProcExposure;
    }

    private LogExposureClass () {
        super();
    }

}

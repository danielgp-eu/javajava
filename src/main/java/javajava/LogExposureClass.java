package javajava;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public static final String STR_I18N_UNKN_FTS = LocalizationClass.getMessage("i18nUnknFtrs");
    /**
     * standard Unknown
     */
    public static final String STR_I18N_UNKN = LocalizationClass.getMessage("i18nUnknown");

    /**
     * Build message for I/O exception
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(final String inStackTrace) {
        final String strFeedbackErr = String.format("Input/Output exception on... %s", inStackTrace);
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Build message for I/O exception
     * @param customMsg custom message
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeInputOutputException(final String customMsg, final String inStackTrace) {
        final String strFeedbackErr = customMsg + String.format("... %s", inStackTrace);
        LOGGER.error(strFeedbackErr);
    }

    /**
     * Log Process Builder command conditionally
     * @param strCommand command to execute
     */
    public static void exposeProcessBuilder(final String strCommand) {
        if (getLogLevel().isLessSpecificThan(Level.INFO) && needProcExposure) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nProcessExecutionCommandIntention"), strCommand);
            LOGGER.debug(strFeedback);
        } 
    }

    /**
     * Log Process Builder command conditionally
     * @param inStackTrace tracking back the Stack Trace
     */
    public static void exposeProjectModel(final String inStackTrace) {
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nErrorOnGettingProjectModel"), inStackTrace);
        LOGGER.error(strFeedback);
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose Query purpose
     */
    public static void exposeSqlExecutionSuccessInfo(final String strQueryPurpose) {
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
        LOGGER.info(strFeedback);
    }

    /**
     * Build message for file operation error
     * @param strFileName file name
     * @param strStagTrace stag trace
     * @return message for file operation error
     */
    public static String getFileErrorMessage(final String strFileName, final String strStagTrace) {
        return String.format(LocalizationClass.getMessage("i18nFileWritingError"), strFileName, strStagTrace);
    }

    /**
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    /**
     * handle NameUnformatted
     * @param intRsParams number for parameters
     * @param strUnformatted original string
     * @param strReplacement replacements (1 to multiple)
     * @return String
     */
    public static String handleNameUnformattedMessage(final int intRsParams, final String strUnformatted, final Object... strReplacement) {
        return switch (intRsParams) {
            case 1 -> String.format(strUnformatted, strReplacement[0]);
            case 2 -> String.format(strUnformatted, strReplacement[0], strReplacement[1]);
            case 3 -> String.format(strUnformatted, strReplacement[0], strReplacement[1], strReplacement[2]);
            default -> String.format(STR_I18N_UNKN_FTS, intRsParams, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(STR_I18N_UNKN)));
        };
    }

    /**
     * Setter for Process Exposure
     * @param inProcExposure true or false for exposing process parameters to Log
     */
    public static void setProcessExposureNeed(final boolean inProcExposure) {
        needProcExposure = inProcExposure;
    }

    /**
     * Constructor
     */
    private LogExposureClass () {
        super();
    }

}

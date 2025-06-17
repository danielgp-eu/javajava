package javajava;
/* Logging classes */
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class to handle Logging
 */
public final class LogLevelChecker {
    /**
     * pointer for all logs
     */
    private static final Logger logger = LogManager.getLogger(LogLevelChecker.class);

    /**
     * Conditional logger ALL,TRACE,DEBUG,INFO,WARN,ERROR,FATAL,OFF
     * @param strFeedback Feedback to Log
     * @param strLevel Level targeted
     */
    public static void logConditional(final String strFeedback, final Level strLevel) {
        final Level crtLevel = logger.getLevel(); // NOPMD by Daniel Popiniuc on 14.06.2025, 16:51
        if (crtLevel.isInRange(Level.FATAL, Level.ALL)) {
            if (strLevel == Level.FATAL) {
                logger.fatal(strFeedback);
            } else if (strLevel == Level.ERROR) {
                logger.error(strFeedback);
            } else if (strLevel == Level.WARN) {
                logger.warn(strFeedback);
            } else if (strLevel == Level.INFO) {
                logger.info(strFeedback);
            } else if (strLevel == Level.DEBUG) {
                logger.debug(strFeedback);
            } else if (strLevel == Level.TRACE) {
                logger.trace(strFeedback);
            }
        }
    }

    /**
     * Constructor
     */
    private LogLevelChecker() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

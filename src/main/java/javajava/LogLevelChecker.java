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
     * Conditional logger
     * @param strFeedback
     * @param strLevel
     */
    public static void logConditional(final String strFeedback, final Level strLevel) {
        if (strLevel.isInRange(Level.FATAL, Level.ALL)) {
            logger.fatal(strFeedback);
            if (strLevel == Level.ERROR) {
                logger.error(strFeedback);
            }
            if (strLevel.isLessSpecificThan(Level.ERROR)) {
                logger.warn(strFeedback);
            }
            if (strLevel.isLessSpecificThan(Level.WARN)) {
                logger.info(strFeedback);
            }
            if (strLevel == Level.INFO) {
                logger.debug(strFeedback);
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

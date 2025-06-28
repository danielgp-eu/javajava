package javajava;
/* Logger classes */
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Managing current Log level
 */
public class LoggerLevelProvider {
    /**
     * Logger
     */
    public Logger LOGGER;
    /**
     * Current Log level
     */
    private final Level currentLevel;

    /**
     * Logger Provider
     * @param loggerName
     */
    public LoggerLevelProvider(final String loggerName) {
        this.LOGGER = LogManager.getLogger(loggerName);
        this.currentLevel = getCurrentLevel(); // NOPMD by Daniel Popiniuc on 28.06.2025, 18:26
    }

    /**
     * Method to get the current logging level
     * @return current Level
     */
    public Level getCurrentLevel() {
        return LOGGER.getLevel();
    }

    /**
     * Method to log a message at DEBUG level
     * @param message
     */
    public void logDebug(final String message) {
        if (currentLevel.isLessSpecificThan(Level.INFO)) {
            LOGGER.debug(message);
        }
    }

    /**
     * Method to log a message at ERROR level
     * @param message
     */
    public void logError(final String message) {
        if (currentLevel.isLessSpecificThan(Level.FATAL)) {
            LOGGER.error(message);
        }
    }

    /**
     * Method to log a message at INFO level
     * @param message
     */
    public void logInfo(final String message) {
        if (currentLevel.isLessSpecificThan(Level.WARN)) {
            LOGGER.info(message);
        }
    }

    /**
     * Method to log a message at WARN level
     * @param message
     */
    public void logWarn(final String message) {
        if (currentLevel.isLessSpecificThan(Level.ERROR)) {
            LOGGER.warn(message);
        }
    }
}

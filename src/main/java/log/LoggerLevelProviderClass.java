package log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Managing current Log level
 */
public final class LoggerLevelProviderClass {
    /**
     * Logger
     */
    public static final Logger LOGGER = LogManager.getLogger("io.github.danielgp-eu.javajava");

    /**
     * Get current Log Level
     * @return current Log Level
     */
    public static Level getLogLevel() {
        return LOGGER.getLevel();
    }

    /**
     * Constructor
     */
    private LoggerLevelProviderClass() {
        // intentionally left blank
    }

}

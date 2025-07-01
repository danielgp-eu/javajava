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
    public final static Logger LOGGER = LogManager.getLogger("io.github.danielgp-eu.javajava");
    /**
     * Current Log level
     */
    public static Level currentLevel = LOGGER.getLevel(); // NOPMD by Daniel Popiniuc on 01.07.2025, 22:26
}

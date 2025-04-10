package danielgp;
/* Facilitates log information capturing */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for Logging
 */
public class LogHandlingClass {
    /**
     * pointer for all logs
     */
    public static final Logger LOGGER = LogManager.getLogger();
}

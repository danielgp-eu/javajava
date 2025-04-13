package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SQLite methods
 */
public class DatabaseSpecificSqLite {

    /**
     * Initiates a SQLite connection
     * 
     * @param strSqLiteFile
     * @return Connection
     */
    public static Connection getSqLiteConnection(final String strSqLiteFile) {
        String strFeedback = String.format("Will attempt to create a SQLite connection to %s file", strSqLiteFile);
        LogHandlingClass.LOGGER.debug(strFeedback);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + strSqLiteFile);
            strFeedback = String.format("SQLite connection to %s database was successfully established!", strSqLiteFile);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch(SQLException e) {
            strFeedback = String.format("Connection failed: %s", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return connection;
    }

    /**
     * constructor
     */
    protected DatabaseSpecificSqLite() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/* SQLite function class */
import org.sqlite.Function;
/* Util classes */
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * SQLite methods
 */
public class DatabaseSpecificSqLite extends DatabaseResultSetingClass {

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
            Function.create(connection, "REGEXP_LIKE", new Function() {
                @Override
                protected void xFunc() throws SQLException {
                    final String text = value_text(0);
                    final String pattern = value_text(1);
                    final Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    final Matcher matcher = regex.matcher(text);
                    result(matcher.find() ? 1 : 0);
                }
            });
        } catch(SQLException e) {
            strFeedback = String.format("Connection failed: %s", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return connection;
    }

    /**
     * constructor
     */
    public DatabaseSpecificSqLite() {
        super();
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

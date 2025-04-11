package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Database methods
 */
public class DatabaseBasicClass {

    /**
     * Connection closing
     * 
     * @param strDatabaseType
     * @param givenConnection
     */
    protected static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        try {
            givenConnection.close();
            final String strFeedback = String.format("%s connection sucessfully closed!", strDatabaseType);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            final String strFeedback = String.format("Closing %s connection failed with following error: %s", strDatabaseType, e.getLocalizedMessage()); 
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType
     * @param givenStatement
     */
    protected static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        try {
            givenStatement.close();
            final String strFeedback = String.format("%s statement sucessfully closed!", strDatabaseType); 
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            final String strFeedback = String.format("Closing %s statement failed: %s", strDatabaseType, e.getLocalizedMessage()); 
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Execute a custom query w/o any result-set
     * 
     * @param objStatement
     * @param strQueryPurpose
     * @param strQueryToUse
     */
    public static void executeQueryWithoutResultSet(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse) {
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now();
            String strFeedback = String.format("Will execute %s query", strQueryPurpose);
            LogHandlingClass.LOGGER.debug(strFeedback);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                strFeedback = String.format("Executing %s query was successful!", strQueryPurpose);
                LogHandlingClass.LOGGER.debug(strFeedback);
            } catch (SQLException e) {
                strFeedback = String.format("Executionfor %s has failed: %s...%", strQueryPurpose, e.getLocalizedMessage(), e.getStackTrace());
                LogHandlingClass.LOGGER.error(strFeedback);
            }
            TimingClass.logDuration(startTimeStamp, String.format("Finished executing %s query", strQueryPurpose), "debug");
        }
    }

    /**
     * Instantiating a statement
     * 
     * @param strDatabaseType
     * @param connection
     * @return Statement
     */
    protected static Statement getSqlStatement(final String strDatabaseType, final Connection connection) {
        String strFeedback = String.format("Will attempt to instantiate a %s statement", strDatabaseType);
        LogHandlingClass.LOGGER.debug(strFeedback);
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            strFeedback = String.format("A %s statement successfully instantiated!", strDatabaseType);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format("Statement creation failed: ", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return objStatement;
    }

    /**
     * Initiates a SQLite connection
     * 
     * @param strSqLiteFile
     * @return Connection
     */
    protected static Connection getSqLiteConnection(final String strSqLiteFile) {
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
    protected DatabaseBasicClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

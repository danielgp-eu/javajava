package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
/* Utility classes */
import java.util.ArrayList;  
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Database methods
 */
public class DatabasingClass {

    /**
     * Connection closing
     * 
     * @param String strDatabaseType
     * @param Connection givenConnection
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
     * extends functionality for Executions
     * 
     * @param String strQueryNameOrPurpose
     * @param ResultSet resultSet
     * @param Properties objProperties
     */
    private static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
        String strFeedback;
        final int intResultSetRows = getResultSetRows(resultSet);
        int intColumnsIs;
        final Iterator<Object> keyIterator = objProperties.keySet().iterator();
        while(keyIterator.hasNext()){
            final String key = (String) keyIterator.next();
            switch(key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    intColumnsIs = getResultSetColumns(resultSet);
                    if (intColumnsIs != intColumnsShould) {
                        strFeedback = String.format("For the \"%s\" query the Resultset was expected to have exact %s column(s) but a %s were found...", strPurpose, intColumnsShould, intColumnsIs);
                        LogHandlingClass.LOGGER.error(strFeedback);
                    }
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        strFeedback = String.format("For the \"%s\" query the Resultset was expected to have exact %s row(s) but a %s was/were found...", strPurpose, intExpectedRows, intResultSetRows);
                        LogHandlingClass.LOGGER.error(strFeedback);
                    }
                    break;
                case "exposeNumberOfColumns":
                    intColumnsIs = getResultSetColumns(resultSet);
                    strFeedback = String.format("Number of columns retrieved is %d", intColumnsIs);
                    LogHandlingClass.LOGGER.info(strFeedback);
                    break;
                default:
                    strFeedback = String.format("Feature %s is NOT known...", key);
                    throw new UnsupportedOperationException(strFeedback);
            }
        }
    }

    /**
     * Statement closing
     * 
     * @param String strDatabaseType
     * @param Statement givenStatement
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
     * @param Statement objStatement
     * @param String strQueryPurpose
     * @param String strQueryToUse
     */
    public static void executeCustomQuery(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse) {
        if (strQueryToUse != null) {
            final long startNano = System.nanoTime();
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
            TimingClass.logDuration(startNano, String.format("Finished executing %s query", strQueryPurpose));
        }
    }

    /**
     * Execute a custom query with result-set expected
     * @param Statement objStatement
     * @param String strQueryPurpose
     * @param String strQueryToUse
     * @param Properties objProperties
     * @return ResultSet
     */
    public static ResultSet executeCustomQuery(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse, final Properties objProperties) {
        ResultSet resultSet = null;
        if (strQueryToUse != null) {
            final long startNano = System.nanoTime();
            String strFeedback = String.format("Will execute %s query", strQueryPurpose);
            LogHandlingClass.LOGGER.debug(strFeedback);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                strFeedback = String.format("Executing %s query was successful!", strQueryPurpose);
                LogHandlingClass.LOGGER.debug(strFeedback);
                digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                strFeedback = String.format("Statement execution for %s has failed with following error: %s", strQueryPurpose, e.getLocalizedMessage());
                LogHandlingClass.LOGGER.error(strFeedback);
            }
            TimingClass.logDuration(startNano, String.format("Finished executing %s query", strQueryPurpose));
        }
        return resultSet;
    }

    /**
     * get # of Columns from ResultSet 
     * 
     * @param ResultSet resultSet
     * @return int
     */
    private static int getResultSetColumns(final ResultSet resultSet) {
        int intColumns = -1;
        try {
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            intColumns = resultSetMetaData.getColumnCount();
        } catch (SQLException e) {
            final String strFeedback = String.format("Unable to get the # of columns in the ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return intColumns;
    }

    /**
     * get list of values
     * 
     * @param resultSet
     * @return
     */
    protected static List<String> getResultSetListOfStrings(final ResultSet resultSet) {
        final List<String> listStrings = new ArrayList<>();
        try {
            while (resultSet.next()) {
                listStrings.add(resultSet.getString(0));
            }
        } catch (SQLException e) {
            final String strFeedback = String.format("Unable to get list of strings from ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return listStrings;
    }

    /**
     * get # of Columns from ResultSet 
     * 
     * @param ResultSet resultSet
     * @return int
     */
    private static int getResultSetRows(final ResultSet resultSet) {
        int intResultSetRows = -1;
        try {
            intResultSetRows = resultSet.getFetchSize() + 1;
        } catch (SQLException e) {
            final String strFeedback = String.format("Unable to get the # of columns in the ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return intResultSetRows;
    }

    /**
     * Instatiating a statement
     * 
     * @param String strDatabaseType
     * @param Connection connection
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
     * @param String strSqLiteFile
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
    protected DatabasingClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

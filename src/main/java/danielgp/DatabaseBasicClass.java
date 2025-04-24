package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
/* Time classes */
import java.time.LocalDateTime;
/* Utility classes */
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

/**
 * Database methods
 */
public class DatabaseBasicClass { // NOPMD by Daniel Popiniuc on 17.04.2025, 17:11

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
     * Fill values into a dynamic query 
     * @param queryProperties
     * @param strRawQuery
     * @param arrayCleanable
     * @param arrayNullable
     * @return
     */
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        final Iterator<Object> keyIterator = queryProperties.keySet().iterator();
        while(keyIterator.hasNext()){
            final String strKey = (String) keyIterator.next();
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format("\"%s\"", strOriginalValue);
            if (strOriginalValue.matches("NULL")) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format("\"%s\"", strOriginalValue.replaceAll("(\"|')", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = "NULL";
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = "NULL";
            }
            strQueryToReturn = strQueryToReturn.replace(String.format("{%s}", strKey), strValueToUse);
        }
        return strQueryToReturn;
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
    public static Statement getSqlStatement(final String strDatabaseType, final Connection connection) {
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

}

package javajava;
/* SQL classes */
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
/* Time classes */
import java.time.LocalDateTime;
/* Utility classes */
import java.util.Arrays;
import java.util.Properties;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Database methods
 */
public class DatabaseBasicClass { // NOPMD by Daniel Popiniuc on 17.04.2025, 17:11
    /**
     * pointer for all logs
     */
    protected static final Logger LOGGER = LogManager.getLogger(DatabaseBasicClass.class);

    /**
     * Connection closing
     * 
     * @param strDatabaseType
     * @param givenConnection
     */
    public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
        LOGGER.debug(strFeedback);
        try {
            givenConnection.close();
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
            LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType
     * @param givenStatement
     */
    public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
        LOGGER.debug(strFeedback);
        try {
            givenStatement.close();
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
            LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
    }

    /**
     * Instantiating a statement
     * 
     * @param strDatabaseType
     * @param connection
     * @return Statement
     */
    public static Statement createSqlStatement(final String strDatabaseType, final Connection connection) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
        LOGGER.debug(strFeedback);
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
            LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return objStatement;
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
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
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
            String strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
            LOGGER.debug(strFeedback);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LOGGER.debug(strFeedback);
            } catch (SQLException e) {
                strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LOGGER.error(strFeedback);
            }
            TimingClass.logDuration(startTimeStamp, String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose), "debug");
        }
    }

}

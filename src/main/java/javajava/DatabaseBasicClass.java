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
import org.apache.logging.log4j.Level;

/**
 * Database methods
 */
public class DatabaseBasicClass { // NOPMD by Daniel Popiniuc on 17.04.2025, 17:11

    /**
     * Connection closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenConnection connection object
     */
    @SuppressWarnings("unused")
    public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
        LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        try {
            givenConnection.close();
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        } catch (SQLException e) {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenStatement statement
     */
    @SuppressWarnings("unused")
    public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
        LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        try {
            givenStatement.close();
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        } catch (SQLException e) {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
        }
    }

    /**
     * Instantiating a statement
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param connection connection to use
     * @return Statement
     */
    public static Statement createSqlStatement(final String strDatabaseType, final Connection connection) {
        String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
        LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        } catch (SQLException e) {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
        }
        return objStatement;
    }

    /**
     * Fill values into a dynamic query 
     * @param queryProperties properties for connection
     * @param strRawQuery raw query
     * @param arrayCleanable array with fields to clean
     * @param arrayNullable array with nullable fields
     * @return final query
     */
    @SuppressWarnings("unused")
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format("\"%s\"", strOriginalValue);
            if (strOriginalValue.matches("NULL")) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format("\"%s\"", strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = "NULL";
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = "NULL";
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                strValueToUse = String.format("\"%s\"", strOriginalValue.replace("\"", "\"\""));
            }
            strQueryToReturn = strQueryToReturn.replace(String.format("{%s}", strKey), strValueToUse);
        }
        return strQueryToReturn;
    }

    /**
     * Execute a custom query w/o any result-set
     * 
     * @param objStatement statement
     * @param strQueryPurpose purpose of query
     * @param strQueryToUse query to use
     */
    public static void executeQueryWithoutResultSet(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse) {
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now();
            String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryToUse);
                    LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
            } catch (SQLException e) {
                strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LogLevelChecker.logConditional(strFeedback, Level.ERROR);
            }
            TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose), "debug");
        }
    }

    /**
     * Constructor
     */
    protected DatabaseBasicClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

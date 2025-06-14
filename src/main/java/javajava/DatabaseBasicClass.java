package javajava;
/* SQL classes */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
/* Time classes */
import java.time.LocalDateTime;
/* Utility classes */
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
            if (strOriginalValue.matches(Common.strNull)) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format("\"%s\"", strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = Common.strNull;
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = Common.strNull;
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
     * Values to be added for bulk operations
     * @param objConnection
     * @param objValues
     * @param strQuery
     */
    public static void executeValuesIntoDatabaseUsingPreparedStatement(final Connection objConnection, final List<Properties> objValues, final String strQuery, final String[] arrayCleanable, final String... arrayNullable) {
        final int intRows = objValues.size();
        final String strFinalQ = Common.convertPromptParametersIntoParameters(strQuery);
        final Properties mapParameterOrder = getPromptParametersOrderWithinQuery(strQuery, objValues);
        try (PreparedStatement preparedStatement = objConnection.prepareStatement(strFinalQ);) {
            for (int crtRow = 1; crtRow <= intRows; crtRow++) {
                final Properties currentProps = objValues.get(crtRow - 1);
                for (final Map.Entry<Object, Object> entry : currentProps.entrySet()) {
                    final String strKey =  entry.getKey().toString();
                    final int index = Integer.parseInt(mapParameterOrder.getProperty(strKey));
                    final String strOriginalValue = entry.getValue().toString();
                    String strValueToUse = strOriginalValue;
                    if (strOriginalValue.matches(Common.strNull)) {
                        preparedStatement.setNull(index, Types.VARCHAR);
                    } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                        strValueToUse = strOriginalValue.replaceAll("([\"'])", "");
                        if (strValueToUse.isEmpty()) {
                            preparedStatement.setNull(index, Types.VARCHAR);
                        } else {
                            preparedStatement.setString(index, strValueToUse);
                        }
                    } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                        preparedStatement.setNull(index, Types.VARCHAR);
                    } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                        preparedStatement.setString(index, strOriginalValue.replace("\"", "\"\""));
                    } else {
                        preparedStatement.setString(index, strValueToUse);
                    }
                }
                LogLevelChecker.logConditional("Final query is: " + preparedStatement.getParameterMetaData().toString(), Level.DEBUG);
                preparedStatement.addBatch();
                if (crtRow % 100 == 0) { // execute every 100 rows
                    preparedStatement.executeBatch();
                } else if (crtRow == intRows) { // left-over rows
                    preparedStatement.executeBatch();
                }
            }
        } catch (SQLException e) {
            LogLevelChecker.logConditional(e.getLocalizedMessage(), Level.ERROR);
        }
    }

    /**
     * get order of Prompt Parameters within Query 
     * @param strOriginalQ query to consider expected to have Prompt Parameters
     * @param objValues list with Values as List<Properties>
     * @return Properties with order as value
     */
    public static Properties getPromptParametersOrderWithinQuery(final String strOriginalQ, final List<Properties> objValues) {
        final List<String> listMatches = Common.extractMatches(strOriginalQ, Common.strPrmptPrmtrRgEx);
        final int intParameters = listMatches.size();
        final Properties mapParameterOrder = new Properties();
        for(int intParameter = 0; intParameter < intParameters; intParameter++) {
            final String crtParameter =  listMatches.get(intParameter);
            for (final Map.Entry<Object, Object> entry : objValues.get(0).entrySet()) {
                if (crtParameter.equals(entry.getKey())) {
                    mapParameterOrder.put(crtParameter, intParameter);
                }
            }
        }
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format("Seems we have a problem as %d parameters are expected but only %d were given %s for query \"%s\"", intParameters, foundParameters, mapParameterOrder.toString(), strOriginalQ);
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
        }
        return mapParameterOrder;
    }

    /**
     * Constructor
     */
    protected DatabaseBasicClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

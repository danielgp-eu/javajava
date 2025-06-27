package javajava;
/* SQL classes */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
/* Time classes */
import java.time.LocalDateTime;
import java.util.ArrayList;
/* Utility classes */
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
/* Logging classes */
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Database methods
 */
public class DatabaseBasicClass {
    /**
     * pointer for all logs
     */
    protected static final Logger LOGGER = LogManager.getLogger(DatabaseBasicClass.class);
    /**
     * pointer for all logs
     */
    protected static final Level LogLevel = LOGGER.getLevel(); // NOPMD by E303778 on 25.06.2025, 11:17

    /**
     * Connection closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenConnection connection object
     */
    @SuppressWarnings("unused")
    public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
            LOGGER.debug(strFeedback);
        }
        try {
            givenConnection.close();
            if (LogLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
                LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
                LOGGER.error(strFeedback);
            }
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
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
            LOGGER.debug(strFeedback);
        }
        try {
            givenStatement.close();
            if (LogLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
                LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
                LOGGER.error(strFeedback);
            }
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
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
            LOGGER.debug(strFeedback);
        }
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            if (LogLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
                LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
                LOGGER.error(strFeedback);
            }
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
            if (LogLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
                LOGGER.debug(strFeedback);
            }
            try {
                if (LogLevel.isLessSpecificThan(Level.INFO)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryToUse);
                    LOGGER.debug(strFeedback);
                }
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                if (LogLevel.isLessSpecificThan(Level.INFO)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                    LOGGER.debug(strFeedback);
                }
            } catch (SQLException e) {
                if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                    LOGGER.error(strFeedback);
                }
            }
            if (LogLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose));
                LOGGER.debug(strFeedback);
            }
        }
    }

    /**
     * Values to be added for bulk operations
     * @param objConnection Connection for destination Database
     * @param strQueryPurpose Purpose for query execution
     * @param objValues Values to use for executions
     * @param strQuery Original Query with Prompt Parameters
     * @param arrayCleanable Clean-able fields as array of Strings
     * @param arrayNullable Null-able fields
     */
    public static void executeValuesIntoDatabaseUsingPreparedStatement(final Connection objConnection, final String strQueryPurpose, final List<Properties> objValues, final String strQuery, final String[] arrayCleanable, final String... arrayNullable) {
        final int intRows = objValues.size();
        final List<String> mapParameterOrder = getPromptParametersOrderWithinQuery(strQuery, objValues);
        final int intParameters = mapParameterOrder.size();
        final String strFinalQ = Common.convertPromptParametersIntoParameters(strQuery);
        try (PreparedStatement preparedStatement = objConnection.prepareStatement(strFinalQ);) {
            // cycle through each row
            for (int crtRow = 1; crtRow <= intRows; crtRow++) {
                final Properties currentProps = objValues.get(crtRow - 1);
                // cycle through every single Parameter to set its value to PreparedStatement
                for (int intParameter = 0; intParameter < intParameters; intParameter++) {
                    final int index = intParameter + 1;
                    final String strKey = mapParameterOrder.get(intParameter);
                    final String strOriginalValue = currentProps.getProperty(strKey);
                    String strValueToUse = strOriginalValue;
                    try {
                        if (Common.strNull.equalsIgnoreCase(strOriginalValue)) {
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
                    } catch (SQLException e) {
                        if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                            // TODO: localize below String
                            final String strFeedback = e.getLocalizedMessage() + " on Parameter " +  strKey + " and Query " + strQuery;
                            LOGGER.error(strFeedback);
                        }
                    }
                }
                preparedStatement.addBatch();
                boolean needsExecution = false;
                if (crtRow % 200 == 0) { // execute every 200 rows
                    needsExecution = true;
                } else if (crtRow == intRows) { // left-over rows
                    needsExecution = true;
                }
                if (needsExecution) {
                    preparedStatement.executeLargeBatch();
                    if (LogLevel.isLessSpecificThan(Level.INFO)) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                        LOGGER.debug(strFeedback);
                    }
                }
            }
        } catch (SQLException e) {
            if (LogLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = e.getLocalizedMessage() + " with Values " + objValues.get(0).toString() + " for Query " + strQuery;
                LOGGER.error(strFeedback);
            }
            throw (IllegalStateException)new IllegalStateException().initCause(e);
        }
    }

    /**
     * get order of Prompt Parameters within Query 
     * @param strOriginalQ query to consider expected to have Prompt Parameters
     * @param objValues list with Values as List of Properties
     * @return List of Strings with order as value
     */
    public static List<String> getPromptParametersOrderWithinQuery(final String strOriginalQ, final List<Properties> objValues) {
        final List<String> valFields = new ArrayList<>();
        objValues.get(0).forEach((strKey, strValue) -> {
            valFields.add(strKey.toString());
        });
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            // TODO: localize below String
            final String strFeedback = String.format("Value Parameters are %s", valFields.toString());
            LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = Common.extractMatches(strOriginalQ, Common.strPrmptPrmtrRgEx);
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            // TODO: localize below String
            final String strFeedback = String.format("Query Parameters are %s", listMatches.toString());
            LOGGER.debug(strFeedback);
        }
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (int intParameter = 0; intParameter < intParameters; intParameter++) {
            final String crtParameter = listMatches.get(intParameter).replaceAll("(\\{|\\})", "");
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        if (LogLevel.isLessSpecificThan(Level.INFO)) {
            // TODO: localize below String
            final String strFeedback = String.format("Mapping for Parameters is %s", mapParameterOrder.toString());
            LOGGER.debug(strFeedback);
        }
        final int foundParameters = mapParameterOrder.size();
        if ((foundParameters != intParameters) && LogLevel.isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder.toString() + " vs. " + objValues.get(0).toString()
                , strOriginalQ);
            LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Constructor
     */
    public DatabaseBasicClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

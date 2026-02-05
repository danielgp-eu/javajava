package javajava;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Database methods
 */
public final class DatabaseOperationsClass {
    /**
     * Database MySQL
     */
    public static final String STR_DB_MYSQL = "MySQL";
    /**
     * NULL string
     */
    public static final String STR_NULL = "NULL";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_QTD_STR_VL = "\"%s\"";

    /**
     * Fill values into a dynamic query
     * @param queryProperties properties for connection
     * @param strRawQuery raw query
     * @param arrayCleanable array with fields to clean
     * @param arrayNullable array with NULL-able fields
     * @return final query
     */
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue);
            if (strOriginalValue.matches(STR_NULL)) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = STR_NULL;
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = STR_NULL;
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                strValueToUse = String.format(STR_QTD_STR_VL, strOriginalValue.replace("\"", "\"\""));
            }
            strQueryToReturn = strQueryToReturn.replace(String.format("{%s}", strKey), strValueToUse);
        }
        return strQueryToReturn;
    }

    /**
     * Execute a custom query with result-set expected
     * @param objStatement statement
     * @param strQueryPurpose query purpose
     * @param strQueryToUse query to use
     * @param objProperties properties (with features to apply)
     * @return ResultSet
     */
    public static ResultSet executeCustomQuery(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse, final Properties objProperties) {
        ResultSet resultSet = null;
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now();
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
                ResultSettingClass.digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose));
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
        }
        return resultSet;
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
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                LogExposureClass.exposeSqlExecutionSuccessInfo(strQueryPurpose);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose));
            LogExposureClass.LOGGER.debug(strFeedbackEnd);
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
        objValues.getFirst().forEach((strKey, _) -> valFields.add(strKey.toString()));
        final String strFeedbackPrmV = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValuesAre"), valFields);
        LogExposureClass.LOGGER.debug(strFeedbackPrmV);
        final List<String> listMatches = BasicStructuresClass.ListAndMapClass.extractMatches(strOriginalQ, BasicStructuresClass.STR_PRMTR_RGX);
        final String strFeedbackPrm = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterForQueryAre"), listMatches);
        LogExposureClass.LOGGER.debug(strFeedbackPrm);
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (final String listMatch : listMatches) {
            final String crtParameter = BasicStructuresClass.StringCleaningClass.cleanStringFromCurlyBraces(listMatch);
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        final String strFeedbackPrmM = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterMappingAre"), mapParameterOrder);
        LogExposureClass.LOGGER.debug(strFeedbackPrmM);
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder + " vs. " + objValues.getFirst().toString()
                , strOriginalQ);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Database connectivity
     */
    public static final class ConnectivityClass {

        /**
         * Connection closing
         *
         * @param strDatabaseType type of database (mainly for meaningful feedback)
         * @param givenConnection connection object
         */
        public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenConnection.close();
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
        }

        /**
         * Statement closing
         *
         * @param strDatabaseType type of database (mainly for meaningful feedback)
         * @param givenStatement statement
         */
        public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenStatement.close();
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
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
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            Statement objStatement = null;
            try {
                objStatement = connection.createStatement();
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return objStatement;
        }

        /**
         * Constructor
         */
        private ConnectivityClass() {
            // intentionally blank
        }

    }

    /**
     * Database Query Binding
     */
    public static final class QueryBindingClass {

        /**
         * Values to be added for bulk operations
         * @param objConnection Connection for destination Database
         * @param strQueryPurpose Purpose for query execution
         * @param objValues Values to use for executions
         * @param strQuery Original Query with Prompt Parameters
         * @param specialFields Clean-able and Null-able fields
         */
        public static void executeValuesIntoDatabaseUsingPreparedStatement(final Connection objConnection, final String strQueryPurpose, final List<Properties> objValues, final String strQuery, final Properties specialFields) {
            final int intRows = objValues.size();
            final List<String> mapParameterOrder = getPromptParametersOrderWithinQuery(strQuery, objValues);
            final int intParameters = mapParameterOrder.size();
            final String strFinalQ = BasicStructuresClass.StringTransformationClass.convertPromptParametersIntoParameters(strQuery);
            try (PreparedStatement preparedStatement = objConnection.prepareStatement(strFinalQ)) {
                final Properties properties = new Properties();
                // cycle through each row
                for (int crtRow = 1; crtRow <= intRows; crtRow++) {
                    final Properties currentProps = objValues.get(crtRow - 1);
                    // cycle through every single Parameter to set its value to PreparedStatement
                    for (int intParameter = 0; intParameter < intParameters; intParameter++) {
                        final int index = intParameter + 1;
                        final String strKey = mapParameterOrder.get(intParameter);
                        final String strOriginalValue = currentProps.getProperty(strKey);
                        properties.put("index", index);
                        properties.put("strKey", strKey);
                        properties.put("strOriginalValue", strOriginalValue);
                        properties.put("strQuery", strQuery);
                        properties.put("strArrayCleanable", specialFields.get("Cleanable").toString());
                        properties.put("strArrayNullable", specialFields.get("Nullable").toString());
                        bindSingleParameter(preparedStatement, properties);
                    }
                    preparedStatement.addBatch();
                    if ((crtRow % 200 == 0)
                            || (crtRow == intRows)) { // each 200 rows OR final one
                        preparedStatement.executeLargeBatch();
                        LogExposureClass.exposeSqlExecutionSuccessInfo(strQueryPurpose);
                    }
                }
            } catch (SQLException e) {
                setSqlExceptionError(e, objValues, strQuery);
                throw (IllegalStateException)new IllegalStateException().initCause(e);
            }
        }

        /**
         * bind Single Parameter
         * @param preparedStatement original Prepared Statement
         * @param properties properties with relevant components
         */
        private static void bindSingleParameter(final PreparedStatement preparedStatement, final Properties properties) {
            final int index = Integer.parseInt(properties.get("index").toString());
            final String strKey = properties.get("strKey").toString();
            final String strQuery = properties.get("strQuery").toString();
            final String strOriginalValue = properties.get("strOriginalValue").toString();
            final String[] arrayCleanable = properties.get("strArrayCleanable").toString().split("\\|");
            final String[] arrayNullable = properties.get("strArrayNullable").toString().split("\\|");
            try {
                if (STR_NULL.equalsIgnoreCase(strOriginalValue)
                        || (Arrays.asList(arrayNullable).contains(strKey)
                        && strOriginalValue.isEmpty())) {
                    preparedStatement.setNull(index, Types.VARCHAR);
                } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                    final String strCleanedValue = strOriginalValue.replaceAll("([\"'])", "");
                    if (strCleanedValue.isEmpty()) {
                        preparedStatement.setNull(index, Types.VARCHAR);
                    } else {
                        preparedStatement.setString(index, strCleanedValue);
                    }
                } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                    preparedStatement.setString(index, strOriginalValue.replace("\"", "\"\""));
                } else {
                    preparedStatement.setString(index, strOriginalValue);
                }
            } catch (SQLException e) {
                setSqlParameterBindingError(e, strKey, strQuery);
            }
        }

        /**
         * Error logging the SQL Exception
         * @param exptObj exception object
         * @param objValues values provided
         * @param strQuery relevant query
         */
        private static void setSqlExceptionError(final SQLException exptObj, final List<Properties> objValues, final String strQuery) {
            final String strFeedback = String.format("%s with Values %s for Query %s", exptObj.getLocalizedMessage(), objValues.getFirst().toString(), strQuery);
            LogExposureClass.LOGGER.error(strFeedback);
        }

        /**
         * Success confirmation to Info log
         * @param exptObj SQLException
         * @param strParameterName parameter name
         * @param strQuery query
         */
        private static void setSqlParameterBindingError(final SQLException exptObj, final String strParameterName, final String strQuery) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterBindingError")
                    , exptObj.getLocalizedMessage()
                    , strParameterName
                    , strQuery);
            LogExposureClass.LOGGER.error(strFeedback);
        }

        /**
         * Constructor
         */
        private QueryBindingClass() {
            // intentionally blank
        }

    }

    /**
     * Basic features for Databases
     */
    public static final class ResultSettingClass {
        /**
         * standard SQL statement unable
         */
        public static final String STR_I18N_STM_UNB = JavaJavaLocalizationClass.getMessage("i18nSQLstatementUnableToGetX");
        /**
         * Rows counter
         */
        private static int intRows;
        /**
         * rows for result set
         */
        private static int intResultSetRows;
        /**
         * column counter
         */
        private static int intColumnsIs;

        /**
         * capture to Log result-set properties
         * @param key current key within loop
         * @param strPurpose query purpose for log text
         * @param objProperties object properties
         */
        private static void captureToLogResultsetAttributes(final String key, final String strPurpose, final Properties objProperties) {
            switch (key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    final String strFeedbackC = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs);
                    LogExposureClass.LOGGER.error(strFeedbackC);
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        final String strFeedbackExR = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows);
                        LogExposureClass.LOGGER.error(strFeedbackExR);
                    }
                    break;
                case "exposeNumberOfColumns":
                    final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                    LogExposureClass.LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    final String strFeedbackN = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
                    LogExposureClass.LOGGER.info(strFeedbackN);
                    break;
                default:
                    final String strFeedbackD = String.format(LogExposureClass.STR_I18N_UNKN_FTS, key, StackWalker.getInstance()
                            .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                    LogExposureClass.LOGGER.error(strFeedbackD);
                    throw new UnsupportedOperationException(strFeedbackD);
            }
        }

        /**
         * extends functionality for Executions
         *
         * @param strPurpose purpose of query
         * @param resultSet result-set
         * @param objProperties properties (with features to apply)
         */
        public static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
            intResultSetRows = getResultSetNumberOfRows(resultSet);
            intColumnsIs = getResultSetNumberOfColumns(resultSet);
            for (final Object obj : objProperties.keySet()) {
                final String key = (String) obj;
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleEvaluation"), key);
                LogExposureClass.LOGGER.debug(strFeedback);
                captureToLogResultsetAttributes(key, strPurpose, objProperties);
            }
        }

        /**
         * Collecting current row
         * @param resultSet digesting result-set
         * @param columnCount number of columns to iterate through
         * @param resultSetMetaData column names
         * @return Properties with current row value and their name
         * @throws SQLException
         */
        private static Properties getCurrentRowIntoProperties(final ResultSet resultSet, final int columnCount, final ResultSetMetaData resultSetMetaData) throws SQLException {
            final Properties currentRow = new Properties();
            for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                String crtValue = resultSet.getString(colIndex);
                if (resultSet.wasNull()) {
                    crtValue = STR_NULL;
                }
                currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
            }
            return currentRow;
        }

        /**
         * get structure from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnStructure(final ResultSet resultSet) {
            final List<Properties> listResultSet = new ArrayList<>();
            try {
                final ResultSetMetaData metaData = resultSet.getMetaData();
                final int columnCount = metaData.getColumnCount();
                final Properties colProperties = new Properties();
                for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
                    colProperties.clear();
                    colProperties.put("Display Size", metaData.getColumnDisplaySize(columnNumber));
                    colProperties.put("Name", metaData.getColumnName(columnNumber));
                    colProperties.put("Precision", metaData.getPrecision(columnNumber));
                    colProperties.put("Scale", metaData.getScale(columnNumber));
                    colProperties.put("Type", metaData.getColumnTypeName(columnNumber));
                    colProperties.put("Nullable", metaData.isNullable(columnNumber));
                    listResultSet.add(colProperties);
                }
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return listResultSet;
        }

        /**
         * get column values from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnValues(final ResultSet resultSet) {
            final List<Properties> listResultSet = new ArrayList<>();
            try {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                int intRow = 0;
                while (resultSet.next()) {
                    listResultSet.add(getCurrentRowIntoProperties(resultSet, columnCount, resultSetMetaData));
                    intRow++;
                }
                final String strFeedback = String.format("I have found %d records", intRow);
                LogExposureClass.LOGGER.debug(strFeedback);
                intRows = intRow;
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return listResultSet;
        }

        /**
         * get column values from ResultSet
         *
         * @param resultSet result-set
         * @return List of Properties
         */
        public static List<Properties> getResultSetColumnValuesWithNullCheck(final ResultSet resultSet) {
            List<Properties> listResultSet = new ArrayList<>();
            if (resultSet == null) {
                final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nSQLresultSetNull");
                LogExposureClass.LOGGER.error(strFeedback);
            } else {
                listResultSet = getResultSetColumnValues(resultSet);
            }
            return listResultSet;

        }

        /**
         * get list of values
         *
         * @param resultSet result-set
         * @return list of strings
         */
        public static List<String> getResultSetListOfStrings(final ResultSet resultSet) {
            final List<String> listStrings = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    listStrings.add(resultSet.getString(1));
                }
            } catch (SQLException e) {
                final String strFeedback = String.format(STR_I18N_STM_UNB, "list of strings", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return listStrings;
        }

        /**
         * get # of Columns from ResultSet
         *
         * @param resultSet result-set
         * @return number of columns
         */
        private static int getResultSetNumberOfColumns(final ResultSet resultSet) {
            int intColumns = -1;
            try {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                intColumns = resultSetMetaData.getColumnCount();
            } catch (SQLException e) {
                final String strFeedback = String.format(STR_I18N_STM_UNB, "columns", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return intColumns;
        }

        /**
         * get # of Columns from ResultSet
         *
         * @param resultSet result-set
         * @return number of rows
         */
        private static int getResultSetNumberOfRows(final ResultSet resultSet) {
            int intResultSetRows = -1;
            try {
                if (intRows == 0) {
                    intResultSetRows = resultSet.getFetchSize() + 1;
                } else {
                    intResultSetRows = intRows;
                }
            } catch (SQLException e) {
                final String strFeedback = String.format(STR_I18N_STM_UNB, "rows", e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return intResultSetRows;
        }

        /**
         * ResultSet capturing standardized
         * @param objStatement statement
         * @param rsProperties result set Properties
         * @param queryProperties properties (with features to apply)
         * @return List of Properties
         */
        public static List<Properties> getResultSetStandardized(final Statement objStatement, final Properties rsProperties, final Properties queryProperties) {
            List<Properties> listReturn = null;
            final String strWhich = rsProperties.get("strWhich").toString();
            final String strQueryToUse = rsProperties.get("strQueryToUse").toString();
            final String strKind = rsProperties.get("strKind").toString();
            try (ResultSet rsStandard = executeCustomQuery(objStatement, strWhich, strQueryToUse, queryProperties)) {
                switch (strKind) {
                    case "Structure":
                        listReturn = getResultSetColumnStructure(rsStandard);
                        break;
                    case "Values":
                        listReturn = getResultSetColumnValuesWithNullCheck(rsStandard);
                        break;
                    default:
                        final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strKind, StackWalker.getInstance().walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                        LogExposureClass.LOGGER.error(strFeedback);
                        break;
                }
            } catch (SQLException e) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            return listReturn;
        }

        /**
         * Constructor
         */
        private ResultSettingClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private DatabaseOperationsClass() {
        // intentionally blank
    }
}

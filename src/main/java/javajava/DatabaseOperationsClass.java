package javajava;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import tools.jackson.databind.JsonNode;

/**
 * Database methods
 */
public final class DatabaseOperationsClass {
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
                ResultSettingClass.digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose));
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                LogExposureClass.exposeSqlExecutionSuccessInfo(strQueryPurpose);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(LocalizationClass.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose));
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
        final String strFeedbackPrmV = String.format(LocalizationClass.getMessage("i18nSQLparameterValuesAre"), valFields);
        LogExposureClass.LOGGER.debug(strFeedbackPrmV);
        final List<String> listMatches = BasicStructuresClass.ListAndMapClass.extractMatches(strOriginalQ, BasicStructuresClass.STR_PRMTR_RGX);
        final String strFeedbackPrm = String.format(LocalizationClass.getMessage("i18nSQLparameterForQueryAre"), listMatches);
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
        final String strFeedbackPrmM = String.format(LocalizationClass.getMessage("i18nSQLparameterMappingAre"), mapParameterOrder);
        LogExposureClass.LOGGER.debug(strFeedbackPrmM);
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder + " vs. " + objValues.getFirst().toString()
                , strOriginalQ);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Returns standard query
     * @param strWhich Which kind of query is needed
     * @return Query as String
     */
    public static String getPreDefinedQuery(final String strDatabaseType, final String strWhich) {
        String strFilePath = String.format("/SQL/%s/%s.sql", strDatabaseType, strWhich);
        long fileSizeActual = 0;
        if (ProjectClass.isRunningFromJar()) {
            try (InputStream inStream = Objects.requireNonNull(DatabaseOperationsClass.class.getResourceAsStream(strFilePath), "Resource not found: " + strFilePath)) {
                // transferTo returns the number of bytes transferred (Java 9+)
                fileSizeActual = inStream.transferTo(OutputStream.nullOutputStream());
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        } else {
            strFilePath = ProjectClass.getCurrentFolder() + "/src/main/resources" + strFilePath;
            fileSizeActual = FileStatisticsClass.RetrievingClass.getFileSizeIfFileExistsAndIsReadable(strFilePath);
        }
        final String strFeedback = String.format("Relevant query file is %s which has a size of %s bytes", strFilePath, fileSizeActual);
        LogExposureClass.LOGGER.debug(strFeedback);
        final long fileSizeLimit = 20;
        if (fileSizeActual < fileSizeLimit) {
            final String strFeedbackErr = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strWhich, StackWalker.getInstance()
                .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
            LogExposureClass.LOGGER.error(strFeedbackErr);
            throw new UnsupportedOperationException(strFeedbackErr);
        }
        return FileOperationsClass.ContentReadingClass.getFileContentIntoString(strFilePath);
    }

    /**
     * Package 3 String into Properties for result-set
     * @param strWhich
     * @param strQueryToUse relevant query
     * @param strKind 
     * @return Properties for result-set
     */
    private static Properties packageResultSetProperties(final String strWhich, final String strQueryToUse, final String strKind) {
        final Properties rsProperties = new Properties();
        rsProperties.put(
"Which", strWhich);
        rsProperties.put("QueryToUse", strQueryToUse);
        rsProperties.put("Kind", strKind);
        return rsProperties;
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenConnection.close();
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                givenStatement.close();
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            Statement objStatement = null;
            try {
                objStatement = connection.createStatement();
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
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
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLparameterBindingError")
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
        public static final String STR_I18N_STM_UNB = LocalizationClass.getMessage("i18nSQLstatementUnableToGetX");
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
                    final String strFeedbackC = String.format(LocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs);
                    LogExposureClass.LOGGER.error(strFeedbackC);
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        final String strFeedbackExR = String.format(LocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows);
                        LogExposureClass.LOGGER.error(strFeedbackExR);
                    }
                    break;
                case "exposeNumberOfColumns":
                    final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                    LogExposureClass.LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    final String strFeedbackN = String.format(LocalizationClass.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
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
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLqueryRuleEvaluation"), key);
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
                final String strFeedback = LocalizationClass.getMessage("i18nSQLresultSetNull");
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
            final String strWhich = rsProperties.get("Which").toString();
            final String strQueryToUse = rsProperties.get("QueryToUse").toString();
            final String strKind = rsProperties.get("Kind").toString();
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
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage());
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
     * MySQL methods
     */
    public static final class SpecificMySql {
        /**
         * Database MySQL
         */
        public static final String STR_DB_MYSQL = "MySQL";

        /**
         * Getting Connection Properties For MySQL from Environment variable
         * @return Properties
         */
        public static Properties getConnectionPropertiesForMySQL() {
            final Properties properties = new Properties();
            final String strEnv = "MYSQL";
            final String strEnvMySql = System.getenv(strEnv);
            if (strEnvMySql == null) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nEnvironmentVariableNotFound"), strEnv);
                LogExposureClass.LOGGER.error(strFeedback);
            } else {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nEnvironmentVariableFound"), strEnv);
                LogExposureClass.LOGGER.debug(strFeedback);
                final InputStream inputStream = new ByteArrayInputStream(strEnvMySql.getBytes());
                final JsonNode ndMySQL = JsonOperationsClass.getJsonFileNodes(inputStream);
                properties.put("ServerName", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerName"));
                properties.put("Port", JsonOperationsClass.getJsonValue(ndMySQL, "/Port"));
                properties.put("Username", JsonOperationsClass.getJsonValue(ndMySQL, "/Username"));
                properties.put("Password", JsonOperationsClass.getJsonValue(ndMySQL, "/Password"));
                properties.put("ServerTimezone", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerTimezone"));
            }
            return properties;
        }

        /**
         * Initiate a MySQL connection with Instance properties and DB specified
         *
         * @param propInstance Properties for Instance
         * @param strDatabase Database to connect to
         * @return Connection
         */
        public static Connection getMySqlConnection(final Properties propInstance, final String strDatabase) {
            Connection connection = null;
            if (propInstance.isEmpty()) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionPropertiesEmpty"), STR_DB_MYSQL);
                LogExposureClass.LOGGER.error(strFeedbackErr);
            } else {
                final String strServer = propInstance.get("ServerName").toString();
                final int strPort = BasicStructuresClass.convertStringIntoInteger(propInstance.get("Port").toString());
                try {
                    final String strConnection = String.format("jdbc:mysql://%s:%s/%s", strServer, strPort, strDatabase);
                    final Properties propConnection = getMySqlProperties(propInstance);
                    final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationAttempt"), STR_DB_MYSQL, strDatabase, strConnection, BasicStructuresClass.StringTransformationClass.obfuscateProperties(propConnection));
                    LogExposureClass.LOGGER.debug(strFeedback);
                    connection = DriverManager.getConnection(strConnection, propConnection);
                    final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationSuccess"), STR_DB_MYSQL, strServer, strPort, strDatabase);
                    LogExposureClass.LOGGER.debug(strFeedbackOk);
                } catch(SQLException e) {
                    final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationFailed"), STR_DB_MYSQL, strServer, strPort, strDatabase, e.getLocalizedMessage());
                    LogExposureClass.LOGGER.debug(strFeedbackErr);
                }
            }
            return connection;
        }

        /**
         * get standardized Information from MySQL
         *
         * @param objStatement Statement
         * @param strWhich Which query is needed
         * @param strKind which type of output would be needed
         * @return List with Properties
         */
        public static List<Properties> getMySqlPreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
            final String strQueryToUse = getPreDefinedQuery(STR_DB_MYSQL, strWhich);
            final Properties rsProperties = packageResultSetProperties(strWhich, strQueryToUse, strKind);
            final Properties queryProperties = new Properties();
            return ResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
        }

        /**
         * get MySQL Properties
         *
         * @param propInstance Instance Properties
         * @return Properties
         */
        private static Properties getMySqlProperties(final Properties propInstance) {
            final Properties properties = new Properties();
            properties.put("user", propInstance.get("Username").toString());
            properties.put("password", propInstance.get("Password").toString());
            properties.put("serverTimezone", propInstance.get("ServerTimezone").toString());
            properties.put("autoReconnect", true);
            properties.put("allowPublicKeyRetrieval", true);
            properties.put("useSSL", false);
            properties.put("useUnicode", true);
            properties.put("useJDBCCompliantTimezoneShift", true);
            properties.put("useLegacyDatetimeCode", false);
            properties.put("characterEncoding", "UTF-8");
            return properties;
        }

        /**
         * Execute MySQL pre-defined actions
         *
         * @param strWhich Which kind of query is needed
         * @param givenProperties Connection Properties
         */
        public static void performMySqlPreDefinedAction(final String strWhich, final Properties givenProperties) {
            try (Connection objConnection = getMySqlConnection(givenProperties, "mysql");
                Statement objStatement = ConnectivityClass.createSqlStatement(STR_DB_MYSQL, objConnection)) {
                final List<Properties> listProps = getMySqlPreDefinedInformation(objStatement, strWhich, "Values");
                final String strFeedback = listProps.toString();
                LogExposureClass.LOGGER.info(strFeedback);
            } catch(SQLException e) {
                final String strFeedbackErr = String.format("Error %s", Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
        }

        /**
         * constructor
         */
        private SpecificMySql() {
            // intentionally blank
        }
    }

    /**
     * Snowflake methods
     */
    public static final class SpecificSnowflakeClass {
        /**
         * Database Snowflake
         */
        public static final String STR_DB_SNOWFLAKE = "Snowflake";
        /**
         * standard String
         */
        public static final String STR_ROLES = "Roles";
        /**
         * Snowflake JDBC version
         */
        private static String jdbcVersion;

        /**
         * Snowflake Bootstrap
         *
         * @param objStatement statement
         */
        public static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
            final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
            executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
        }

        /**
         * Initiate a Snowflake connection with Instance properties and DB specified
         *
         * @param propInstance instance properties
         * @param strDatabase database to connect to
         * @return Connection
         */
        public static Connection getSnowflakeConnection(final Properties propInstance, final String strDatabase) {
            loadSnowflakeDriver();
            Connection connection = null;
            final String strConnection = String.format("jdbc:snowflake://%s.snowflakecomputing.com/", propInstance.get("AccountName").toString().replace("\"", ""));
            final Properties propConnection = getSnowflakeProperties(strDatabase, propInstance);
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationAttempt"), STR_DB_SNOWFLAKE, strDatabase, strConnection, propConnection);
            LogExposureClass.LOGGER.debug(strFeedback);
            try {
                connection = DriverManager.getConnection(strConnection, propConnection);
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationSuccessLight"), STR_DB_SNOWFLAKE, strDatabase);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch(SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationFailedLight"), STR_DB_SNOWFLAKE, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return connection;
        }

        /**
         * Retrieving Snowflake JDBC driver version
         * @return String
         */
        private static String getSnowflakeJdbcDriverVersion() {
            final String vSnowflakeId = "snowflake.jdbc";
            String vJdbcVersion = null;
            String vFoundIn = null;
            final Map<String, Object> moduleMap = ProjectClass.getProjectModuleLibraries();
            if (moduleMap.containsKey(vSnowflakeId)) {
                vJdbcVersion = moduleMap.get(vSnowflakeId).toString();
                vFoundIn = "Modules";
            } else {
                ProjectClass.loadProjectModel();
                ProjectClass.Loaders.loadComponents();
                final Map<String, Object> projDependencies = ProjectClass.Components.getProjectModelComponent("Dependencies");
                if (projDependencies.containsKey(vSnowflakeId)) {
                    vJdbcVersion = projDependencies.get(vSnowflakeId).toString();
                    vFoundIn = "Dependencies";
                }
            }
            final String strFeedback = String.format("I have found Snowflake JDBC driver v.%s from %s", vJdbcVersion, vFoundIn);
            LogExposureClass.LOGGER.debug(strFeedback);
            return vJdbcVersion;
        }

        /**
         * get standardized Information from Snowflake
         *
         * @param objStatement statement
         * @param strWhich which action
         * @param strKind kind of output
         * @return List of Properties
         */
        public static List<Properties> getSnowflakePreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
            final Properties queryProperties = new Properties();
            if (STR_ROLES.equalsIgnoreCase(strWhich)) {
                queryProperties.put("expectedExactNumberOfColumns", "1");
            }
            final String strQueryToUse = getPreDefinedQuery(STR_DB_SNOWFLAKE, strWhich);
            final Properties rsProperties = packageResultSetProperties(strWhich, strQueryToUse, strKind);
            return ResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
        }

        /**
         * build Snowflake Properties
         *
         * @param propInstance instance properties
         * @return Properties
         */
        public static Properties getSnowflakeProperties(final Properties propInstance) {
            final String strDatabase = propInstance.get("Default Database").toString().replace("\"", "");
            return getSnowflakeProperties(strDatabase, propInstance);
        }


        /**
         * build Snowflake Properties
         *
         * @param strDatabase Database name to connect to
         * @param propInstance instance properties
         * @return Properties
         */
        private static Properties getSnowflakeProperties(final String strDatabase, final Properties propInstance) {
            final Properties properties = new Properties();
            String currentUser = ShellingClass.getCurrentUserAccount();
            if (currentUser.isEmpty()) {
                currentUser = "UNKNOWN_USER";
            }
            properties.put("user", currentUser.toUpperCase(Locale.getDefault()));
            properties.put("db", strDatabase);
            String authValue = propInstance.get("Authenticator").toString().replace("\"", "");
            if (jdbcVersion.startsWith("4.0")
                    && "externalbrowser".equalsIgnoreCase(authValue)) {
                authValue = "EXTERNAL_BROWSER";
            }
            properties.put("authenticator", authValue);
            properties.put("role", propInstance.get("Role").toString().replace("\"", ""));
            properties.put("schema", propInstance.get("Schema").toString().replace("\"", ""));
            properties.put("warehouse", propInstance.get("Warehouse").toString().replace("\"", ""));
            properties.put("tracing", "SEVERE"); // to hide INFO and Warnings which are visible otherwise
            return properties;
        }

        /**
         * Loading Snowflake driver
         * if (jdbcVersion.startsWith("4.")) {
         *  strDriverName = "net.snowflake.client.api.driver.SnowflakeDriver";
         */
        private static void loadSnowflakeDriver() {
            jdbcVersion = getSnowflakeJdbcDriverVersion();
            final String strDriverName = "net.snowflake.client.jdbc.SnowflakeDriver";
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nSQLdriverLoadingAttempt"), STR_DB_SNOWFLAKE, strDriverName);
            LogExposureClass.LOGGER.debug(strFeedback);
            try {
                Class.forName(strDriverName);
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nSQLdriverLoadingSuccess"), STR_DB_SNOWFLAKE, strDriverName + " v. " + jdbcVersion);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (ClassNotFoundException ex) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLdriverLoadingNotFound"), STR_DB_SNOWFLAKE, strDriverName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
        }

        /**
         * Execute Snowflake pre-defined actions
         * @param strWhich which action to perform
         * @param objProps object properties
         */
        public static void performSnowflakePreDefinedAction(final String strWhich, final Properties objProps) {
            try (Connection objConnection = getSnowflakeConnection(objProps, objProps.get("databaseName").toString());
                Statement objStatement = DatabaseOperationsClass.ConnectivityClass.createSqlStatement(STR_DB_SNOWFLAKE, objConnection)) {
                executeSnowflakeBootstrapQuery(objStatement);
                getSnowflakePreDefinedInformation(objStatement, strWhich, "Values");
            } catch(SQLException e) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nError"), Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Constructor
         */
        private SpecificSnowflakeClass() {
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

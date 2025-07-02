package javajava;
/* SQL classes */
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
/* Time classes */
import java.time.LocalDateTime;
/* Utility classes */
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/* Logging */
import org.apache.logging.log4j.Level;

/**
 * Basic features for Databases 
 */
public class DatabaseResultSettingClass extends DatabaseBasicClass {

    /**
     * extends functionality for Executions
     * 
     * @param strPurpose purpose of query
     * @param resultSet result-set
     * @param objProperties properties (with features to apply)
     */
    private static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
        final int intResultSetRows = getResultSetNumberOfRows(resultSet);
        final int intColumnsIs = getResultSetNumberOfColumns(resultSet);
        for (final Object obj : objProperties.keySet()) {
            final String key = (String) obj;
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleEvaluation"), key);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
            switch (key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    if (intColumnsIs != intColumnsShould) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs);
                        LoggerLevelProvider.LOGGER.error(strFeedback);
                    }
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows);
                        LoggerLevelProvider.LOGGER.error(strFeedback);
                    }
                    break;
                case "exposeNumberOfColumns":
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                    LoggerLevelProvider.LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    final String strFeedbackN = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
                    LoggerLevelProvider.LOGGER.info(strFeedbackN);
                    break;
                default:
                    final String strFeedbackD = String.format(Common.STR_I18N_UNKN_FTS, key, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                        LoggerLevelProvider.LOGGER.error(strFeedbackD);
                    }
                    throw new UnsupportedOperationException(strFeedbackD);
            }
        }
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
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                    LoggerLevelProvider.LOGGER.debug(strFeedback);
                }
                digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                    LoggerLevelProvider.LOGGER.error(strFeedback);
                }
            }
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose));
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        }
        return resultSet;
    }

    /**
     * get structure from ResultSet
     * 
     * @param resultSet result-set
     * @return List of Properties
     */
    protected static List<Properties> getResultSetColumnStructure(final ResultSet resultSet) {
        final List<Properties> listResultSet = new ArrayList<>();
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
                final Properties colProperties = new Properties(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:12
                colProperties.put("Display Size", metaData.getColumnDisplaySize(columnNumber));
                colProperties.put("Name", metaData.getColumnName(columnNumber));
                colProperties.put("Precision", metaData.getPrecision(columnNumber));
                colProperties.put("Scale", metaData.getScale(columnNumber));
                colProperties.put("Type", metaData.getColumnTypeName(columnNumber));
                colProperties.put("Nullable", metaData.isNullable(columnNumber));
                listResultSet.add(colProperties);
            }
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLresultSetStructure"), listResultSet);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(Common.STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return listResultSet;
    }

    /**
     * get column values from ResultSet
     * 
     * @param resultSet result-set
     * @return List of Properties
     */
    private static List<Properties> getResultSetColumnValues(final ResultSet resultSet) {
        final List<Properties> listResultSet = new ArrayList<>();
        try {
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            final int columnCount = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                final Properties currentRow = new Properties(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:12
                for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                    String crtValue = resultSet.getString(colIndex);
                    if (resultSet.wasNull()) {
                        crtValue = Common.STR_NULL;
                    }
                    currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                }
                listResultSet.add(currentRow);
            }
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLresultSetListOfValues"), listResultSet);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(Common.STR_I18N_STM_UNB, "structures", e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
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
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = JavaJavaLocalization.getMessage("i18nSQLresultSetNull");
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
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
    @SuppressWarnings("unused")
    protected static List<String> getResultSetListOfStrings(final ResultSet resultSet) {
        final List<String> listStrings = new ArrayList<>();
        try {
            while (resultSet.next()) {
                listStrings.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(Common.STR_I18N_STM_UNB, "list of strings", e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
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
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(Common.STR_I18N_STM_UNB, "columns", e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
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
            intResultSetRows = resultSet.getFetchSize() + 1;
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(Common.STR_I18N_STM_UNB, "rows", e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return intResultSetRows;
    }

    /**
     * ResultSet capturing standardized
     * @param objStatement statement
     * @param strWhich output type from result-set
     * @param strQueryToUse query to use
     * @param queryProperties properties (with features to apply)
     * @param strKind type of result
     * @return List of Properties
     */
    protected static List<Properties> getResultSetStandardized(final Statement objStatement, final String strWhich, final String strQueryToUse, final Properties queryProperties, final String strKind) {
        List<Properties> listReturn = null;
        try (ResultSet rsStandard = executeCustomQuery(objStatement, strWhich, strQueryToUse, queryProperties)) {
            switch (strKind) {
                case "Structure":
                    listReturn = getResultSetColumnStructure(rsStandard);
                    break;
                case "Values":
                    listReturn = getResultSetColumnValuesWithNullCheck(rsStandard);
                    break;
                default:
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                        final String strFeedback = String.format(Common.STR_I18N_UNKN_FTS, strKind, StackWalker.getInstance()
                            .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
                        LoggerLevelProvider.LOGGER.error(strFeedback);
                    }
                    break;
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return listReturn;
    }

    /**
     * Constructor
     */
    public DatabaseResultSettingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }

}

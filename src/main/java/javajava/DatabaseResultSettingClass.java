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

/**
 * Basic features for Databases 
 */
public class DatabaseResultSettingClass extends DatabaseBasicClass {

    /**
     * extends functionality for Executions
     * 
     * @param strPurpose
     * @param resultSet
     * @param objProperties
     */
    private static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
        String strFeedback;
        final int intResultSetRows = getResultSetNumberOfRows(resultSet);
        final int intColumnsIs = getResultSetNumberOfColumns(resultSet);
        for (final Object obj : objProperties.keySet()) {
            final String key = (String) obj;
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryRuleEvaluation"), key);
            LOGGER.debug(strFeedback);
            switch (key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    if (intColumnsIs != intColumnsShould) {
                        strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs);
                        LOGGER.error(strFeedback);
                    }
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows);
                        LOGGER.error(strFeedback);
                    }
                    break;
                case "exposeNumberOfColumns":
                    strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                    LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
                    LOGGER.info(strFeedback);
                    break;
                default:
                    strFeedback = String.format(Common.strUnknFtrs, key, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                    LOGGER.error(strFeedback);
                    throw new UnsupportedOperationException(strFeedback);
            }
        }
    }

    /**
     * Execute a custom query with result-set expected
     * @param objStatement
     * @param strQueryPurpose
     * @param strQueryToUse
     * @param objProperties
     * @return ResultSet
     */
    public static ResultSet executeCustomQuery(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse, final Properties objProperties) {
        ResultSet resultSet = null;
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now();
            String strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
            LOGGER.debug(strFeedback);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                strFeedback = String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LOGGER.debug(strFeedback);
                digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                LOGGER.error(strFeedback);
            }
            TimingClass.logDuration(startTimeStamp, String.format(DanielLocalization.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose), "debug");
        }
        return resultSet;
    }

    /**
     * get structure from ResultSet
     * 
     * @param resultSet
     * @return List of Properties
     */
    protected static List<Properties> getResultSetColumnStructure(final ResultSet resultSet) {
        final List<Properties> listResultSet = new ArrayList<>();
        String strFeedback;
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
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLresultSetStructure"), listResultSet);
            LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format(Common.strStatementUnableX, "structures", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return listResultSet;
    }

    /**
     * get column values from ResultSet
     * 
     * @param resultSet
     * @return List of Properties
     */
    public static List<Properties> getResultSetColumnValues(final ResultSet resultSet) {
        final List<Properties> listResultSet = new ArrayList<>();
        String strFeedback;
        try {
            if (resultSet == null) {
                strFeedback = DanielLocalization.getMessage("i18nSQLresultSetNull");
                LOGGER.debug(strFeedback);
            } else {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                while (resultSet.next()) {
                    final Properties currentRow = new Properties(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:12
                    for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                        String crtValue = resultSet.getString(colIndex);
                        if (resultSet.wasNull()) {
                            crtValue = "NULL";
                        }
                        currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                    }
                    listResultSet.add(currentRow);
                }
                strFeedback = String.format(DanielLocalization.getMessage("i18nSQLresultSetListOfValues"), listResultSet);
                LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            strFeedback = String.format(Common.strStatementUnableX, "structures", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return listResultSet;
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
                listStrings.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            final String strFeedback = String.format(Common.strStatementUnableX, "list of strings", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return listStrings;
    }

    /**
     * get # of Columns from ResultSet 
     * 
     * @param resultSet
     * @return number of columns
     */
    private static int getResultSetNumberOfColumns(final ResultSet resultSet) {
        int intColumns = -1;
        try {
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            intColumns = resultSetMetaData.getColumnCount();
        } catch (SQLException e) {
            final String strFeedback = String.format(Common.strStatementUnableX, "columns", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return intColumns;
    }

    /**
     * get # of Columns from ResultSet 
     * 
     * @param resultSet
     * @return number of rows
     */
    private static int getResultSetNumberOfRows(final ResultSet resultSet) {
        int intResultSetRows = -1;
        try {
            intResultSetRows = resultSet.getFetchSize() + 1;
        } catch (SQLException e) {
            final String strFeedback = String.format(Common.strStatementUnableX, "rows", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return intResultSetRows;
    }

    /**
     * get standardized Information
     * @param objStatement
     * @param strWhich
     * @param strQueryToUse
     * @param queryProperties
     * @param strKind
     */
    protected static List<Properties> getResultSetStandardized(final Statement objStatement, final String strWhich, final String strQueryToUse, final Properties queryProperties, final String strKind) {
        String strFeedback;
        List<Properties> listReturn = null;
        try (ResultSet rsStandard = executeCustomQuery(objStatement, strWhich, strQueryToUse, queryProperties)) {
            switch(strKind) {
                case "Structure":
                    listReturn = getResultSetColumnStructure(rsStandard);
                    break;
                case "Values":
                    listReturn = getResultSetColumnValues(rsStandard);
                    break;
                default:
                    strFeedback = String.format(Common.strUnknFtrs, strKind, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                    LOGGER.error(strFeedback);
                    break;
            }
        } catch (SQLException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return listReturn;
    }

}

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
     * @param strPurpose purpose of query
     * @param resultSet result-set
     * @param objProperties properties (with features to apply)
     */
    private static void digestCustomQueryProperties(final String strPurpose, final ResultSet resultSet, final Properties objProperties) {
        final int intResultSetRows = getResultSetNumberOfRows(resultSet);
        final int intColumnsIs = getResultSetNumberOfColumns(resultSet);
        for (final Object obj : objProperties.keySet()) {
            final String key = (String) obj;
            String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleEvaluation"), key);
            Common.levelProvider.logDebug(strFeedback);
            switch (key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs);
                    Common.levelProvider.logError(strFeedback);
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows);
                    Common.levelProvider.logError(strFeedback);
                    break;
                case "exposeNumberOfColumns":
                    strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                    Common.levelProvider.logInfo(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
                    Common.levelProvider.logInfo(strFeedback);
                    break;
                default:
                    strFeedback = String.format(Common.strUnknFtrs, key, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                    Common.levelProvider.logError(strFeedback);
                    throw new UnsupportedOperationException(strFeedback);
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
            String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
            Common.levelProvider.logDebug(strFeedback);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                Common.levelProvider.logDebug(strFeedback);
                digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                Common.levelProvider.logError(strFeedback);
            }
            strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose));
            Common.levelProvider.logDebug(strFeedback);
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
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLresultSetStructure"), listResultSet);
            Common.levelProvider.logDebug(strFeedback);
        } catch (SQLException e) {
            final String strFeedback = String.format(Common.strStmntUnableX, "structures", e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
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
            if (resultSet == null) {
                final String strFeedback = JavaJavaLocalization.getMessage("i18nSQLresultSetNull");
                Common.levelProvider.logDebug(strFeedback);
            } else {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                while (resultSet.next()) {
                    final Properties currentRow = new Properties(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:12
                    for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                        String crtValue = resultSet.getString(colIndex);
                        if (resultSet.wasNull()) {
                            crtValue = Common.strNull;
                        }
                        currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                    }
                    listResultSet.add(currentRow);
                }
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLresultSetListOfValues"), listResultSet);
                Common.levelProvider.logDebug(strFeedback);
            }
        } catch (SQLException e) {
            final String strFeedback = String.format(Common.strStmntUnableX, "structures", e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
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
            final String strFeedback = String.format(Common.strStmntUnableX, "list of strings", e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
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
            final String strFeedback = String.format(Common.strStmntUnableX, "columns", e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
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
            final String strFeedback = String.format(Common.strStmntUnableX, "rows", e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
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
                    listReturn = getResultSetColumnValues(rsStandard);
                    break;
                default:
                    final String strFeedback = String.format(Common.strUnknFtrs, strKind, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                    Common.levelProvider.logError(strFeedback);
                    break;
            }
        } catch (SQLException e) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage());
            Common.levelProvider.logError(strFeedback);
        }
        return listReturn;
    }

    /**
     * Constructor
     */
    public DatabaseResultSettingClass() { // NOPMD by Daniel Popiniuc on 18.05.2025, 04:19
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }

}

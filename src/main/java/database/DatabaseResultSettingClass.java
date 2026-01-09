package database;

import javajava.CommonClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposure;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Basic features for Databases 
 */
public final class DatabaseResultSettingClass {

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
     * capture to Log resultset properties
     * @param key current key within loop
     * @param strPurpose query purpose for log text
     * @param objProperties object properties
     */
    private static void captureToLogResultsetAttributes(final String key, final String strPurpose, final Properties objProperties) {
        switch (key) {
            case "expectedExactNumberOfColumns":
                final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                if (intColumnsIs != intColumnsShould) {
                    LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingColumns"), strPurpose, intColumnsShould, intColumnsIs));
                }
                break;
            case "expectedExactNumberOfRows":
                final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                if (intResultSetRows != intExpectedRows) {
                    LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleUnmatchingRows"), strPurpose, intExpectedRows, intResultSetRows));
                }
                break;
            case "exposeNumberOfColumns":
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleExposingColumns"), intColumnsIs);
                LogExposure.exposeMessageToInfoLog(strFeedback);
                break;
            case "exposeNumberOfRows":
                final String strFeedbackN = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleExposingRows"), intResultSetRows);
                LogExposure.exposeMessageToInfoLog(strFeedbackN);
                break;
            default:
                final String strFeedbackD = String.format(CommonClass.STR_I18N_UNKN_FTS, key, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(CommonClass.STR_I18N_UNKN)));
                LogExposure.exposeMessageToErrorLog(strFeedbackD);
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
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryRuleEvaluation"), key));
            captureToLogResultsetAttributes(key, strPurpose, objProperties);
        }
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
            LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_STM_UNB, "structures", e.getLocalizedMessage()));
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
            final Properties currentRow = new Properties();
            while (resultSet.next()) {
                currentRow.clear();
                for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                    String crtValue = resultSet.getString(colIndex);
                    if (resultSet.wasNull()) {
                        crtValue = CommonClass.STR_NULL;
                    }
                    currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                }
                listResultSet.add(currentRow);
                intRow++;
            }
            LogExposure.exposeMessageToDebugLog(String.format("I have found %d records", intRow));
            intRows = intRow;
        } catch (SQLException e) {
            LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_STM_UNB, "structures", e.getLocalizedMessage()));
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
            LogExposure.exposeMessageToErrorLog(JavaJavaLocalizationClass.getMessage("i18nSQLresultSetNull"));
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
            LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_STM_UNB, "list of strings", e.getLocalizedMessage()));
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
            LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_STM_UNB, "columns", e.getLocalizedMessage()));
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
            LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_STM_UNB, "rows", e.getLocalizedMessage()));
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
        try (ResultSet rsStandard = DatabaseBasicClass.executeCustomQuery(objStatement, strWhich, strQueryToUse, queryProperties)) {
            switch (strKind) {
                case "Structure":
                    listReturn = getResultSetColumnStructure(rsStandard);
                    break;
                case "Values":
                    listReturn = getResultSetColumnValuesWithNullCheck(rsStandard);
                    break;
                default:
                    LogExposure.exposeMessageToErrorLog(String.format(CommonClass.STR_I18N_UNKN_FTS, strKind, StackWalker.getInstance()
                            .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(CommonClass.STR_I18N_UNKN))));
                    break;
            }
        } catch (SQLException e) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementExecutionError"), strWhich, e.getLocalizedMessage()));
        }
        return listReturn;
    }

    /**
     * Constructor
     */
    private DatabaseResultSettingClass() {
        // intentionally blank
    }

}

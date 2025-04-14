package danielgp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Basic features for Databases 
 */
public class DatabaseResultSetingClass extends DatabaseBasicClass {

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
        final Iterator<Object> keyIterator = objProperties.keySet().iterator();
        while(keyIterator.hasNext()){
            final String key = (String) keyIterator.next();
            strFeedback = String.format("Evaluating ResultSet for %s...", key);
            LogHandlingClass.LOGGER.debug(strFeedback);
            switch(key) {
                case "expectedExactNumberOfColumns":
                    final int intColumnsShould = Integer.parseInt(objProperties.getProperty(key));
                    if (intColumnsIs != intColumnsShould) {
                        strFeedback = String.format("For the \"%s\" query the Resultset was expected to have exact %s column(s) but a %s were found...", strPurpose, intColumnsShould, intColumnsIs);
                        LogHandlingClass.LOGGER.error(strFeedback);
                    }
                    break;
                case "expectedExactNumberOfRows":
                    final int intExpectedRows = Integer.parseInt(objProperties.getProperty(key));
                    if (intResultSetRows != intExpectedRows) {
                        strFeedback = String.format("For the \"%s\" query the Resultset was expected to have exact %s row(s) but a %s was/were found...", strPurpose, intExpectedRows, intResultSetRows);
                        LogHandlingClass.LOGGER.error(strFeedback);
                    }
                    break;
                case "exposeNumberOfColumns":
                    strFeedback = String.format("Number of columns retrieved is %d", intColumnsIs);
                    LogHandlingClass.LOGGER.info(strFeedback);
                    break;
                case "exposeNumberOfRows":
                    strFeedback = String.format("Number of rows retrieved is %d", intResultSetRows);
                    LogHandlingClass.LOGGER.info(strFeedback);
                    break;
                default:
                    strFeedback = String.format("Feature %s is NOT known...", key);
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
            String strFeedback = String.format("Will execute %s query which is defined as: %s", strQueryPurpose, strQueryToUse);
            LogHandlingClass.LOGGER.debug(strFeedback);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                strFeedback = String.format("Executing %s query was successful!", strQueryPurpose);
                LogHandlingClass.LOGGER.debug(strFeedback);
                digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                strFeedback = String.format("Statement execution for %s has failed with following error: %s", strQueryPurpose, e.getLocalizedMessage());
                LogHandlingClass.LOGGER.error(strFeedback);
            }
            TimingClass.logDuration(startTimeStamp, String.format("Finished executing %s query", strQueryPurpose), "debug");
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
            final Integer columnCount = metaData.getColumnCount();
            for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
                final Properties colProperties = new Properties();
                colProperties.put("Display Size", metaData.getColumnDisplaySize(columnNumber));
                colProperties.put("Name", metaData.getColumnName(columnNumber));
                colProperties.put("Precision", metaData.getPrecision(columnNumber));
                colProperties.put("Scale", metaData.getScale(columnNumber));
                colProperties.put("Type", metaData.getColumnTypeName(columnNumber));
                colProperties.put("Nullable", metaData.isNullable(columnNumber));
                listResultSet.add(colProperties);
            }
            strFeedback = String.format("Current ResultSet structure is : %s", listResultSet.toString());
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (SQLException e) {
            strFeedback = String.format("Unable to get ResultSet structures and error is: %s", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return listResultSet;
    }

    /**
     * get column values from ResultSet
     * 
     * @param resultSet
     * @return List of Properties
     */
    protected static List<Properties> getResultSetColumnValues(final ResultSet resultSet) {
        final List<Properties> listResultSet = new ArrayList<>();
        String strFeedback;
        try {
            if (resultSet == null) {
                strFeedback = "ResultSet is null";
                LogHandlingClass.LOGGER.debug(strFeedback);
            } else {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final Integer columnCount = resultSetMetaData.getColumnCount();
                while (resultSet.next()) {
                    final Properties currentRow = new Properties();
                    for (int colIndex = 1; colIndex <= columnCount; colIndex++) {
                        String crtValue = resultSet.getString(colIndex);
                        if (resultSet.wasNull()) {
                            crtValue = "NULL";
                        }
                        currentRow.put(resultSetMetaData.getColumnName(colIndex), crtValue);
                    }
                    listResultSet.add(currentRow);
                }
                strFeedback = String.format("Final list of values is %s", listResultSet.toString());
                LogHandlingClass.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            strFeedback = String.format("Unable to get ResultSet structures and error is: %s", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
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
            final String strFeedback = String.format("Unable to get list of strings from ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
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
            final String strFeedback = String.format("Unable to get the # of columns in the ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
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
            final String strFeedback = String.format("Unable to get the # of columns in the ResultSet...", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return intResultSetRows;
    }

    /**
     * constructor
     */
    protected DatabaseResultSetingClass() {
        super();
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

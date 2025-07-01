package javajava;
/* SQL classes */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
/* Utility classes */
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
/* Logging */
import org.apache.logging.log4j.Level;

/**
 * Database Query Binding 
 */
public final class DatabaseQueryBindingClass {

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
        final List<String> mapParameterOrder = DatabaseBasicClass.getPromptParametersOrderWithinQuery(strQuery, objValues);
        final int intParameters = mapParameterOrder.size();
        final String strFinalQ = Common.convertPromptParametersIntoParameters(strQuery);
        try (PreparedStatement preparedStatement = objConnection.prepareStatement(strFinalQ);) {
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
                    bindSingleParameter(preparedStatement, properties, arrayCleanable, arrayNullable);
                }
                preparedStatement.addBatch();
                if ((crtRow % 200 == 0)
                    || (crtRow == intRows)) { // each 200 rows OR final one
                    preparedStatement.executeLargeBatch();
                    setSqlExecutionSuccessInfo(strQueryPurpose);
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
     * @param arrayCleanable clean-able list of fields
     * @param arrayNullable NULL-able list of fields
     * @return PreparedStatement
     */
    private static PreparedStatement bindSingleParameter(final PreparedStatement preparedStatement, final Properties properties, final String[] arrayCleanable, final String... arrayNullable) {
        final int index = Integer.parseInt(properties.get("index").toString());
        final String strKey = properties.get("strKey").toString();
        final String strQuery = properties.get("strQuery").toString();
        final String strOriginalValue = properties.get("strOriginalValue").toString();
        final String strValueToUse = strOriginalValue;
        try {
            if (Common.STR_NULL.equalsIgnoreCase(strOriginalValue)) {
                preparedStatement.setNull(index, Types.VARCHAR);
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                final String strCleanedValue = strOriginalValue.replaceAll("([\"'])", "");
                if (strCleanedValue.isEmpty()) {
                    preparedStatement.setNull(index, Types.VARCHAR);
                } else {
                    preparedStatement.setString(index, strCleanedValue);
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                preparedStatement.setNull(index, Types.VARCHAR);
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                preparedStatement.setString(index, strOriginalValue.replace("\"", "\"\""));
            } else {
                preparedStatement.setString(index, strValueToUse);
            }
        } catch (SQLException e) {
            setSqlParameterBindingError(e, strKey, strQuery);
        }
        return preparedStatement;
    }

    /**
     * Error logging the SQL Exception
     * @param e exception object
     * @param objValues values provided
     * @param strQuery relevant query
     */
    private static void setSqlExceptionError(final SQLException exptObj, final List<Properties> objValues, final String strQuery) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = exptObj.getLocalizedMessage() + " with Values " + objValues.get(0).toString() + " for Query " + strQuery;
            LoggerLevelProvider.LOGGER.error(strFeedback);
        }
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose
     */
    private static void setSqlExecutionSuccessInfo(final String strQueryPurpose) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose
     */
    private static void setSqlParameterBindingError(final SQLException exptObj, final String strParameterName, final String strQuery) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterBindingError")
                , exptObj.getLocalizedMessage()
                , strParameterName
                , strQuery);
            LoggerLevelProvider.LOGGER.error(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private DatabaseQueryBindingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }

}

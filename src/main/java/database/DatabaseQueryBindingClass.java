package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import structure.StringManipulationClass;

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
     * @param specialFields Clean-able and Null-able fields
     */
    public static void executeValuesIntoDatabaseUsingPreparedStatement(final Connection objConnection, final String strQueryPurpose, final List<Properties> objValues, final String strQuery, final Properties specialFields) {
        final int intRows = objValues.size();
        final List<String> mapParameterOrder = DatabaseBasicClass.getPromptParametersOrderWithinQuery(strQuery, objValues);
        final int intParameters = mapParameterOrder.size();
        final String strFinalQ = StringManipulationClass.TransformingClass.convertPromptParametersIntoParameters(strQuery);
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
            if (DatabaseBasicClass.STR_NULL.equalsIgnoreCase(strOriginalValue) 
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
    private DatabaseQueryBindingClass() {
        // intentionally blank
    }

}

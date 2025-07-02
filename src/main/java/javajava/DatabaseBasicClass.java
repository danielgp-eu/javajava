package javajava;
/* SQL classes */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/* Time classes */
import java.time.LocalDateTime;
/* Utility classes */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
/* Logging */
import org.apache.logging.log4j.Level;

/**
 * Database methods
 */
public final class DatabaseBasicClass {

    /**
     * Fill values into a dynamic query 
     * @param queryProperties properties for connection
     * @param strRawQuery raw query
     * @param arrayCleanable array with fields to clean
     * @param arrayNullable array with NULL-able fields
     * @return final query
     */
    @SuppressWarnings("unused")
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format(Common.STR_QTD_STR_VL, strOriginalValue);
            if (strOriginalValue.matches(Common.STR_NULL)) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format(Common.STR_QTD_STR_VL, strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = Common.STR_NULL;
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = Common.STR_NULL;
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                strValueToUse = String.format(Common.STR_QTD_STR_VL, strOriginalValue.replace("\"", "\"\""));
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
                DatabaseResultSettingClass.digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
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
     * Execute a custom query w/o any result-set
     * 
     * @param objStatement statement
     * @param strQueryPurpose purpose of query
     * @param strQueryToUse query to use
     */
    public static void executeQueryWithoutResultSet(final Statement objStatement, final String strQueryPurpose, final String strQueryToUse) {
        if (strQueryToUse != null) {
            final LocalDateTime startTimeStamp = LocalDateTime.now();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                setSqlExecutionSuccessInfo(strQueryPurpose);
            } catch (SQLException e) {
                if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                    LoggerLevelProvider.LOGGER.error(strFeedback);
                }
            }
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose));
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterValuesAre"), valFields.toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> listMatches = Common.extractMatches(strOriginalQ, Common.STR_PRMTR_RGX);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterForQueryAre"), listMatches.toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (int intParameter = 0; intParameter < intParameters; intParameter++) {
            final String crtParameter = StringManipulationClass.cleanStringFromCurlyBraces(listMatches.get(intParameter));
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterMappingAre"), mapParameterOrder.toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final int foundParameters = mapParameterOrder.size();
        if ((foundParameters != intParameters)  && LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder.toString() + " vs. " + objValues.get(0).toString()
                , strOriginalQ);
            LoggerLevelProvider.LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose
     */
    public static void setSqlExecutionSuccessInfo(final String strQueryPurpose) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private DatabaseBasicClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

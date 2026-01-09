package database;

import javajava.CommonClass;
import javajava.ListAndMapClass;
import javajava.StringManipulationClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposure;
import time.TimingClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
    public static String distributePropertiesToQuery(final Properties queryProperties, final String strRawQuery, final String[] arrayCleanable, final String... arrayNullable) {
        String strQueryToReturn = strRawQuery;
        for (final Object obj : queryProperties.keySet()) {
            final String strKey = (String) obj;
            final String strOriginalValue = queryProperties.getProperty(strKey);
            String strValueToUse = String.format(CommonClass.STR_QTD_STR_VL, strOriginalValue);
            if (strOriginalValue.matches(CommonClass.STR_NULL)) {
                strValueToUse = strOriginalValue;
            } else if (Arrays.asList(arrayCleanable).contains(strKey)) {
                strValueToUse = String.format(CommonClass.STR_QTD_STR_VL, strOriginalValue.replaceAll("([\"'])", ""));
                if (strOriginalValue.isEmpty()) {
                    strValueToUse = CommonClass.STR_NULL;
                }
            } else if (Arrays.asList(arrayNullable).contains(strKey) && strOriginalValue.isEmpty()) {
                strValueToUse = CommonClass.STR_NULL;
            } else if (strKey.contains("_JSON") || strKey.startsWith("JSON_")) {
                strValueToUse = String.format(CommonClass.STR_QTD_STR_VL, strOriginalValue.replace("\"", "\"\""));
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
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse));
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose));
                DatabaseResultSettingClass.digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage()));
            }
            LogExposure.exposeMessageToDebugLog(TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose)));
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
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose));
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                setSqlExecutionSuccessInfo(strQueryPurpose);
            } catch (SQLException e) {
                LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace())));
            }
            LogExposure.exposeMessageToDebugLog(TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose)));
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
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValuesAre"), valFields));
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, CommonClass.STR_PRMTR_RGX);
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterForQueryAre"), listMatches));
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (final String listMatch : listMatches) {
            final String crtParameter = StringManipulationClass.cleanStringFromCurlyBraces(listMatch);
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterMappingAre"), mapParameterOrder));
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder + " vs. " + objValues.getFirst().toString()
                , strOriginalQ);
            LogExposure.exposeMessageToErrorLog(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose Query purpose
     */
    public static void setSqlExecutionSuccessInfo(final String strQueryPurpose) {
        LogExposure.exposeMessageToInfoLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose));
    }

    /**
     * Constructor
     */
    private DatabaseBasicClass() {
        // intentionally blank
    }
}

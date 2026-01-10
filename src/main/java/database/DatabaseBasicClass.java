package database;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import structure.ListAndMapClass;
import structure.StringManipulationClass;
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
     * Database MySQL
     */
    public static final String STR_DB_MYSQL = "MySQL";
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
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttemptPurpose"), strQueryPurpose, strQueryToUse);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                resultSet = objStatement.executeQuery(strQueryToUse);
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
                DatabaseResultSettingClass.digestCustomQueryProperties(strQueryPurpose, resultSet, objProperties);
            } catch (SQLException e) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementExecutionError"), strQueryPurpose, e.getLocalizedMessage());
                LogExposureClass.LOGGER.error(strFeedback);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinishedDuration"), strQueryPurpose));
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
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionAttempt"), strQueryPurpose);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            try {
                if (strQueryToUse.startsWith("INSERT INTO")) {
                    objStatement.executeLargeUpdate(strQueryToUse);
                } else {
                    objStatement.execute(strQueryToUse);
                }
                setSqlExecutionSuccessInfo(strQueryPurpose);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionError"), strQueryPurpose, e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            final String strFeedbackEnd = TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionFinished"), strQueryPurpose));
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
        final String strFeedbackPrmV = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValuesAre"), valFields);
        LogExposureClass.LOGGER.debug(strFeedbackPrmV);
        final List<String> listMatches = ListAndMapClass.extractMatches(strOriginalQ, StringManipulationClass.STR_PRMTR_RGX);
        final String strFeedbackPrm = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterForQueryAre"), listMatches);
        LogExposureClass.LOGGER.debug(strFeedbackPrm);
        final List<String> mapParameterOrder = new ArrayList<>();
        final int intParameters = listMatches.size();
        for (final String listMatch : listMatches) {
            final String crtParameter = StringManipulationClass.cleanStringFromCurlyBraces(listMatch);
            final int intPosition = valFields.indexOf(crtParameter);
            if (intPosition != -1) {
                mapParameterOrder.add(crtParameter);
            }
        }
        final String strFeedbackPrmM = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterMappingAre"), mapParameterOrder);
        LogExposureClass.LOGGER.debug(strFeedbackPrmM);
        final int foundParameters = mapParameterOrder.size();
        if (foundParameters != intParameters) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLparameterValueMissing")
                , intParameters
                , foundParameters
                , mapParameterOrder + " vs. " + objValues.getFirst().toString()
                , strOriginalQ);
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return mapParameterOrder;
    }

    /**
     * Success confirmation to Info log
     * @param strQueryPurpose Query purpose
     */
    public static void setSqlExecutionSuccessInfo(final String strQueryPurpose) {
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLqueryExecutionSuccess"), strQueryPurpose);
        LogExposureClass.LOGGER.info(strFeedback);
    }

    /**
     * Constructor
     */
    private DatabaseBasicClass() {
        // intentionally blank
    }
}

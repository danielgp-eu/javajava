package database;

import org.apache.logging.log4j.Level;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;
import javajava.ShellingClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Snowflake methods
 */
public final class DatabaseSpecificSnowflake {
    /**
     * Map with predefined queries
     */
    private static final Map<String, String> STD_QUERY;

    static {
        // Initialize the concurrent map
        final Map<String, String> tempMap = new ConcurrentHashMap<>();
        tempMap.put("Columns", """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "ORDINAL_POSITION"
    , "COLUMN_NAME"
    , "DATA_TYPE"
    , CASE
        WHEN "DATA_TYPE" = 'TEXT'
            AND ("CHARACTER_MAXIMUM_LENGTH" >= 16777215)    THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" = 'TEXT'                           THEN
            'VARCHAR('
                || TO_CHAR("CHARACTER_MAXIMUM_LENGTH")
                || ')'
        WHEN "DATA_TYPE" = 'FLOAT'                          THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" = 'NUMBER'                         THEN
            'NUMBER('
                || TO_CHAR("NUMERIC_PRECISION")
                || ','
                || TO_CHAR("NUMERIC_SCALE")
                || ')'
        WHEN "DATA_TYPE" IN ('ARRAY'
            , 'BINARY'
            , 'BOOLEAN'
            , 'DATE'
            , 'OBJECT'
            , 'TIME'
            , 'VARIANT')                                    THEN
            "DATA_TYPE"
        WHEN "DATA_TYPE" IN ('TIMESTAMP'
            , 'TIMESTAMP_LTZ'
            , 'TIMESTAMP_NTZ'
            , 'TIMESTAMP_TZ')                               THEN
            "DATA_TYPE"
                || '('
                ||  "DATETIME_PRECISION"
                || ')'
        ELSE
            '???'
        END                 AS "Data_Type_Full"
    , "IS_NULLABLE"
    , "COLUMN_DEFAULT"
    , "COMMENT"
    , CURRENT_ACCOUNT()     AS "SNOWFLAKE_INSTANCE"
    , SYSDATE()             AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "INFORMATION_SCHEMA"."COLUMNS";""");
        tempMap.put("Databases", """
SELECT
      "DATABASE_NAME"
    , "DATABASE_OWNER"
    , "COMMENT"
    , "CREATED"
    , "LAST_ALTERED"
    , "RETENTION_TIME"
    , "TYPE"
    , CURRENT_ACCOUNT()     AS "SNOWFLAKE_INSTANCE"
    , SYSDATE()             AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "INFORMATION_SCHEMA"."DATABASES";""");
        tempMap.put("TablesAndViews", """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "TABLE_OWNER"
    , "TABLE_TYPE"
    , "IS_TRANSIENT"
    , "CLUSTERING_KEY"
    , "ROW_COUNT"
    , "BYTES"
    , "RETENTION_TIME"
    , "SELF_REFERENCING_COLUMN_NAME"
    , "REFERENCE_GENERATION"
    , "USER_DEFINED_TYPE_CATALOG"
    , "USER_DEFINED_TYPE_SCHEMA"
    , "USER_DEFINED_TYPE_NAME"
    , "IS_INSERTABLE_INTO"
    , "IS_TYPED"
    , "COMMIT_ACTION"
    , "CREATED"
    , "LAST_ALTERED"
    , "LAST_DDL"
    , "LAST_DDL_BY"
    , "AUTO_CLUSTERING_ON"
    , "COMMENT"
    , "IS_TEMPORARY"
    , "IS_ICEBERG"
    , "IS_DYNAMIC"
    , "IS_IMMUTABLE"
    , `IS_HYBRID`
    , CURRENT_ACCOUNT()     AS "SNOWFLAKE_INSTANCE"
    , SYSDATE()             AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "INFORMATION_SCHEMA"."TABLES";""");
        tempMap.put("Views", """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "TABLE_OWNER"
    , "VIEW_DEFINITION"
    , "IS_SECURE"
    , "CREATED"
    , "LAST_ALTERED"
    , "LAST_DDL"
    , "LAST_DDL_BY"
    , "COMMENT"
FROM
    "INFORMATION_SCHEMA"."VIEWS";""");
        tempMap.put("ViewsLight", """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "VIEW_DEFINITION"
FROM
    "INFORMATION_SCHEMA"."VIEWS";""");
        tempMap.put("Warehouses", "SHOW WAREHOUSES;");
        // Make the map unmodifiable
        STD_QUERY = Collections.unmodifiableMap(tempMap);
    }

    /**
     * Snowflake Bootstrap
     * 
     * @param objStatement statement
     */
    public static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
        final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
        DatabaseBasicClass.executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
    }

    /**
     * Initiate a Snowflake connection with Instance properties and DB specified
     * 
     * @param propInstance instance properties
     * @param strDatabase database to connect to
     * @return Connection
     */
    public static Connection getSnowflakeConnection(final Properties propInstance, final String strDatabase) {
        loadSnowflakeDriver();
        Connection connection = null;
        final String strConnection = String.format("jdbc:snowflake://%s.snowflakecomputing.com/", propInstance.get("AccountName").toString().replace("\"", ""));
        final Properties propConnection = getSnowflakeProperties(strDatabase, propInstance);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationAttempt"), Common.STR_DB_SNOWFLAKE, strDatabase, strConnection, propConnection);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationSuccessLight"), Common.STR_DB_SNOWFLAKE, strDatabase);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch(SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationFailedLight"), Common.STR_DB_SNOWFLAKE, e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return connection;
    }

    /**
     * get standardized Information from Snowflake
     * 
     * @param objStatement statement
     * @param strWhich which action
     * @param strKind kind of output
     * @return List of Properties
     */
    public static List<Properties> getSnowflakePreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
        final Properties queryProperties = new Properties();
        if (Common.STR_ROLES.equalsIgnoreCase(strWhich)) {
            queryProperties.put("expectedExactNumberOfColumns", "1");
        }
        final String strQueryToUse = getSnowflakePreDefinedMetadataQuery(strWhich);
        final Properties rsProperties = new Properties();
        rsProperties.put("strWhich", strWhich);
        rsProperties.put("strQueryToUse", strQueryToUse);
        rsProperties.put("strKind", strKind);
        return DatabaseResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
    }

    /**
     * returns standard Metadata query specific to Snowflake
     * 
     * @param strWhichQuery which action
     * @return String
     */
    public static String getSnowflakePreDefinedMetadataQuery(final String strWhichQuery) {
        final String strQueryToUse = STD_QUERY.get(strWhichQuery);
        if (strQueryToUse.isEmpty()) {
            final String strFeedback = String.format(Common.STR_I18N_UNKN_FTS, strWhichQuery, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.STR_I18N_UNKN)));
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
            throw new UnsupportedOperationException(strFeedback);
        }
        return strQueryToUse;
    }

    /**
     * build Snowflake Properties
     * 
     * @param propInstance instance properties
     * @return Properties
     */
    public static Properties getSnowflakeProperties(final Properties propInstance) {
        final String strDatabase = propInstance.get("Default Database").toString().replace("\"", "");
        return getSnowflakeProperties(strDatabase, propInstance);
    }


    /**
     * build Snowflake Properties
     * 
     * @param strDatabase Database name to connect to
     * @param propInstance instance properties
     * @return Properties
     */
    private static Properties getSnowflakeProperties(final String strDatabase, final Properties propInstance) {
        final Properties properties = new Properties();
        properties.put("user", ShellingClass.getCurrentUserAccount().toUpperCase(Locale.getDefault()));
        properties.put("db", strDatabase);
        properties.put("authenticator", propInstance.get("Authenticator").toString().replace("\"", ""));
        properties.put("role", propInstance.get("Role").toString().replace("\"", ""));
        properties.put("schema", propInstance.get("Schema").toString().replace("\"", ""));
        properties.put("warehouse", propInstance.get("Warehouse").toString().replace("\"", ""));
        properties.put("tracing", "SEVERE"); // to hide INFO and Warnings which are visible otherwise
        return properties;
    }

    /**
     * Loading Snowflake driver
     */
    private static void loadSnowflakeDriver() {
        final String strDriverName = "net.snowflake.client.jdbc.SnowflakeDriver";
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLdriverLoadingAttempt"), Common.STR_DB_SNOWFLAKE, strDriverName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        try {
            Class.forName(strDriverName);
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLdriverLoadingSuccess"), Common.STR_DB_SNOWFLAKE, strDriverName);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (ClassNotFoundException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLdriverLoadingNotFound"), Common.STR_DB_SNOWFLAKE, strDriverName);
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Execute Snowflake pre-defined actions
     * @param strWhich which action to perform
     * @param objProps object properties
     */
    public static void performSnowflakePreDefinedAction(final String strWhich, final Properties objProps) {
        try (Connection objConnection = getSnowflakeConnection(objProps, objProps.get("databaseName").toString());
            Statement objStatement = DatabaseConnectivity.createSqlStatement("Snowflake", objConnection)) {
            executeSnowflakeBootstrapQuery(objStatement);
            getSnowflakePreDefinedInformation(objStatement, strWhich, "Values");
        } catch(SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nError"), Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * constructor
     */
    private DatabaseSpecificSnowflake() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

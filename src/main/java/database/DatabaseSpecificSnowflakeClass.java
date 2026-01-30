package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import file.ProjectClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import shell.ShellingClass;

/**
 * Snowflake methods
 */
public final class DatabaseSpecificSnowflakeClass {
    /**
     * Database Snowflake
     */
    public static final String STR_DB_SNOWFLAKE = "Snowflake";
    /**
     * standard String
     */
    public static final String STR_ROLES = "Roles";
    /**
     * Map with predefined queries
     */
    private static final Map<String, String> STD_QUERY;
    /**
     * Snowflake JDBC version
     */
    private static String jdbcVersion;

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
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationAttempt"), STR_DB_SNOWFLAKE, strDatabase, strConnection, propConnection);
        LogExposureClass.LOGGER.debug(strFeedback);
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationSuccessLight"), STR_DB_SNOWFLAKE, strDatabase);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch(SQLException e) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationFailedLight"), STR_DB_SNOWFLAKE, e.getLocalizedMessage());
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
        return connection;
    }

    /**
     * Retrieving Snowflake JDBC driver version
     * @return String
     */
    private static String getSnowflakeJdbcDriverVersion() {
        final String vSnowflakeId = "snowflake.jdbc";
        String vJdbcVersion = null;
        String vFoundIn = null;
        final Map<String, Object> moduleMap = ProjectClass.getProjectModuleLibraries();
        if (moduleMap.containsKey(vSnowflakeId)) {
            vJdbcVersion = moduleMap.get(vSnowflakeId).toString();
            vFoundIn = "Modules";
        } else {
            ProjectClass.loadProjectModel();
            ProjectClass.Loaders.loadComponents();
            final Map<String, Object> projDependencies = ProjectClass.Components.getProjectModelComponent("Dependencies");
            if (projDependencies.containsKey(vSnowflakeId)) {
                vJdbcVersion = projDependencies.get(vSnowflakeId).toString();
                vFoundIn = "Dependencies";
            }
        }
        final String strFeedback = String.format("I have found Snowflake JDBC driver v.%s from %s", vJdbcVersion, vFoundIn);
        LogExposureClass.LOGGER.debug(strFeedback);
        return vJdbcVersion;
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
        if (STR_ROLES.equalsIgnoreCase(strWhich)) {
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
            final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strWhichQuery, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
            LogExposureClass.LOGGER.error(strFeedback);
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
        String currentUser = ShellingClass.getCurrentUserAccount();
        if (currentUser.isEmpty()) {
            currentUser = "UNKNOWN_USER";
        }
        properties.put("user", currentUser.toUpperCase(Locale.getDefault()));
        properties.put("db", strDatabase);
        String authValue = propInstance.get("Authenticator").toString().replace("\"", "");
        if (jdbcVersion.startsWith("4.0") 
                && "externalbrowser".equalsIgnoreCase(authValue)) {
            authValue = "EXTERNAL_BROWSER";
        }
        properties.put("authenticator", authValue);
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
        jdbcVersion = getSnowflakeJdbcDriverVersion();
        final String strDriverName = "net.snowflake.client.jdbc.SnowflakeDriver"; // for 3.x
        /*String strDriverName = null;
        if (jdbcVersion.startsWith("4.0")) {
            strDriverName = "net.snowflake.client.api.driver.SnowflakeDriver"; // for 4.x
        } else {
            strDriverName = "net.snowflake.client.jdbc.SnowflakeDriver"; // for 3.x
        }*/
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLdriverLoadingAttempt"), STR_DB_SNOWFLAKE, strDriverName);
        LogExposureClass.LOGGER.debug(strFeedback);
        try {
            Class.forName(strDriverName);
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLdriverLoadingSuccess"), STR_DB_SNOWFLAKE, strDriverName + " v. " + jdbcVersion);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (ClassNotFoundException ex) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLdriverLoadingNotFound"), STR_DB_SNOWFLAKE, strDriverName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * Execute Snowflake pre-defined actions
     * @param strWhich which action to perform
     * @param objProps object properties
     */
    public static void performSnowflakePreDefinedAction(final String strWhich, final Properties objProps) {
        try (Connection objConnection = getSnowflakeConnection(objProps, objProps.get("databaseName").toString());
            Statement objStatement = DatabaseConnectivityClass.createSqlStatement(STR_DB_SNOWFLAKE, objConnection)) {
            executeSnowflakeBootstrapQuery(objStatement);
            getSnowflakePreDefinedInformation(objStatement, strWhich, "Values");
        } catch(SQLException e) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nError"), Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Constructor
     */
    private DatabaseSpecificSnowflakeClass() {
        // intentionally blank
    }
}

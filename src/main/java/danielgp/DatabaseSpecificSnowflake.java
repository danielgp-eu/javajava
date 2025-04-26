package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/* Utility classes */
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Snowflake methods
 */
public class DatabaseSpecificSnowflake extends DatabaseResultSettingClass {
    /**
     * standard String
     */
    private final static String strRoles = "Roles";

    /**
     * Snowflake Bootstrap
     * 
     * @param objStatement
     */
    public static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
        final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
        executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
    }

    /**
     * Initiate a Snowflake connection with Instance properties and DB specified
     * 
     * @param propInstance
     * @param strDatabase
     * @param strNamedInstance
     * @return Connection
     */
    protected static Connection getSnowflakeConnection(final Properties propInstance, final String strDatabase, final String strNamedInstance) {
        loadSnowflakeDriver();
        Connection connection = null;
        final String strConnection = String.format("jdbc:snowflake://%s.snowflakecomputing.com/", propInstance.get("AccountName").toString().replace("\"", ""));
        final Properties propConnection = getSnowflakeProperties(strDatabase, propInstance);
        String strFeedback = String.format("Will attempt to create a Snowflake connection to database %s using %s as connection string and %s properties", strDatabase, strConnection, propConnection);
        LOGGER.debug(strFeedback);
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            strFeedback = String.format("Snowflake connection to database %s was successfully established!", strDatabase);
            LOGGER.debug(strFeedback);
        } catch(SQLException e) {
            strFeedback = String.format("Connection failed: %s", e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return connection;
    }

    /**
     * get standardized Information from Snowflake
     * 
     * @param objStatement
     * @param strWhich
     * @param strKind
     */
    protected static List<Properties> getSnowflakePreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
        final Properties queryProperties = new Properties();
        if (strRoles.equalsIgnoreCase(strWhich)) {
            queryProperties.put("expectedExactNumberOfColumns", "1");
        }
        final String strQueryToUse = getSnowflakePreDefinedMetadataQuery(strWhich);
        return getResultSetStandardized(objStatement, strWhich, strQueryToUse, queryProperties, strKind);
    }

    /**
     * returns standard Metadata query specific to Snowflake
     * 
     * @param strWhich
     * @return
     */
    public static String getSnowflakePreDefinedMetadataQuery(final String strWhich) {
        String strQueryToUse = "";
        switch(strWhich) {
            case "Columns":
                strQueryToUse = """
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
    "INFORMATION_SCHEMA"."COLUMNS";
                """;
                break;
            case "Databases":
                strQueryToUse = """
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
    "INFORMATION_SCHEMA"."DATABASES";
                """;
                break;
            case "Roles":
                strQueryToUse = """
SELECT
    TRIM(VALUE) AS "AssignedRoleName"
FROM
    TABLE(FLATTEN(input => PARSE_JSON(CURRENT_AVAILABLE_ROLES())));
                """;
                break;
            case "Schemas":
                strQueryToUse = """
SELECT
      "CATALOG_NAME"
    , "SCHEMA_NAME"
    , "SCHEMA_OWNER"
    , "IS_TRANSIENT"
    , "IS_MANAGED_ACCESS"
    , "RETENTION_TIME"
    , "DEFAULT_CHARACTER_SET_CATALOG"
    , "DEFAULT_CHARACTER_SET_SCHEMA"
    , "DEFAULT_CHARACTER_SET_NAME"
    , "SQL_PATH"
    , "CREATED"
    , "LAST_ALTERED"
    , "COMMENT"
    , CURRENT_ACCOUNT()     AS "SNOWFLAKE_INSTANCE"
    , SYSDATE()             AS "EXTRACTION_TIMESTAMP_UTC"
FROM
    "INFORMATION_SCHEMA"."SCHEMATA";
                """;
                break;
            case "TablesAndViews":
                strQueryToUse = """
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
    "INFORMATION_SCHEMA"."TABLES";
                """;
                break;
            case "Views":
                strQueryToUse = """
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
    "INFORMATION_SCHEMA"."VIEWS"
                """;
                break;
            case "Views_Light":
                strQueryToUse = """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "VIEW_DEFINITION"
FROM
    "INFORMATION_SCHEMA"."VIEWS"
                """;
                break;
            case "Warehouses":
                strQueryToUse = "SHOW WAREHOUSES;";
                break;
            default:
                final String strFeedback = String.format(Common.strOtherSwitch, strWhich, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst()
                        .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                        .orElse(Common.strUnknown)));
                LOGGER.error(strFeedback);
                break;
        }
        return strQueryToUse;
    }

    /**
     * build Snowflake Properties
     * 
     * @param propInstance
     * @return Properties
     */
    protected static Properties getSnowflakeProperties(final Properties propInstance) {
        final String strDatabase = propInstance.get("Default Database").toString().replace("\"", "");
        return getSnowflakeProperties(strDatabase, propInstance);
    }


    /**
     * build Snowflake Properties
     * 
     * @param strDatabase
     * @param propInstance
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
        String strFeedback = String.format("Will attempt to load Snowflake driver %s", strDriverName);
        LOGGER.debug(strFeedback);
        try {
            Class.forName(strDriverName);
            strFeedback = String.format("Snowflake driver %s has been successfully loaded", strDriverName);
            LOGGER.debug(strFeedback);
        } catch (ClassNotFoundException ex) {
            strFeedback = String.format("Snowflake driver %s not found... :-(", strDriverName);
            LOGGER.error(strFeedback);
        }
    }

    /**
     * constructor
     */
    protected DatabaseSpecificSnowflake() {
        super();
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

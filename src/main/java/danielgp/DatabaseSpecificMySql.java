package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/* Utility classes */
import java.util.List;
import java.util.Properties;

/**
 * MySQL methods
 */
public class DatabaseSpecificMySql extends DatabaseResultSetingClass {

    /**
     * Initiate a MySQL connection with Instance properties and DB specified
     * 
     * @param propInstance
     * @return Connection
     */
    public static Connection getMySqlConnection(final Properties propInstance, final String strDatabase) {
        Connection connection = null;
        final String strServer = propInstance.get("ServerName").toString();
        final String strPort = propInstance.get("Port").toString();
        final String strConnection = String.format("jdbc:mysql://%s:%s/%s", strServer, strPort, strDatabase);
        final Properties propConnection = getMySqlProperties(propInstance);
        String strFeedback = String.format("Will attempt to create a MySQL connection to database %s using %s as connection string and %s properties", strDatabase, strConnection, propConnection.toString());
        LogHandlingClass.LOGGER.debug(strFeedback);
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            strFeedback = String.format("MySQL connection to database %s was successfully established!", strDatabase);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch(SQLException e) {
            strFeedback = String.format("Connection failed: ", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return connection;
    }

    /**
     * get standardized Information from MySQL
     * 
     * @param objStatement
     * @param strWhich
     */
    public static  List<Properties> getMySqlPreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
        final String strQueryToUse = getMySqlPreDefinedMetadataQuery(strWhich);
        final Properties queryProperties = new Properties();
        return getResultSetStandardized(objStatement, strWhich, strQueryToUse, queryProperties, strKind);
    }

    /**
     * returns standard Metadata query specific to Snowflake
     * 
     * @param strWhich
     * @return
     */
    protected static String getMySqlPreDefinedMetadataQuery(final String strWhich) {
        String strQueryToUse = "";
        switch(strWhich) {
            case "Columns":
                strQueryToUse = """
SELECT
      `TABLE_CATALOG`
    , `TABLE_SCHEMA`
    , `TABLE_NAME`
    , `COLUMN_NAME`
    , `ORDINAL_POSITION`
    , `COLUMN_DEFAULT`
    , `IS_NULLABLE`
    , `DATA_TYPE`
    , `CHARACTER_SET_NAME`
    , `COLLATION_NAME`
    , `COLUMN_TYPE`
    , `COLUMN_KEY`
    , `EXTRA`
    , `COLUMN_COMMENT`
    , `GENERATION_EXPRESSION`
    , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    `information_schema`.`SCHEMATA`;
                """;
                break;
            case "Databases":
                strQueryToUse = """
SELECT
      `CATALOG_NAME`
    , `SCHEMA_NAME`
    , `DEFAULT_CHARACTER_SET_NAME`
    , `DEFAULT_COLLATION_NAME`
    , `SQL_PATH`
    , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    `information_schema`.`SCHEMATA`;
                """;
                break;
            case "TablesAndViews":
                strQueryToUse = """
SELECT
      `TABLE_CATALOG`
    , `TABLE_SCHEMA`
    , `TABLE_NAME`
    , `TABLE_TYPE`
    , `ENGINE`
    , `VERSION`
    , `ROW_FORMAT`
    , `TABLE_ROWS`
    , `AVG_ROW_LENGTH`
    , `DATA_LENGTH`
    , `MAX_DATA_LENGTH`
    , `INDEX_LENGTH`
    , `DATA_FREE`
    , `AUTO_INCREMENT`
    , `CREATE_TIME`
    , `UPDATE_TIME`
    , `CHECK_TIME`
    , `TABLE_COLLATION`
    , `CHECKSUM`
    , `CREATE_OPTIONS`
    , `TABLE_COMMENT`
    , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    `information_schema`.`TABLES`;
                """;
                break;
            case "Views":
                strQueryToUse = """
SELECT
      `TABLE_CATALOG`
    , `TABLE_SCHEMA`
    , `TABLE_NAME`
    , `VIEW_DEFINITION`
    , `CHECK_OPTION`
    , `DEFINER`
    , `SECURITY_TYPE`
    , `CHARACTER_SET_CLIENT`
    , `COLLATION_CONNECTION`
    , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    `information_schema`.`VIEWS`;
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
            default:
                final String strFeedback = String.format("This %s type of predefined action is unknown...", strWhich);
                throw new UnsupportedOperationException(strFeedback);
        }
        return strQueryToUse;
    }

    /**
     * build MySQL Properties
     * 
     * @param strDatabase
     * @param propInstance
     * @return Properties
     */
    private static Properties getMySqlProperties(final Properties propInstance) {
        final Properties properties = new Properties();
        properties.put("user", propInstance.get("Username").toString());
        properties.put("password", propInstance.get("Password").toString());
        properties.put("serverTimezone", propInstance.get("ServerTimezone").toString());
        properties.put("autoReconnect", true);
        properties.put("allowPublicKeyRetrieval", true);
        properties.put("useSSL", false);
        properties.put("useUnicode", true);
        properties.put("useJDBCCompliantTimezoneShift", true);
        properties.put("useLegacyDatetimeCode", false);
        properties.put("characterEncoding", "UTF-8");
        return properties;
    }

    /**
     * Execute SQLite pre-defined actions
     * 
     * @param String strWhich
     * @param Properties givenProperties
     */
    public static void performMySqlPreDefinedAction(final String strWhich, final Properties givenProperties) {
        try (Connection objConnection = getMySqlConnection(givenProperties, "mysql");
            Statement objStatement = getSqlStatement("MySQL", objConnection);) {
            getMySqlPreDefinedInformation(objStatement, strWhich, "Values");
        } catch(SQLException e){
            final String strFeedback = String.format("Error", e.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * constructor
     */
    protected DatabaseSpecificMySql() {
        super();
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

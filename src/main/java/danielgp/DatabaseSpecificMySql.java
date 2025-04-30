package danielgp;
/* Jackson classes for fast JSON handling */
import com.fasterxml.jackson.databind.JsonNode;
/* I/O classes */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/* Utility classes */
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * MySQL methods
 */
public class DatabaseSpecificMySql extends DatabaseResultSettingClass {

    /**
     * Getting Connection Properties For MySQL from Environment variable
     * @return
     */
    protected static Properties getConnectionPropertiesForMySQL() {
        final Properties properties = new Properties();
        final String strEnv = "MYSQL";
        final String strEnvMySql = System.getenv(strEnv);
        if (strEnvMySql == null) {
            final String strFeedback = String.format("Environment variable %s not found!", strEnv);
            LOGGER.error(strFeedback);
        } else {
            final InputStream inputStream = new ByteArrayInputStream(strEnvMySql.getBytes());
            final JsonNode ndMySQL = JsoningClass.getJsonFileNodes(inputStream);
            properties.put("ServerName", JsoningClass.getJsonValue(ndMySQL, "/ServerName"));
            properties.put("Port", JsoningClass.getJsonValue(ndMySQL, "/Port"));
            properties.put("Username", JsoningClass.getJsonValue(ndMySQL, "/Username"));
            properties.put("Password", JsoningClass.getJsonValue(ndMySQL, "/Password"));
            properties.put("ServerTimezone", JsoningClass.getJsonValue(ndMySQL, "/ServerTimezone"));
        }
        return properties;
    }

    /**
     * Initiate a MySQL connection with Instance properties and DB specified
     * 
     * @param propInstance
     * @return Connection
     */
    public static Connection getMySqlConnection(final Properties propInstance, final String strDatabase) {
        Connection connection = null;
        String strFeedback;
        if (propInstance.isEmpty()) {
            strFeedback = "MySQL connection properties seems to be empty, hence connection cannot be initiated";
            LOGGER.error(strFeedback);
        } else {
            final String strServer = propInstance.get("ServerName").toString();
            final String strPort = propInstance.get("Port").toString();
            try {
                final String strConnection = String.format("jdbc:mysql://%s:%s/%s", strServer, strPort, strDatabase);
                final Properties propConnection = getMySqlProperties(propInstance);
                strFeedback = String.format("Will attempt to create a MySQL connection to database %s using %s as connection string and %s properties", strDatabase, strConnection, propConnection);
                LOGGER.debug(strFeedback);
                connection = DriverManager.getConnection(strConnection, propConnection);
                strFeedback = String.format("MySQL connection to server %s, port %s and database %s was successfully established!", strServer, strPort, strDatabase);
                LOGGER.debug(strFeedback);
            } catch(SQLException e) {
                strFeedback = String.format("MySQL connection to server %s, port %s and database %s failed: %s", strServer, strPort, strDatabase, e.getLocalizedMessage());
                LOGGER.error(strFeedback);
            }
        }
        return connection;
    }

    /**
     * get standardized Information from MySQL
     * 
     * @param objStatement
     * @param strWhich
     * @param strKind
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
        return switch (strWhich) {
            case "Columns" -> """
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
                        `information_schema`.`SCHEMATA`;""";
            case "Databases" -> """
                    SELECT
                          `CATALOG_NAME`
                        , `SCHEMA_NAME`
                        , `DEFAULT_CHARACTER_SET_NAME`
                        , `DEFAULT_COLLATION_NAME`
                        , `SQL_PATH`
                        , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
                    FROM
                        `information_schema`.`SCHEMATA`;""";
            case "TablesAndViews" -> """
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
                        `information_schema`.`TABLES`;""";
            case "Views" -> """
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
                        `information_schema`.`VIEWS`;""";
            case "Views_Light" -> """
                    SELECT
                          "TABLE_CATALOG"
                        , "TABLE_SCHEMA"
                        , "TABLE_NAME"
                        , "VIEW_DEFINITION"
                    FROM
                        "INFORMATION_SCHEMA"."VIEWS";""";
            default -> {
                final String strFeedback = String.format(Common.strUnknFtrs, strWhich, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                LOGGER.error(strFeedback);
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * get MySQL Properties
     * 
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
     * @param strWhich
     * @param givenProperties
     */
    public static void performMySqlPreDefinedAction(final String strWhich, final Properties givenProperties) {
        try (Connection objConnection = getMySqlConnection(givenProperties, "mysql");
            Statement objStatement = createSqlStatement("MySQL", objConnection)) {
            getMySqlPreDefinedInformation(objStatement, strWhich, "Values");
        } catch(SQLException e) {
            final String strFeedback = String.format("Error %s", Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
        }
    }

    /**
     * constructor
     */
    protected DatabaseSpecificMySql() {
        super();
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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
     * Log to Info details from Snowflake
     * 
     * @param objStatement
     * @param strWhich
     */
    public static void exposeMySqlPreDefinedInformation(final Statement objStatement, final String strWhich) {
        final String strQueryToUse;
        String strFeedback;
        final List<Properties> listStructure;
        final Properties queryProperties = new Properties();
        switch(strWhich) {
            case "Databases":
                strQueryToUse = """
SELECT
      `CATALOG_NAME`
    , `SCHEMA_NAME`
    , `DEFAULT_CHARACTER_SET_NAME`
    , `DEFAULT_COLLATION_NAME`
    , `SQL_PATH`
    , UTC_TIMESTAMP()               AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    `information_schema`.`SCHEMATA`;
                """;
                final ResultSet rsDb = executeCustomQuery(objStatement, "Databases", strQueryToUse, queryProperties);
                listStructure = getResultSetColumnStructure(rsDb);
                strFeedback = String.format("Structure list for Databases is %s", listStructure.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                final List<Properties> listDbs = getResultSetColumnValues(rsDb);
                strFeedback = String.format("Final list of Databases is %s", listDbs.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
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
                final ResultSet rsTables = executeCustomQuery(objStatement, "Databases", strQueryToUse, queryProperties);
                listStructure = getResultSetColumnStructure(rsTables);
                strFeedback = String.format("Structure list for Databases is %s", listStructure.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                final List<Properties> listTables = getResultSetColumnValues(rsTables);
                strFeedback = String.format("Final list of Databases is %s", listTables.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                break;
            default:
                strFeedback = String.format("This %s type of predefined action is unknown...", strWhich);
                throw new UnsupportedOperationException(strFeedback);
        }
    }

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
     * constructor
     */
    protected DatabaseSpecificMySql() {
        super();
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

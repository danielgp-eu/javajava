package danielgp;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
/* Utility classes */
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Snowflake methods
 */
public class DatabaseSpecificSnowflake extends DatabaseResultSetingClass {

    /**
     * Snowflake Bootstrap
     * 
     * @param objStatement
     */
    protected static void executeSnowflakeBootstrapQuery(final Statement objStatement) {
        final String strQueryToUse = "ALTER SESSION SET JDBC_QUERY_RESULT_FORMAT='JSON';";
        executeQueryWithoutResultSet(objStatement, "Bootstrap", strQueryToUse);
    }

    /**
     * Log to Info details from Snowflake
     * 
     * @param objStatement
     * @param strWhich
     */
    protected static void exposeSnowflakePreDefinedInformation(final Statement objStatement, final String strWhich) {
        final String strQueryToUse;
        String strFeedback;
        final List<Properties> listStructure;
        final Properties queryProperties = new Properties();
        switch(strWhich) {
            case "AvailableRoles":
                strQueryToUse = "SELECT TRIM(VALUE) AS \"AssignedRoleName\" FROM TABLE(FLATTEN(input => PARSE_JSON(CURRENT_AVAILABLE_ROLES())));";
                queryProperties.put("expectedExactNumberOfColumns", "1");
                try (ResultSet resultSetRole = executeCustomQuery(objStatement, "Available Roles", strQueryToUse, queryProperties)) {
                    final List<String> listRoles = getResultSetListOfStrings(resultSetRole);
                    strFeedback = String.format("Current roles were found: %s", listRoles.toString()); 
                    LogHandlingClass.LOGGER.info(strFeedback);
                } catch (SQLException e) {
                    strFeedback = String.format("Statement execution for %s has failed with following error: %s", strWhich, e.getLocalizedMessage());
                    LogHandlingClass.LOGGER.error(strFeedback);
                }
                break;
            case "AvailableWarehouses":
                strQueryToUse = "SHOW WAREHOUSES;";
                final ResultSet rsWarehouse = executeCustomQuery(objStatement, "Available Warehouses", strQueryToUse, queryProperties);
                listStructure = getResultSetColumnStructure(rsWarehouse);
                strFeedback = String.format("Structure list for Warehouses is %s", listStructure.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                final List<Properties> listWarehouses = getResultSetColumnValues(rsWarehouse);
                strFeedback = String.format("Final list of Warehouses is %s", listWarehouses.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                break;
            case "Databases":
                strQueryToUse = "SELECT \"DATABASE_NAME\", \"DATABASE_OWNER\", \"COMMENT\", \"CREATED\", \"LAST_ALTERED\", \"RETENTION_TIME\", \"TYPE\", CURRENT_ACCOUNT() AS \"SNOWFLAKE_INSTANCE\", SYSDATE() AS \"EXTRACTION_TIMESTAMP_UTC\" FROM \"INFORMATION_SCHEMA\".\"DATABASES\";";
                final ResultSet rsDb = executeCustomQuery(objStatement, "Databases", strQueryToUse, queryProperties);
                listStructure = getResultSetColumnStructure(rsDb);
                strFeedback = String.format("Structure list for Databases is %s", listStructure.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                final List<Properties> listDbs = getResultSetColumnValues(rsDb);
                strFeedback = String.format("Final list of Databases is %s", listDbs.toString());
                LogHandlingClass.LOGGER.info(strFeedback);
                break;
            default:
                strFeedback = String.format("Provided %s is not defined, hence nothing will be actually executed...", strWhich);
                LogHandlingClass.LOGGER.error(strFeedback);
                break;
        }
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
        String strFeedback = String.format("Will attempt to create a Snowflake connection to database %s using %s as connection string and %s properties", strDatabase, strConnection, propConnection.toString());
        LogHandlingClass.LOGGER.debug(strFeedback);
        try {
            connection = DriverManager.getConnection(strConnection, propConnection);
            strFeedback = String.format("Snowflake connection to database %s was successfully established!", strDatabase);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch(SQLException e) {
            strFeedback = String.format("Connection failed: ", e.getLocalizedMessage());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return connection;
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
        LogHandlingClass.LOGGER.debug(strFeedback);
        try {
            Class.forName(strDriverName);
            strFeedback = String.format("Snowflake driver %s has been successfully loaded", strDriverName);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (ClassNotFoundException ex) {
            strFeedback = String.format("Snowflake driver %s not found... :-(", strDriverName);
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * constructor
     */
    protected DatabaseSpecificSnowflake() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

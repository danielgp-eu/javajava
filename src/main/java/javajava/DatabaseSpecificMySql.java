package javajava;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import tools.jackson.databind.JsonNode;

/**
 * MySQL methods
 */
public final class DatabaseSpecificMySql {

    /**
     * Getting Connection Properties For MySQL from Environment variable
     * @return Properties
     */
    public static Properties getConnectionPropertiesForMySQL() {
        final Properties properties = new Properties();
        final String strEnv = "MYSQL";
        final String strEnvMySql = System.getenv(strEnv);
        if (strEnvMySql == null) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nEnvironmentVariableNotFound"), strEnv);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nEnvironmentVariableFound"), strEnv);
            LogExposureClass.LOGGER.debug(strFeedback);
            final InputStream inputStream = new ByteArrayInputStream(strEnvMySql.getBytes());
            final JsonNode ndMySQL = JsonOperationsClass.getJsonFileNodes(inputStream);
            properties.put("ServerName", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerName"));
            properties.put("Port", JsonOperationsClass.getJsonValue(ndMySQL, "/Port"));
            properties.put("Username", JsonOperationsClass.getJsonValue(ndMySQL, "/Username"));
            properties.put("Password", JsonOperationsClass.getJsonValue(ndMySQL, "/Password"));
            properties.put("ServerTimezone", JsonOperationsClass.getJsonValue(ndMySQL, "/ServerTimezone"));
        }
        return properties;
    }

    /**
     * Initiate a MySQL connection with Instance properties and DB specified
     * 
     * @param propInstance Properties for Instance
     * @param strDatabase Database to connect to
     * @return Connection
     */
    public static Connection getMySqlConnection(final Properties propInstance, final String strDatabase) {
        Connection connection = null;
        if (propInstance.isEmpty()) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionPropertiesEmpty"), DatabaseOperationsClass.STR_DB_MYSQL);
            LogExposureClass.LOGGER.error(strFeedbackErr);
        } else {
            final String strServer = propInstance.get("ServerName").toString();
            final int strPort = BasicStructuresClass.convertStringIntoInteger(propInstance.get("Port").toString());
            try {
                final String strConnection = String.format("jdbc:mysql://%s:%s/%s", strServer, strPort, strDatabase);
                final Properties propConnection = getMySqlProperties(propInstance);
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationAttempt"), DatabaseOperationsClass.STR_DB_MYSQL, strDatabase, strConnection, propConnection);
                LogExposureClass.LOGGER.debug(strFeedback);
                connection = DriverManager.getConnection(strConnection, propConnection);
                final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationSuccess"), DatabaseOperationsClass.STR_DB_MYSQL, strServer, strPort, strDatabase);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch(SQLException e) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCreationFailed"), DatabaseOperationsClass.STR_DB_MYSQL, strServer, strPort, strDatabase, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
        }
        return connection;
    }

    /**
     * get standardized Information from MySQL
     * 
     * @param objStatement Statement
     * @param strWhich Which query is needed
     * @param strKind which type of output would be needed
     * @return List with Properties
     */
    public static List<Properties> getMySqlPreDefinedInformation(final Statement objStatement, final String strWhich, final String strKind) {
        final String strQueryToUse = getMySqlPreDefinedMetadataQuery(strWhich);
        final Properties rsProperties = new Properties();
        rsProperties.put("strWhich", strWhich);
        rsProperties.put("strQueryToUse", strQueryToUse);
        rsProperties.put("strKind", strKind);
        final Properties queryProperties = new Properties();
        return DatabaseOperationsClass.ResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
    }

    /**
     * returns standard Metadata query specific to Snowflake
     * 
     * @param strWhich Which kind of query is needed
     * @return Query as String
     */
    public static String getMySqlPreDefinedMetadataQuery(final String strWhich) {
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
            case "Schemas" -> """
SELECT
      "CATALOG_NAME"
    , "SCHEMA_NAME"
    , "DEFAULT_CHARACTER_SET_NAME"
    , "DEFAULT_COLLATION_NAME"
    , "SQL_PATH"
    , "DEFAULT_ENCRYPTION"
    , UTC_TIMESTAMP()           AS `EXTRACTION_TIMESTAMP_UTC`
FROM
    "INFORMATION_SCHEMA"."SCHEMATA";""";
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
            case "ViewsLight" -> """
SELECT
      "TABLE_CATALOG"
    , "TABLE_SCHEMA"
    , "TABLE_NAME"
    , "VIEW_DEFINITION"
FROM
    "INFORMATION_SCHEMA"."VIEWS";""";
            default -> {
                final String strFeedback = String.format(LogExposureClass.STR_I18N_UNKN_FTS, strWhich, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(LogExposureClass.STR_I18N_UNKN)));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    /**
     * get MySQL Properties
     * 
     * @param propInstance Instance Properties
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
     * Execute MySQL pre-defined actions
     * 
     * @param strWhich Which kind of query is needed
     * @param givenProperties Connection Properties
     */
    public static void performMySqlPreDefinedAction(final String strWhich, final Properties givenProperties) {
        try (Connection objConnection = getMySqlConnection(givenProperties, "mysql");
            Statement objStatement = DatabaseOperationsClass.ConnectivityClass.createSqlStatement(DatabaseOperationsClass.STR_DB_MYSQL, objConnection)) {
            final List<Properties> listProps = getMySqlPreDefinedInformation(objStatement, strWhich, "Values");
            final String strFeedback = listProps.toString();
            LogExposureClass.LOGGER.info(strFeedback);
        } catch(SQLException e) {
            final String strFeedbackErr = String.format("Error %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * constructor
     */
    private DatabaseSpecificMySql() {
        // intentionally blank
    }
}

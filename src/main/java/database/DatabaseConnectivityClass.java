package database;

import localization.JavaJavaLocalizationClass;
import log.LogExposure;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connectivity
 */
public final class DatabaseConnectivityClass {

    /**
     * Connection closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenConnection connection object
     */
    public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType));
        try {
            givenConnection.close();
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType));
        } catch (SQLException e) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage()));
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenStatement statement
     */
    public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType));
        try {
            givenStatement.close();
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType));
        } catch (SQLException e) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage()));
        }
    }

    /**
     * Instantiating a statement
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param connection connection to use
     * @return Statement
     */
    public static Statement createSqlStatement(final String strDatabaseType, final Connection connection) {
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType));
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType));
        } catch (SQLException e) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage()));
        }
        return objStatement;
    }

    /**
     * Constructor
     */
    private DatabaseConnectivityClass() {
        // intentionally blank
    }
}

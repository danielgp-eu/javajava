package database;

import javajava.CommonClass;
import javajava.LoggerLevelProviderClass;
import localization.JavaJavaLocalizationClass;
import org.apache.logging.log4j.Level;

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
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        try {
            givenConnection.close();
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
                LoggerLevelProviderClass.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenStatement statement
     */
    public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        try {
            givenStatement.close();
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
                LoggerLevelProviderClass.LOGGER.error(strFeedback);
            }
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
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
                LoggerLevelProviderClass.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
                LoggerLevelProviderClass.LOGGER.error(strFeedback);
            }
        }
        return objStatement;
    }

    /**
     * Constructor
     */
    private DatabaseConnectivityClass() {
        throw new UnsupportedOperationException(CommonClass.STR_I18N_AP_CL_WN);
    }
}

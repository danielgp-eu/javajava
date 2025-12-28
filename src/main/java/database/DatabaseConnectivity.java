package database;

import org.apache.logging.log4j.Level;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database connectivity
 */
public final class DatabaseConnectivity {

    /**
     * Connection closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenConnection connection object
     */
    public static void closeConnection(final String strDatabaseType, final Connection givenConnection) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        try {
            givenConnection.close();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        try {
            givenStatement.close();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return objStatement;
    }

    /**
     * Constructor
     */
    private DatabaseConnectivity() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

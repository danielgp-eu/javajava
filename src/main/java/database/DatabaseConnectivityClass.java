package database;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

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
        final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseAttempt"), strDatabaseType);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        try {
            givenConnection.close();
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseSuccess"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLconnectionCloseError"), strDatabaseType, e.getLocalizedMessage());
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
    }

    /**
     * Statement closing
     * 
     * @param strDatabaseType type of database (mainly for meaningful feedback)
     * @param givenStatement statement
     */
    public static void closeStatement(final String strDatabaseType, final Statement givenStatement) {
        final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseAttempt"), strDatabaseType);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        try {
            givenStatement.close();
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseSuccess"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCloseError"), strDatabaseType, e.getLocalizedMessage());
            LogExposureClass.LOGGER.debug(strFeedbackErr);
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
        final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationAttempt"), strDatabaseType);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        Statement objStatement = null;
        try {
            objStatement = connection.createStatement();
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationSuccess"), strDatabaseType);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nSQLstatementCreationError"), e.getLocalizedMessage());
            LogExposureClass.LOGGER.debug(strFeedbackErr);
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

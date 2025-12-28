package database;

import org.apache.logging.log4j.Level;
import org.sqlite.Function;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLite methods
 */
public final class DatabaseSpecificSqLite {

    /**
     * Initiates a SQLite connection
     * 
     * @param strSqLiteFile file with SQLite database
     * @return Connection
     */
    public static Connection getSqLiteConnection(final String strSqLiteFile) {
        final String strConnection = "jdbc:sqlite:" + strSqLiteFile.replace("\\", "/");
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationAttemptLight"), Common.STR_DB_SQLITE, strSqLiteFile, strConnection);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(strConnection);
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationSuccessLight"), Common.STR_DB_SQLITE, strSqLiteFile);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
            Function.create(connection, "REGEXP_LIKE", new Function() {
                @Override
                protected void xFunc() throws SQLException {
                    final String text = value_text(0);
                    final String pattern = value_text(1);
                    final Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                    final Matcher matcher = regex.matcher(text);
                    result(matcher.find() ? 1 : 0);
                }
            });
        } catch(SQLException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationFailedLight"), Common.STR_DB_SQLITE, e.getLocalizedMessage());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return connection;
    }

    /**
     * constructor
     */
    private DatabaseSpecificSqLite() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }

}

package javajava;
/* SQL classes */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/* SQLite function class */
import org.sqlite.Function;
/* Util classes */
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * SQLite methods
 */
public class DatabaseSpecificSqLite extends DatabaseResultSettingClass {

    /**
     * Initiates a SQLite connection
     * 
     * @param strSqLiteFile
     * @return Connection
     */
    public static Connection getSqLiteConnection(final String strSqLiteFile) {
        final String strConnection = "jdbc:sqlite:" + strSqLiteFile.replace("\\", "/");
        String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationAttemptLight"), Common.strDbSqLite, strSqLiteFile, strConnection);
        LOGGER.debug(strFeedback);
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(strConnection);
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationSuccessLight"), Common.strDbSqLite, strSqLiteFile);
            LOGGER.debug(strFeedback);
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
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nSQLconnectionCreationFailedLight"), Common.strDbSqLite, e.getLocalizedMessage());
            LOGGER.error(strFeedback);
        }
        return connection;
    }

    /**
     * constructor
     */
    public DatabaseSpecificSqLite() {
        super();
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }

}

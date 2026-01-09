package javajava;

import localization.JavaJavaLocalizationClass;
import org.apache.logging.log4j.Level;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Class with common features
 */
public final class CommonClass {
    /**
     * Database MySQL
     */
    public static final String STR_DB_MYSQL = "MySQL";
    /**
     * Database Snowflake
     */
    public static final String STR_DB_SNOWFLAKE = "Snowflake";
    /**
     * Database SQLite
     */
    public static final String STR_DB_SQLITE = "SQLite";
    /**
     * standard String
     */
    public static final String STR_NAME = "Name";
    /**
     * NULL string
     */
    public static final String STR_NULL = "NULL";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_PRMTR_RGX = "\\{[0-9A-Za-z\\s_\\-]{2,50}\\}";
    /**
     * standard String
     */
    public static final String STR_ROLES = "Roles";
    /**
     * Regular Expression for Prompt Parameters within SQL Query
     */
    public static final String STR_QTD_STR_VL = "\"%s\"";
    /**
     * standard Application class feedback
     */
    public static final String STR_I18N_AP_CL_WN = JavaJavaLocalizationClass.getMessage("i18nAppClassWarning");
    /**
     * standard SQL statement unable
     */
    public static final String STR_I18N_STM_UNB = JavaJavaLocalizationClass.getMessage("i18nSQLstatementUnableToGetX");
    /**
     * standard Unknown feature
     */
    public static final String STR_I18N_UNKN_FTS = JavaJavaLocalizationClass.getMessage("i18nUnknFtrs");
    /**
     * standard Unknown
     */
    public static final String STR_I18N_UNKN = JavaJavaLocalizationClass.getMessage("i18nUnknown");

    /**
     * Execution Interrupted details captured to Error log
     * @param strError details
     */
    public static void setInputOutputExecutionLoggedToError(final String strError) {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.FATAL)) {
            LoggerLevelProviderClass.LOGGER.error(strError);
        }
    }

    /**
     * safely compute percentage
     * @param numerator top number
     * @param denominator dividing number
     * @return float value
     */
    public static float getPercentageSafely(final long numerator, final long denominator) {
        float percentage = 0;
        if (denominator != 0) {
            final double percentageExact = (float) numerator / denominator * 100;
            percentage = (float) new BigDecimal(Double.toString(percentageExact))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        }
        return percentage;
    }

    // Private constructor to prevent instantiation
    private CommonClass() {
        // intentionally left blank
    }
}

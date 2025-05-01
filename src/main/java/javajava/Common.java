package javajava;
/* Utility classes */
import java.util.Map;
/* Regular Expressions classes */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class with common features
 */
public final class Common {
    /**
     * standard Application class feedback
     */
    public static final String strAppClsWrng = JavaJavaLocalization.getMessage("i18nAppClassWarning");
    /**
     * Database MySQL
     */
    public static final String strDbMySQL = "MySQL";
    /**
     * Database Snowflake
     */
    public static final String strDbSnowflake = "Snowflake";
    /**
     * standard SQL statement unable
     */
    public static final String strStmntUnableX = JavaJavaLocalization.getMessage("i18nSQLstatementUnableToGetX");
    /**
     * standard Unknown feature
     */
    public static final String strUnknFtrs = JavaJavaLocalization.getMessage("i18nUnknFtrs");
    /**
     * standard Unknown
     */
    public static final String strUnknown = JavaJavaLocalization.getMessage("i18nUnknown");
    /**
     * Counts number of parameters with in a string
     *
     * @param inputString
     * @return int
     */
    public static int countParametersWithinString(final String inputString) {
        final Pattern pattern = Pattern.compile("%[a-zA-Z]");
        final Matcher matcher = pattern.matcher(inputString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Cycle inside Map and build a JSON string out of it
     *
     * @param arrayAttrib
     * @return String
     */
    public static String getMapIntoJsonString(final Map<String, Object> arrayAttrib) {
        final StringBuffer strJsonSubString = new StringBuffer();
        arrayAttrib.forEach((strKey, objValue) -> {
            if (!strJsonSubString.isEmpty()) {
                strJsonSubString.append(',');
            }
            String strRaw = "\"%s\":\"%s\"";
            if (objValue.toString().startsWith("[") && objValue.toString().endsWith("]")) {
                strRaw = "\"%s\":%s";
            }
            strJsonSubString.append(String.format(strRaw, strKey, objValue));
        });
        return String.format("{%s}", strJsonSubString);
    }

    /**
     * handle NameUnformatted
     * @param intRsParams
     * @param strUnformatted
     * @param strReplacement1
     * @param strReplacement2
     * @return
     */
    public static String handleNameUnformattedMessage(final int intRsParams, final String strUnformatted, final String strReplacement1, final String strReplacement2) {
        return switch (intRsParams) {
            case 1 -> String.format(strUnformatted, strReplacement1);
            case 2 -> String.format(strUnformatted, strReplacement1, strReplacement2);
            default -> {
                final String strFeedback = String.format(strUnknFtrs, intRsParams, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(strUnknown)));
                throw new UnsupportedOperationException(strFeedback);
            }
        };
    }

    // Private constructor to prevent instantiation
    private Common() {
        throw new UnsupportedOperationException(strAppClsWrng);
    }
}

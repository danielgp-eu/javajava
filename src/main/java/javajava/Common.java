package javajava;
/* Utility classes */
import java.util.Arrays;
import java.util.List;
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
     * Database SQLite
     */
    public static final String strDbSqLite = "SQLite";
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
     * @param inputString string to evaluate
     * @return number of parameters within given string
     */
    public static int countParametersWithinString(final String inputString) {
        final Pattern pattern = Pattern.compile("%(|[1-9]{1}\\$)(|,[0-9]{1,3}|\\+|\\(|\\,)(|\\.[1-9]{1}|[0-9]{1,2})(a|b|c|d|e|f|g|h|n|o|s|t|x)");
        final Matcher matcher = pattern.matcher(inputString);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Build a pair of Key and Value for JSON
     * @param strKey
     * @param objValue
     * @return
     */
    private static String getJsonKeyAndValue(final String strKey, final Object objValue) {
        Boolean needsQuotesAround = false;
        final List<String> unquotedValues = Arrays.asList("null", "true", "false");
        if (objValue instanceof Integer) {
            needsQuotesAround = true;
        } else if (objValue instanceof Double) {
            needsQuotesAround = true;
        } else if (hasMatchingSubstring(objValue.toString(), unquotedValues)) {
            needsQuotesAround = true;
        } else if (objValue.toString().startsWith("[") && objValue.toString().endsWith("]")) {
            needsQuotesAround = true;
        } else if (isStringActuallyNumeric(objValue.toString())) {
            needsQuotesAround = true;
        }
        String strRaw = "\"%s\":\"%s\"";
        if (needsQuotesAround) {
            strRaw = "\"%s\":%s";
        }
        return String.format(strRaw, strKey, objValue);
    }

    /**
     * Cycle inside Map and build a JSON string out of it
     *
     * @param arrayAttrib array with attribute values
     * @return String
     */
    public static String getMapIntoJsonString(final Map<String, Object> arrayAttrib) {
        final StringBuffer strJsonSubString = new StringBuffer();
        arrayAttrib.forEach((strKey, objValue) -> {
            if (!strJsonSubString.isEmpty()) {
                strJsonSubString.append(',');
            }
            strJsonSubString.append(getJsonKeyAndValue(strKey, objValue));
        });
        return String.format("{%s}", strJsonSubString);
    }

    /**
     * Checks if given string is included in a given List of Strings
     * @param str
     * @param substrings
     * @return bollean true if found, false otherwise
     */
    private static boolean hasMatchingSubstring(final String str, final List<String> substrings) {
        return substrings.stream().anyMatch(str::contains);
    }

    /**
     * handle NameUnformatted
     * @param intRsParams number for parameters
     * @param strUnformatted original string
     * @param strReplacement1 1st replacement
     * @param strReplacement2 2nd replacement
     * @return string
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

    /**
     * Check if String is actually Numeric
     *
     * @param inputString string to evaluate
     * @return True if given String is actually Numeric
     */
    private static Boolean isStringActuallyNumeric(final String inputString) {
        if (inputString == null) {
            return false; 
        }
        final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        return pattern.matcher(inputString).matches();
    }

    // Private constructor to prevent instantiation
    private Common() {
        throw new UnsupportedOperationException(strAppClsWrng);
    }
}

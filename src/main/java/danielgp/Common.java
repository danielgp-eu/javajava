package danielgp;
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
     * Counts number of parameters with in a string
     *
     * @param String inputString
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
     * Cycle trough Map and build a JSON string out of it
     *
     * @param Map<> arrayAttrib
     * @return String
     */
    public static String getMapIntoJsonString(final Map<String, Object> arrayAttrib) {
        final StringBuffer strJsonSubString = new StringBuffer();
        arrayAttrib.forEach((strKey, objValue)->{
            if (!strJsonSubString.isEmpty()) {
                strJsonSubString.append(',');
            }
            strJsonSubString.append(String.format("\"%s\":\"%s\"", strKey, objValue.toString()));
        });
        return String.format("{%s}", strJsonSubString.toString());
    }

    // Private constructor to prevent instantiation
    private Common() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

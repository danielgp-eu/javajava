package structure;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import log.LogExposureClass;

/**
 * Number handling
 */
public final class NumberClass {

    /**
     * Safely computes percentage
     * @param numerator top number
     * @param denominator dividing number
     * @return float value
     */
    public static float computePercentageSafely(final long numerator, final long denominator) {
        float percentage = 0;
        if (denominator == 0) {
            final String strFeedback = String.format("Denominator is 0 hence Percentage calculation with Numerator %s is not possible and will return 0", numerator);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            final double percentageExact = (float) numerator / denominator * 100;
            percentage = (float) new BigDecimal(Double.toString(percentageExact))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        }
        return percentage;
    }

    /**
     * Convert String to BigDecimal
     * @param strNumber string to evaluate
     * @return BigDecimal
     */
    public static BigDecimal convertStringIntoBigDecimal(final String strNumber) {
        BigDecimal noToReturn = null;
        final boolean isNumeric = StringManipulationClass.EvaluatingClass.isStringActuallyNumeric(strNumber);
        if (isNumeric) {
            noToReturn = new BigDecimal(strNumber).stripTrailingZeros();
        }
        return noToReturn;
    }

    /**
     * Convert String to Integer
     * @param strNumber string to evaluate
     * @return integer
     */
    public static int convertStringIntoInteger(final String strNumber) {
        int noToReturn = 0;
        final boolean isNumeric = StringManipulationClass.EvaluatingClass.isStringActuallyInteger(strNumber);
        if (isNumeric) {
            noToReturn = Integer.parseInt(strNumber);
        }
        return noToReturn;
    }

    /**
     * Extracts all occurrences of a given reg-ex pattern from a text.
     * @param inputString The input string to search within.
     * @return A List of strings, where each string is a full match found.
     */
    public static int countNamedParametersWithinQuery(final String inputString) {
        return countOccurrences(inputString, "NamedParameters");
    }

    /**
     * Count occurrences with String
     * @param hystack string to count in
     * @param needleType type of pattern search
     * @return number of occurrences
     */
    private static int countOccurrences(final String hystack, final String needleType) {
        final Pattern pattern = switch(needleType) {
            case "NamedParameters" -> Pattern.compile(StringManipulationClass.STR_PRMTR_RGX);
            case "ComplexPositionalTypeParameters" -> Pattern.compile("%(|[1-9]\\$)(|,\\d{1,3}|\\+|\\(|,)(|\\.[1-9]|\\d{1,2})[abcdefghnostx]");
            case "PositionalTypeParameters" -> Pattern.compile("%[ACEGHSTXacdefghostx]");
            default -> Pattern.compile(".*");
        };
        final Matcher matcher = pattern.matcher(hystack);
        int count = 0;
        while (matcher.find()) {
            count = count + matcher.groupCount();
        }
        return count;
    }

    /**
     * Counts number of parameters with in a string
     * @param inputString string to evaluate
     * @return number of parameters within given string
     */
    public static int countPositionalTypeParametersWithinQuery(final String inputString) {
        return countOccurrences(inputString, "PositionalTypeParameters");
    }

    /**
     * Constructor
     */
    private NumberClass() {
        // intentionally blank
    }

}

package javajava;

/**
 * String Manipulation
 */
public final class StringManipulationClass {

    /**
     * Clean String From CurlyBraces
     * @param strOriginal
     * @return
     */
    public static String cleanStringFromCurlyBraces(final String strOriginal) {
        final StringBuilder strBuilder = new StringBuilder();
        for (final char c : strOriginal.toCharArray()) {
            if (c != '{' && c != '}') {
                strBuilder.append(c);
            }
        }
        return strBuilder.toString();
    }

    /**
     * get Named Parameter From Prompt One
     * @param strOriginal
     * @return
     */
    public static String getNamedParameterFromPromptOne(final String strOriginal) {
        return ":" + cleanStringFromCurlyBraces(strOriginal).replace(" ", "_");
    }

    // Private constructor to prevent instantiation
    private StringManipulationClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

package javajava;
/* ICU classes */
import com.ibm.icu.text.PluralRules;
/* Text classes */
import java.text.MessageFormat;
/* Utility classes */
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
/* Logging classes */
import org.apache.logging.log4j.Level;

/**
 * Internationalization
 */
public final class JavaJavaLocalization {
    /**
     * localization
     */
    private static ResourceBundle bundle;
    /**
     * locale file prefix
     */
    private final static String DEFAULT_LOCALE = "en-US";
    /**
     * locale folder
     */
    private final static String MESSAGES_FOLDER = "Locale/JavaJavaBundle/";
    /**
     * locale file prefix
     */
    private final static String MESSAGES_KEY = "JavaJavaTranslation";

    /**
     * getting Locale
     * @return Locale
     */
    public static Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * getting Message
     * @param key translation bundle name
     * @return String
     */
    public static String getMessage(final String key) {
        setBundle();
        return bundle.getString(key);
    }

    /**
     * Formatted message
     * @param key translation bundle name
     * @param arguments localization arguments
     * @return String
     */
    public static String getMessage(final String key, final Object ... arguments) {
        return MessageFormat.format(getMessage(key), arguments);
    }

    /**
     * message with Plural
     * @param baseKey translation bundle
     * @param count number to evaluate for plural rules
     * @return String
     */
    public static String getMessageWithPlural(final String baseKey, final long count) {
        final PluralRules pluralRules = PluralRules.forLocale(getLocale());
        final String pluralKey = baseKey + "." + pluralRules.select(count);
        return bundle.getString(pluralKey);
    }

    /**
     * get User locale
     * @return String
     */
    public static String getUserLocale() {
        return System.getProperty("user.language.format")
            + "-" + System.getProperty("user.country.format");
    }

    /**
     * Supported check
     * @param locale localization to use
     * @return boolean
     */
    public static boolean isSupported(final Locale locale) {
        final Locale[] availableLocales = Locale.getAvailableLocales();
        return Arrays.asList(availableLocales).contains(locale);
    }

    /**
     * set Bundle
     */
    private static void setBundle() {
        if (Objects.isNull(bundle)) {
            bundle = ResourceBundle.getBundle(MESSAGES_FOLDER + MESSAGES_KEY);
        }
    }

    /**
     * setting Locale
     * @param locale localization to use
     */
    public static void setLocale(final Locale locale) {
        final String strLocale = locale.toString();
        setLocaleByString(strLocale);
    }

    /**
     * setting Locale
     * @param strLocale localization to use
     */
    public static void setLocaleByString(final String strLocale) {
        String strFeedback = String.format("Request to set localization to %s received", strLocale);
        LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        final Locale lclRequested = Locale.forLanguageTag(strLocale);
        if (isSupported(lclRequested)) {
            Locale.setDefault(lclRequested);
            strFeedback = String.format("Localization %s is supported and has just been set!", strLocale);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        } else {
            Locale.setDefault(Locale.forLanguageTag(DEFAULT_LOCALE));
            strFeedback = String.format("Localization %s is NOT supported and default one (which is %s) has been set!", strLocale, DEFAULT_LOCALE);
            LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        }
    }

    /**
     * constructor
     */
    private JavaJavaLocalization() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

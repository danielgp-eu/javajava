package danielgp;
/* ICU classes */
import com.ibm.icu.text.PluralRules;
/* Text classes */
import java.text.MessageFormat;
/* Utility classes */
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
/**
 * Internationalization
 */
public final class DanielLocalization {
    /**
     * localization
     */
    private static ResourceBundle bundle;
    /**
     * locale file prefix
     */
    private final static String DEFAULT_LOCALE = "en-US";
    /**
     * locale file prefix
     */
    private final static String MESSAGES_KEY = "DanielTranslations";

    /**
     * getting Locale
     * @return
     */
    public static Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * getting Message
     * @param args
     */
    public static String getMessage(final String key) {
        setBundle();
        return bundle.getString(key);
    }

    /**
     * Formatted message
     * @param key
     * @param arguments
     * @return
     */
    public static String getMessage(final String key, final Object ... arguments) {
        return MessageFormat.format(getMessage(key), arguments);
    }

    /**
     * message with Plural
     * @param baseKey
     * @param count
     * @return
     */
    public static String getMessageWithPlural(final String baseKey, final long count) {
        final PluralRules pluralRules = PluralRules.forLocale(getLocale());
        final String pluralKey = baseKey + "." + pluralRules.select(count);
        return bundle.getString(pluralKey);
    }

    /**
     * get User locale
     * @return
     */
    public static String getUserLocale() {
        return System.getProperty("user.language.format") + "-" + System.getProperty("user.country.format");
    }

    /**
     * Supported check
     * @param locale
     * @return
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
            bundle = ResourceBundle.getBundle(MESSAGES_KEY);
        }
    }

    /**
     * setting Locale
     * @param locale
     */
    public static void setLocale(final Locale locale) {
        if (isSupported(locale)) {
            Locale.setDefault(locale);
        } else {
            Locale.setDefault(Locale.forLanguageTag(DEFAULT_LOCALE));
        }
    }

    /**
     * constructor
     */
    private DanielLocalization() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

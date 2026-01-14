package localization;

import com.ibm.icu.text.PluralRules;
import log.LogExposureClass;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Internationalization
 */
public final class JavaJavaLocalizationClass {
    /**
     * localization
     */
    private static ResourceBundle bundle;
    /**
     * locale file prefix
     */
    private static final String DEFAULT_LOCALE = "en-US";
    /**
     * locale folder
     */
    private static final String MESSAGES_FOLDER = "Locale/JavaJavaBundle/";
    /**
     * locale file prefix
     */
    private static final String MESSAGES_KEY = "JavaJavaTranslation";

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
     * get User country
     * @return String
     */
    private static String getUserCountry() {
        String strUserCountry = System.getProperty("user.country.format");
        if (strUserCountry == null) {
            strUserCountry = System.getProperty("user.country");
        }
        return strUserCountry;
    }

    /**
     * get User language
     * @return String
     */
    private static String getUserLanguage() {
        String strUserLanguage = System.getProperty("user.language.format");
        if (strUserLanguage == null) {
            strUserLanguage = System.getProperty("user.language");
        }
        return strUserLanguage;
    }

    /**
     * get User locale
     * @return String
     */
    public static String getUserLocale() {
        return getUserLanguage() + "-" + getUserCountry();
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
        final String strLineSep = "~".repeat(30);
        final StringBuilder strBuilder = new StringBuilder();
        final Locale lclRequested = Locale.forLanguageTag(strLocale);
        if (isSupported(lclRequested)) {
            Locale.setDefault(lclRequested);
            strBuilder.append(String.format("Requested localization to %s is supported and has been successfully set!", strLocale));
        } else {
            Locale.setDefault(Locale.forLanguageTag(DEFAULT_LOCALE));
            strBuilder.append(String.format("Requested localization %s is NOT supported, hence default one (which is %s) has been successfully set!", strLocale, DEFAULT_LOCALE));
        }
        strBuilder.append(strLineSep);
        final String strFeedback = strBuilder.toString();
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * constructor
     */
    private JavaJavaLocalizationClass() {
        // intentionally blank
    }
}

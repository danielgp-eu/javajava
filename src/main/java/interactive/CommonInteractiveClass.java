package interactive;

import java.time.LocalDateTime;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import time.TimingClass;

/**
 * Common class for Interactive service
 */
public final class CommonInteractiveClass {
    /**
     * boolean Auto Locale
     */
    private static boolean bolAutoLocale;
    /**
     * default Locale
     */
    public static final String DEFAULT_LOCALE = "en-US";
    /**
     * Exit Code
     */
    private static int exitCode;
    /**
     * Start Date Time
     */
    private static LocalDateTime startDateTime;

    /**
     * Starting sequence
     */
    public static void initializeLocalization() {
        if (bolAutoLocale) {
            final String userLocale = JavaJavaLocalizationClass.getUserLocale();
            JavaJavaLocalizationClass.setLocaleByString(userLocale);
        } else {
            JavaJavaLocalizationClass.setLocaleByString(DEFAULT_LOCALE);
        }
    }

    /**
     * Shut Down sequence
     * @param inOperation main Operation executed
     */
    public static void shutMeDown(final String inOperation) {
        final String strFeedbackExit = String.format("Exiting with code %s", exitCode);
        LogExposureClass.LOGGER.info(strFeedbackExit);
        final String strFeedbackEnd = TimingClass.logDuration(startDateTime,
                String.format(JavaJavaLocalizationClass.getMessage("i18nEntOp"), inOperation));
        LogExposureClass.LOGGER.info(strFeedbackEnd);
    }

    /**
     * Starting sequence
     */
    public static void startMeUp() {
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nNewExec") + "-".repeat(80);
        LogExposureClass.LOGGER.info(strFeedback);
    }

    /**
     * Setter for Auto Locale
     * @param inAutoLocale true or false
     */
    public static void setAutoLocale(final boolean inAutoLocale) {
        bolAutoLocale = inAutoLocale;
    }

    /**
     * Setter for Exit Code
     * @param inExitCode actual Exit Code
     */
    public static void setExitCode(final int inExitCode) {
        exitCode = inExitCode;
    }

    /**
     * Setter for Start DateTime
     */
    public static void setStartDateTime() {
        startDateTime = LocalDateTime.now();
    }

    /**
     * Constructor
     */
    private CommonInteractiveClass() {
        // intentionally blank
    }

}

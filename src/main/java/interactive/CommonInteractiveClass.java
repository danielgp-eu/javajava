package interactive;

import java.time.LocalDateTime;

import org.apache.maven.model.Model;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import project.ProjectClass;
import time.TimingClass;

/**
 * Common class for Interactive service
 */
public final class CommonInteractiveClass {
    /**
     * arity one or more
     */
    /* default */ public static final String ARITY_ONE_OR_MORE = "1..*";
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
     * Short command for Folder
     */
    public static final String FOLDER_CMD_SHORT = "-fldNm";
    /**
     * Long command for Folder
     */
    public static final String FOLDER_CMD_LONG = "--folderName";
    /**
     * Description for Folder
     */
    public static final String FOLDER_DESC = "Folder Name to be inspected";
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
        final String strFeedbackLines = "-".repeat(80);
        LogExposureClass.LOGGER.info(strFeedbackLines);
        final String[] prjProperties = getProjectProperties();
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nNewExec"), prjProperties[0], prjProperties[1], prjProperties[2]);
        LogExposureClass.LOGGER.info(strFeedback);
        LogExposureClass.LOGGER.info(strFeedbackLines);
    }

    /**
     * Get Project Properties
     * @return String array with GroupId, ArtifactId and Version
     */
    private static String[] getProjectProperties() {
        final String[] strToReturn = new String[3];
        final Model projectModel = ProjectClass.getProjectModel();
        strToReturn[0] = projectModel.getGroupId();
        strToReturn[1] = projectModel.getArtifactId();
        strToReturn[2] = projectModel.getVersion();
        return strToReturn;
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

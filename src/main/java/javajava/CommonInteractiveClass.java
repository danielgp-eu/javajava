package javajava;

import java.time.LocalDateTime;

import org.apache.maven.model.Model;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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
     * Start Date Time
     */
    private static LocalDateTime startDateTime;


    /**
     * Starting sequence
     */
    public static void initializeLocalization() {
        if (bolAutoLocale) {
            final String userLocale = LocalizationClass.getUserLocale();
            LocalizationClass.setLocaleByString(userLocale);
        } else {
            LocalizationClass.setLocaleByString(DEFAULT_LOCALE);
        }
    }

    /**
     * Shut Down sequence
     * @param inOperation main Operation executed
     */
    public static void shutMeDown(final String inOperation) {
        final String strFeedbackExit = String.format("Exiting with code %s", exitCode);
        LogExposureClass.LOGGER.info(strFeedbackExit);
        final LocalDateTime finishTimeStamp = LocalDateTime.now();
        final String strFeedbackEnd = TimingClass.logDuration(startDateTime,
                finishTimeStamp,
                String.format(LocalizationClass.getMessage("i18nEntOp"), inOperation));
        LogExposureClass.LOGGER.info(strFeedbackEnd);
    }

    /**
     * Starting sequence
     */
    public static void startMeUp() {
        final String strFeedbackLines = "-".repeat(80);
        LogExposureClass.LOGGER.info(strFeedbackLines);
        final String[] prjProperties = getProjectProperties();
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nNewExec"), prjProperties[0], prjProperties[1], prjProperties[2]);
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
     * Reusable Folder Name for Picocli logic
     */
    @Command(synopsisHeading      = "%nUsage:%n%n",
             descriptionHeading   = "%nDescription:%n%n",
             parameterListHeading = "%nParameters:%n%n",
             optionListHeading    = "%nOptions:%n%n",
             commandListHeading   = "%nCommands:%n%n")
    /* default */ public static class FolderNameOptionMixinClass {

        /**
         * String for FolderName
         */
        @Option(
                names = {"-fldNm", "--folderName"},
                description = "Folder Name in scope",
                arity = ARITY_ONE_OR_MORE,
                required = true)
        private static String[] strFolderNames;

        /**
         * Getter for strFolderNames
         * @return array of Folder Names (1 or many)
         */
        public String[] getFolderNames() {
            return strFolderNames.clone();
        }

    }

    /**
     * Reusable input File Name for Picocli logic
     */
    @Command(synopsisHeading      = "%nUsage:%n%n",
             descriptionHeading   = "%nDescription:%n%n",
             parameterListHeading = "%nParameters:%n%n",
             optionListHeading    = "%nOptions:%n%n",
             commandListHeading   = "%nCommands:%n%n")
    /* default */ public static class InFileNameOptionMixinClass {

        /**
         * String for in FileNames
         */
        @CommandLine.Option(
                names = {"-if", "--inFileName"},
                description = "Input file(s) to consider",
                arity = "1..*",
                required = true)
        private String[] strInFileNames;

        /**
         * Getter for strFileNames
         * @return array of File Names (1 or many)
         */
        public String[] getInFileNames() {
            return strInFileNames.clone();
        }

    }

    /**
     * Reusable output File Name for Picocli logic
     */
    @Command(synopsisHeading      = "%nUsage:%n%n",
             descriptionHeading   = "%nDescription:%n%n",
             parameterListHeading = "%nParameters:%n%n",
             optionListHeading    = "%nOptions:%n%n",
             commandListHeading   = "%nCommands:%n%n")
    /* default */ public static class OutFileNameOptionMixinClass {

        /**
         * String for out FileName
         */
        @CommandLine.Option(
                names = {"-of", "--outFileName"},
                description = "Destination file to write information into",
                arity = "1",
                required = true)
        private String strOutFileName;

        /**
         * Getter for strCsvFileName
         * @return array of CSV File Name (only 1, required)
         */
        public String getOutFileName() {
            return strOutFileName;
        }

    }

    /**
     * Constructor
     */
    private CommonInteractiveClass() {
        // intentionally blank
    }

}

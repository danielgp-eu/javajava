package environment;

import java.util.Properties;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingAssembleClass extends OshiUsageClass {

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        final StringBuilder strJsonString = new StringBuilder(100);
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationCapturing");
        LogExposureClass.LOGGER.info(strFeedback);
        return strJsonString.append('{')
                .append(getHardwareDetails())
                .append(',')
                .append(getSoftwareDetails())
                .append(',')
                .append(getApplicationDetails())
                .append(',')
                .append(getEnvironmentDetails())
                .append('}').toString();
    }

    /**
     * Application details
     * @return String
     */
    private static String getApplicationDetails() {
        final String projDependencies = ProjectDependencyResolverClass.getDependency();
        final Properties projProperties = ProjectModelClass.getProjectProperties();
        final String strDetails = String.format("\"Application\":{\"%s/%s\":\"%s\",\"Dependencies\":%s}", projProperties.get("groupId"), projProperties.get("artifactId"), projProperties.get("version"), projDependencies);
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationApplicationCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Environment details
     * @return String
     */
    private static String getEnvironmentDetails() {
        final String strDetails = String.format("\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME"));
        final String strFeedbackEnv = JavaJavaLocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        return strDetails;
    }

    /**
     * Hardware details
     * @return String
     */
    private static String getHardwareDetails() {
        final String strDetails = String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardwareClass.getDetailsAboutCentralPowerUnit(), EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory(), EnvironmentSoftwareClass.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardwareClass.getDetailsAboutGraphicCards(), EnvironmentHardwareClass.getDetailsAboutMonitor(), EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces());
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Software details
     * @return String
     */
    private static String getSoftwareDetails() {
        final String strDetails = String.format("\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftwareClass.getDetailsAboutOperatingSystem(), EnvironmentSoftwareClass.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftwareClass.getDetailsAboutSoftwareUser());
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

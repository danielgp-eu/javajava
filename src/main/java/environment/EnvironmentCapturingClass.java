package environment;

import dependency.ProjectDependencyResolverClass;
import javajava.CommonClass;
import javajava.LoggerLevelProviderClass;
import localization.JavaJavaLocalizationClass;
import org.apache.logging.log4j.Level;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingClass extends OshiUsageClass {

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationCapturing");
            LoggerLevelProviderClass.LOGGER.info(strFeedback);
        }
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append(String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardwareClass.getDetailsAboutCentralPowerUnit(), EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory(), EnvironmentSoftwareClass.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardwareClass.getDetailsAboutGraphicCards(), EnvironmentHardwareClass.getDetailsAboutMonitor(), EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces()));
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftwareClass.getDetailsAboutOperatingSystem(), EnvironmentSoftwareClass.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftwareClass.getDetailsAboutSoftwareUser()));
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Application\":{\"Dependencies\":%s}", ProjectDependencyResolverClass.getDependency()));
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationApplicationCaptured");
            LoggerLevelProviderClass.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME")));
        if (LoggerLevelProviderClass.getLogLevel().isLessSpecificThan(Level.WARN)) {
            final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured");
            LoggerLevelProviderClass.LOGGER.info(strFeedback);
        }
        return String.format("{%s}", strJsonString);
    }

    /**
     * Constructor
     */
    private  EnvironmentCapturingClass() {
        super();
        throw new UnsupportedOperationException(CommonClass.STR_I18N_AP_CL_WN);
    }
}

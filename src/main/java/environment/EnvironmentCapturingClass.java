package environment;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;
import org.apache.logging.log4j.Level;

import dependency.ProjectDependencyResolver;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingClass extends OshiUsage {

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationCapturing");
            LoggerLevelProvider.LOGGER.info(strFeedback);
        }
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append(String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardware.getDetailsAboutCentralPowerUnit(), EnvironmentHardware.getDetailsAboutRandomAccessMemory(), EnvironmentSoftware.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardware.getDetailsAboutGraphicCards(), EnvironmentHardware.getDetailsAboutMonitor(), EnvironmentHardware.getDetailsAboutNetworkInterfaces()));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationHardwareCaptured");
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftware.getDetailsAboutOperatingSystem(), EnvironmentSoftware.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftware.getDetailsAboutSoftwareUser()));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationSoftwareCaptured");
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Application\":{\"Dependencies\":%s}", ProjectDependencyResolver.getDependency()));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationApplicationCaptured");
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME")));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationEnvironmentCaptured");
            LoggerLevelProvider.LOGGER.info(strFeedback);
        }
        return String.format("{%s}", strJsonString);
    }

    /**
     * Constructor
     */
    private  EnvironmentCapturingClass() {
        super();
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

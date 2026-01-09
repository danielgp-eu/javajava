package environment;

import dependency.ProjectDependencyResolverClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposure;

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
        LogExposure.exposeMessageToInfoLog(JavaJavaLocalizationClass.getMessage("i18nAppInformationCapturing"));
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append(String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardwareClass.getDetailsAboutCentralPowerUnit(), EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory(), EnvironmentSoftwareClass.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardwareClass.getDetailsAboutGraphicCards(), EnvironmentHardwareClass.getDetailsAboutMonitor(), EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces()));
        LogExposure.exposeMessageToDebugLog(JavaJavaLocalizationClass.getMessage("i18nAppInformationHardwareCaptured"));
        strJsonString.append(String.format(",\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftwareClass.getDetailsAboutOperatingSystem(), EnvironmentSoftwareClass.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftwareClass.getDetailsAboutSoftwareUser()));
        LogExposure.exposeMessageToDebugLog(JavaJavaLocalizationClass.getMessage("i18nAppInformationSoftwareCaptured"));
        strJsonString.append(String.format(",\"Application\":{\"Dependencies\":%s}", ProjectDependencyResolverClass.getDependency()));
        LogExposure.exposeMessageToDebugLog(JavaJavaLocalizationClass.getMessage("i18nAppInformationApplicationCaptured"));
        strJsonString.append(String.format(",\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME")));
        LogExposure.exposeMessageToDebugLog(JavaJavaLocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured"));
        return String.format("{%s}", strJsonString);
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingClass() {
        super();
    }
}

package javajava;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingAssembleClass {

    /**
     * Environment details gathered
     * @return Map
     */
    private static Map<String, String> gatherEnvironmentDetails() {
        return Map.of(
                "Computer", System.getenv("COMPUTERNAME"),
                "Username", System.getenv("USERNAME"),
                "Country", System.getProperty("user.country"),
                "Country.Format", System.getProperty("user.country.format"),
                "Language", System.getProperty("user.language"),
                "Language.Format", System.getProperty("user.language.format"),
                "Home", System.getProperty("user.home").replace("\\", "\\\\"),
                "Name", System.getProperty("user.name"),
                "Timezone", System.getProperty("user.timezone"));
    }

    /**
     * Environment details gathered
     * @return Map
     */
    private static Map<String, Object> gatherJavaDetails() {
        return Map.of(
                "Date", System.getProperty("java.version.date"),
                "Release", System.getProperty("java.vendor.version"),
                "Runtime", System.getProperty("java.runtime.name"),
                BasicStructuresClass.STR_VERSION, System.getProperty("java.version"),
                "Vendor", System.getProperty("java.vendor"),
                "VM", System.getProperty("java.vm.name"));
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String packageCurrentEnvironmentDetailsIntoJson() {
        final StringBuilder strJsonString = new StringBuilder();
        final String strFeedback = LocalizationClass.getMessage("i18nAppInformationCapturing");
        LogExposureClass.LOGGER.info(strFeedback);
        final String strHardware = "\"Hardware\":{"
                + "\"CPU\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutCentralProcessorUnit())
                + "\"RAM\":" + HardwareClass.getDetailsAboutRandomAccessMemory()
                + "\"GPU\":" + HardwareClass.getDetailsAboutGraphicCards()
                + "\"Monitor\":" + HardwareClass.getDetailsAboutMonitor()
                + "\"Network Interface\":" + HardwareClass.getDetailsAboutNetworkInterfaces()
                + "\"Storage\":" + OshiUsageClass.getDetailsAboutAvailableStoragePartitions()
                + "\"System\"" + HardwareClass.getDetailsAboutComputerSystem()
                + "}";
        final String strFeedbackH = LocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackH);
        final String strSoftware = "\"Software\":{"
                + "\"OS\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutOperatingSystem())
                + "\"Java\":" + JsonOperationsClass.getMapIntoJsonString(gatherJavaDetails())
                + "\"Network\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutNetwork())
                + "}";
        final String strFeedbackS = LocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackS);
        final String strEnvironment = "\"Environment\":{" + gatherEnvironmentDetails() + "}";
        final String strFeedbackEnv = LocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        return strJsonString.append('{')
                .append(strHardware)
                .append(',')
                .append(strSoftware)
                .append(',')
                .append(ProjectClass.Application.getApplicationDetails())
                .append(',')
                .append(strEnvironment)
                .append('}').toString();
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static List<Properties> packageCurrentEnvironmentDetailsIntoListOfProperties() {
        final List<Properties> resultReleases = new ArrayList<>();
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Environment", gatherEnvironmentDetails()));
        return resultReleases;
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

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
    private static Map<String, Object> gatherEnvironmentDetails() {
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
                "Release", System.getProperty("java.vendor.version"),
                "Runtime Name", System.getProperty("java.runtime.name"),
                "Runtime Version", System.getProperty("java.runtime.version"),
                "Vendor", System.getProperty("java.vendor"),
                BasicStructuresClass.STR_VERSION, System.getProperty("java.version"),
                "Version Date", System.getProperty("java.version.date"),
                "VM Name", System.getProperty("java.vm.name"),
                "VM Version", System.getProperty("java.vm.version"),
                "VM Specification Name", System.getProperty("java.vm.specification.name"),
                "VM Specification Vendor", System.getProperty("java.vm.specification.vendor"));
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
                + ",\"GPU\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutGraphicCards())
                + ",\"Mainboard\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutMainboard())
                + ",\"Monitor\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutMonitor())
                + ",\"Network Interface\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutNetworkInterfaces())
                + ",\"RAM\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutRandomAccessMemory())
                + "}";
        final String strFeedbackH = LocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackH);
        final String strSoftware = "\"Software\":{"
                + "\"OS\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutOperatingSystem())
                + ",\"Java\":" + JsonOperationsClass.getMapIntoJsonString(gatherJavaDetails())
                + ",\"Network\":" + JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutNetwork())
                + ",\"Storage\":" + JsonOperationsClass.getMapIntoJsonString(OshiUsageClass.getDetailsAboutAvailableStoragePartitions())
                + "}";
        final String strFeedbackS = LocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackS);
        final String strEnvironment = "\"Environment\":" + JsonOperationsClass.getMapIntoJsonString(gatherEnvironmentDetails());
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
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - CPU", HardwareClass.getDetailsAboutCentralProcessorUnit()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - GPU", HardwareClass.getDetailsAboutGraphicCards()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - Mainboard", HardwareClass.getDetailsAboutMainboard()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - Monitors", HardwareClass.getDetailsAboutMonitor()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - Network Interaces", HardwareClass.getDetailsAboutNetworkInterfaces()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Hardware - RAM", HardwareClass.getDetailsAboutRandomAccessMemory()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Software - Java", gatherJavaDetails()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Software - OS", HardwareClass.getDetailsAboutOperatingSystem()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Software - Network", HardwareClass.getDetailsAboutNetwork()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapClass.convertMapOfStringsIntoListOfProperties("Software - Storage", OshiUsageClass.getDetailsAboutAvailableStoragePartitions()));
        return resultReleases;
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

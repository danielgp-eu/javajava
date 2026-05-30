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
        String strComputer = System.getenv("COMPUTERNAME");
        if (strComputer == null) {
            strComputer = System.getenv("HOSTNAME");
        }
        final String userAccount = ShellingClass.getCurrentUserAccount();
        return Map.of(
                "Computer", strComputer != null ? strComputer : "---",
                "Country", System.getProperty("user.country", "---"),
                "Country.Format", System.getProperty("user.country.format", "---"),
                "Language", System.getProperty("user.language", "---"),
                "Language.Format", System.getProperty("user.language.format", "---"),
                "Home", System.getProperty("user.home", "---").replace("\\", "\\\\"),
                "Name", System.getProperty("user.name", "---"),
                "Timezone", System.getProperty("user.timezone", "---"),
                "Username", System.getenv("USERNAME") != null ? System.getenv("USERNAME") : System.getProperty("user.name", "---"),
                "User Account", userAccount != null ? userAccount : "---"));
    }

    /**
     * Hardware details gathered
     * @return Map
     */
    private static Map<String, Object> gatherHardwareDetails() {
        return Map.of(
                "CPU", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutCentralProcessorUnit()),
                "GPU", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutGraphicCards()),
                "Mainboard", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutMainboard()),
                "Monitor", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutMonitor()),
                "Network Interface", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutNetworkInterfaces()),
                "RAM", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutRandomAccessMemory()));
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
     * Software details gathered
     * @return Map
     */
    private static Map<String, Object> gatherSoftwareDetails() {
        return Map.of(
                "Java", JsonOperationsClass.getMapIntoJsonString(gatherJavaDetails()),
                "OS", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutOperatingSystem()),
                "Network", JsonOperationsClass.getMapIntoJsonString(HardwareClass.getDetailsAboutNetwork()),
                "Storage", JsonOperationsClass.getMapIntoJsonString(OshiUsageClass.getDetailsAboutAvailableStoragePartitions()));
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String packageCurrentEnvironmentDetailsIntoJson() {
        final StringBuilder strJsonString = new StringBuilder(1000).append('{');
        final String strFeedback = "Capturing information...";
        LogExposureClass.LOGGER.info(strFeedback);
        final String strHardware = JsonOperationsClass.getMapIntoJsonString(gatherHardwareDetails());
        if (strHardware != null) {
            strJsonString.append("\"Hardware\":").append(strHardware);
        }
        final String strFeedbackH = "I just captured Hardware information...";
        LogExposureClass.LOGGER.debug(strFeedbackH);
        final String strSoftware = JsonOperationsClass.getMapIntoJsonString(gatherSoftwareDetails());
        if (strSoftware != null) {
            strJsonString.append(",\"Software\":").append(strSoftware);
        }
        final String strFeedbackS = "I just captured Software information...";
        LogExposureClass.LOGGER.debug(strFeedbackS);
        final String strAppDetails = ProjectClass.ApplicationSubClass.getApplicationDetails();
        if (strAppDetails != null) {
            strJsonString.append(',').append(strAppDetails);
        }
        final String strEnvironment = JsonOperationsClass.getMapIntoJsonString(gatherEnvironmentDetails());
        final String strFeedbackEnv = "I just captured Environment information...";
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        if (strEnvironment != null) {
            strJsonString.append(",\"Environment\":").append(strEnvironment);
        }
        return strJsonString.append('}').toString();
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static List<Properties> packageCurrentEnvironmentDetailsIntoListOfProperties() {
        final List<Properties> resultReleases = new ArrayList<>();
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Environment", gatherEnvironmentDetails()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - CPU", HardwareClass.getDetailsAboutCentralProcessorUnit()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - GPU", HardwareClass.getDetailsAboutGraphicCards()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Mainboard", HardwareClass.getDetailsAboutMainboard()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Monitors", HardwareClass.getDetailsAboutMonitor()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - Network Interfaces", HardwareClass.getDetailsAboutNetworkInterfaces()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Hardware - RAM", HardwareClass.getDetailsAboutRandomAccessMemory()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Java", gatherJavaDetails()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - OS", HardwareClass.getDetailsAboutOperatingSystem()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Network", HardwareClass.getDetailsAboutNetwork()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Software - Storage", OshiUsageClass.getDetailsAboutAvailableStoragePartitions()));
        resultReleases.addAll(BasicStructuresClass.ListAndMapSubClass.convertMapOfStringsIntoListOfProperties("Application", ProjectClass.ApplicationSubClass.getApplicationDetailsIntoMap()));
        return resultReleases;
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

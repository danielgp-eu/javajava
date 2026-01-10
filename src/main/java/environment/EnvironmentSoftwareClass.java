package environment;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import structure.ListAndMapClass;

import java.util.List;
import java.util.Map;

/**
 * Capturing current environment software details
 */
public final class EnvironmentSoftwareClass {

    /**
     * List with all partitions
     * 
     * @return String
     */
    public static String getDetailsAboutAvailableStoragePartitions() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final FileSystem osFileSystem = OshiUsageClass.OshiSoftware.getOshiFileSystem();
        final List<OSFileStore> osFileStores = osFileSystem.getFileStores();
        strJsonString.append("\"Partition(s)\":[");
        int strCounterDisk = 0;
        for(final OSFileStore fileStore : osFileStores) {
            if (strCounterDisk > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(ListAndMapClass.getMapIntoJsonString(Map.of(
                "Description", fileStore.getDescription(),
                "Label", fileStore.getLabel(),
                "Logical Volume", fileStore.getLogicalVolume(),
                "Mount", fileStore.getMount().replace("\\", "\\\\"),
                OshiUsageClass.STR_NAME, fileStore.getName(),
                "Options", fileStore.getOptions(),
                "Total Space", FormatUtil.formatBytes(fileStore.getTotalSpace()),
                "Type", fileStore.getType(),
                "UUID", fileStore.getUUID(),
                "Usable Space", FormatUtil.formatBytes(fileStore.getUsableSpace())
            )));
            strCounterDisk++;
        }
        strJsonString.append(']'); // from Disk(s)
        return strJsonString.toString();
    }

    /**
     * Operating System details
     *
     * @return String
     */
    public static String getDetailsAboutOperatingSystem() {
        final OperatingSystem.OSVersionInfo version = OshiUsageClass.OshiSoftware.getOshiVersionInfo();
        return ListAndMapClass.getMapIntoJsonString(Map.of(
                "Architecture", System.getProperty("os.arch"),
                "Build", version.getBuildNumber(),
                "Code", version.getCodeName(),
                "Family", OshiUsageClass.OshiSoftware.getOshiFamily(),
                "Manufacturer", OshiUsageClass.OshiSoftware.getOshiManufacturer(),
                OshiUsageClass.STR_NAME, System.getProperty("os.name"),
                "Platform", SystemInfo.getCurrentPlatform().toString(),
                "Version", version.getVersion()
        ));
    }

    /**
     * JAVA info
     * 
     * @return String
     */
    public static String getDetailsAboutSoftwareUser() {
        return ListAndMapClass.getMapIntoJsonString(Map.of(
            "Country", System.getProperty("user.country"),
            "Country.Format", System.getProperty("user.country.format"),
            "Language", System.getProperty("user.language"),
            "Language.Format", System.getProperty("user.language.format"),
            "Home", System.getProperty("user.home").replace("\\", "\\\\"),
            "Name", System.getProperty("user.name"),
            "Timezone", System.getProperty("user.timezone")
        ));
    }

    /**
     * JAVA info
     * 
     * @return String
     */
    public static String getDetailsAboutSoftwarePlatformJava() {
        return ListAndMapClass.getMapIntoJsonString(Map.of(
            "Date", System.getProperty("java.version.date"),
            "Release", System.getProperty("java.vendor.version"),
            "Runtime", System.getProperty("java.runtime.name"),
            "Version", System.getProperty("java.version"),
            "Vendor", System.getProperty("java.vendor"),
            "VM", System.getProperty("java.vm.name")
        ));
    }

    /**
     * Constructor empty
     */
    private EnvironmentSoftwareClass() {
        // intentionally left blank
    }

}
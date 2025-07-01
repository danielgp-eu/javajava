package javajava;
/* Java utility classes */
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
/* Logging */
import org.apache.logging.log4j.Level;
/* OSHI Hardware/Software classes */
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.PhysicalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingClass {
    /**
     * Hardware info
     */
    private final static SystemInfo systemInfo = new SystemInfo();
    /**
     * Hardware info
     */
    private final static HardwareAbstractionLayer hardware = systemInfo.getHardware(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
    /**
     * OS info
     */
    private final static OperatingSystem operatingSystem = systemInfo.getOperatingSystem(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
    /**
     * string constant
     */
    private final static String STR_ACTV_PXLS = "Active Pixels";
    /**
     * string constant
     */
    private final static String STR_MONITOR_NAME = "Monitor Name";
    /**
     * string constant
     */
    private final static String STR_PHYSC_DIM = "Physical Dimensions";
    /**
     * string constant
     */
    private final static String STR_PRFRD_TM_CLCK = "Preferred Timing Clock";
    /**
     * string constant
     */
    private final static String STR_RANGE_LMTS = "Range Limits";
    /**
     * string constant
     */
    private final static String STR_SRL_NUM = "Serial Number";
    /**
     * Display details
     * 
     * @param crtDisplay current Display object
     * @return String
     */
    private static String digestDisplayDetails(final Display crtDisplay) {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final String[] arrayDetails = crtDisplay.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\n");
        for (final String crtLine : arrayDetails) {
            final String strSlimLine = crtLine.trim();
            if (strSlimLine.endsWith(" in") && strSlimLine.contains(" cm ")) {
                final int intCmPos = strSlimLine.indexOf(" cm ");
                arrayAttributes.put(STR_PHYSC_DIM + " [in]", strSlimLine.substring(0, intCmPos));
                final int intInPos = strSlimLine.indexOf(" in");
                arrayAttributes.put(STR_PHYSC_DIM + " [cm]", strSlimLine.substring(intCmPos + 4, intInPos));
            }
            if (strSlimLine.startsWith(STR_MONITOR_NAME)) {
                arrayAttributes.put(STR_MONITOR_NAME, strSlimLine.replace(STR_MONITOR_NAME + " ", ""));
            }
            if (strSlimLine.startsWith(STR_PRFRD_TM_CLCK)) {
                final int intClockLen = STR_PRFRD_TM_CLCK.length();
                final int intPixelPos = strSlimLine.indexOf("Active Pixels");
                arrayAttributes.put(STR_PRFRD_TM_CLCK, strSlimLine.substring(intClockLen, intPixelPos).trim());
                arrayAttributes.put(STR_ACTV_PXLS, strSlimLine.substring(intPixelPos).replace(STR_ACTV_PXLS + " ", "").trim());
            }
            if (strSlimLine.startsWith(STR_RANGE_LMTS)) {
                arrayAttributes.put(STR_RANGE_LMTS, strSlimLine.replace(STR_RANGE_LMTS + " ", ""));
            }
            if (strSlimLine.startsWith(STR_SRL_NUM)) {
                arrayAttributes.put(STR_SRL_NUM, strSlimLine.replace(STR_SRL_NUM + " ", ""));
            }
        }
        return Common.getMapIntoJsonString(arrayAttributes);
    }

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
        strJsonString.append(String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", getDetailsAboutCentralPowerUnit(), getDetailsAboutRandomAccessMemory(), getDetailsAboutAvailableStoragePartitions(), getDetailsAboutGraphicCards(), getDetailsAboutMonitor(), getDetailsAboutNetworkInterfaces()));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationHardwareCaptured");
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", getDetailsAboutOperatingSystem(), Common.getDetailsAboutSoftwarePlatformJava(), Common.getDetailsAboutSoftwareUser()));
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationSoftwareCaptured");
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        strJsonString.append(String.format(",\"Application\":{\"Dependencies\":%s}", DependenciesClass.getCurrentDependencies()));
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
     * List with all partitions
     * 
     * @return String
     */
    private static String getDetailsAboutAvailableStoragePartitions() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final FileSystem osFileSystem = operatingSystem.getFileSystem();
        final List<OSFileStore> osFileStores = osFileSystem.getFileStores();
        strJsonString.append("\"Partition(s)\":[");
        int strCounterDisk = 0;
        for(final OSFileStore fileStore : osFileStores) {
            if (strCounterDisk > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Description", fileStore.getDescription(),
                "Label", fileStore.getLabel(),
                "Logical Volume", fileStore.getLogicalVolume(),
                "Mount", fileStore.getMount().replace("\\", "\\\\"),
                Common.STR_NAME, fileStore.getName(),
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
     * Capturing details about CPU
     * 
     * @return String
     */
    private static String getDetailsAboutCentralPowerUnit() {
        final CentralProcessor processor = hardware.getProcessor();
        final CentralProcessor.ProcessorIdentifier procIdentif = processor.getProcessorIdentifier();
        return Common.getMapIntoJsonString(Map.of(
            "Feature Flags", processor.getFeatureFlags().toString().replace("[", "[\"").replace(", ", "\",\"").replace("]", "\"]"),
            "Family", procIdentif.getFamily(),
            "Identifier", procIdentif.getIdentifier(),
            "Local Processors", processor.getLogicalProcessorCount(),
            "Model", procIdentif.getModel(),
            Common.STR_NAME, procIdentif.getName(),
            "Physical", processor.getPhysicalProcessorCount()
        ));
    }

    /**
     * GPU info
     * 
     * @return String
     */
    private static String getDetailsAboutGraphicCards() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final List<GraphicsCard> graphicCards = hardware.getGraphicsCards(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
        int intCounter = 0;
        for (final GraphicsCard  graphicCard : graphicCards) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Device Id", graphicCard.getDeviceId(),
                Common.STR_NAME, graphicCard.getName(),
                "Vendor", graphicCard.getVendor(),
                "VRAM", FormatUtil.formatBytes(graphicCard.getVRam()),
                "Driver Version", graphicCard.getVersionInfo()
            )));
            intCounter++;
        }
        return String.format("[%s]", strJsonString);
    }

    /**
     * GPU info
     * 
     * @return String
     */
    private static String getDetailsAboutMonitor() {
        final StringBuilder strJsonString = new StringBuilder();
        final List<Display> displays = hardware.getDisplays(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
        int intCounter = 0;
        for (final Display crtDisplay : displays) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(digestDisplayDetails(crtDisplay));
            intCounter++;
        }
        return String.format("[%s]", strJsonString);
    }

    /**
     * Operating System details
     * 
     * @return String
     */
    private static String getDetailsAboutOperatingSystem() {
        final OperatingSystem.OSVersionInfo versionInfo = operatingSystem.getVersionInfo();
        return Common.getMapIntoJsonString(Map.of(
            "Architecture", System.getProperty("os.arch"),
            "Build", versionInfo.getBuildNumber(),
            "Code", versionInfo.getCodeName(),
            "Family", operatingSystem.getFamily(),
            "Manufacturer", operatingSystem.getManufacturer(),
            Common.STR_NAME, System.getProperty("os.name"),
            "Platform", SystemInfo.getCurrentPlatform().toString(),
            "Version", versionInfo.getVersion()
        ));
    }

    /**
     * Capturing RAM information
     * 
     * @return String
     */
    private static String getDetailsAboutRandomAccessMemory() {
        final GlobalMemory globalMemory = hardware.getMemory();
        final StringBuilder strJsonString = new StringBuilder();
        strJsonString.append(String.format("{\"Total\":{\"Total\":\"%s\",\"Available\":\"%s\",\"Page Size\":\"%s\"}"
            , FormatUtil.formatBytes(globalMemory.getTotal())
            , FormatUtil.formatBytes(globalMemory.getAvailable())
            , FormatUtil.formatBytes(globalMemory.getPageSize())));
        final List<PhysicalMemory> physicalMemories = globalMemory.getPhysicalMemory();
        strJsonString.append(",\"Banks\":[");
        int intCounter = 0;
        for (final PhysicalMemory physicalMemory : physicalMemories) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Bank/Slot Label", physicalMemory.getBankLabel(),
                "Capacity", FormatUtil.formatBytes(physicalMemory.getCapacity()),
                "Clock Speed", FormatUtil.formatHertz(physicalMemory.getClockSpeed()),
                "Manufacturer", physicalMemory.getManufacturer(),
                "Type", physicalMemory.getMemoryType(),
                "Part Number", physicalMemory.getPartNumber().trim(),
                "Serial Number", physicalMemory.getSerialNumber()
            )));
            intCounter++;
        }
        strJsonString.append("]}");
        return strJsonString.toString();
    }

    /**
     * Sensors Information
     * 
     * @return String
     */
    private static String getDetailsAboutNetworkInterfaces() {
        final List<NetworkIF> networkIFs = hardware.getNetworkIFs(); // NOPMD by Daniel Popiniuc on 04.06.2025, 23:31
        final StringBuilder strJsonString = new StringBuilder();
        int intCounter = 0;
        strJsonString.append('[');
        for (final NetworkIF net : networkIFs) {
            net.updateAttributes(); // Refresh interface stats
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                Common.STR_NAME, net.getName(),
                "Display Name", net.getDisplayName(),
                "MAC Address", net.getMacaddr(),
                "IPv4", String.join(", ", net.getIPv4addr()),
                "IPv6", String.join(", ", net.getIPv6addr()),
                "Status", net.getIfOperStatus(),
                "Speed", FormatUtil.formatBytes(net.getSpeed()),
                "NDIS Physical Medium Type", Common.getNetworkPhysicalMediumType(net.getNdisPhysicalMediumType())
            )));
            intCounter++;
        }
        strJsonString.append(']');
        return strJsonString.toString();
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

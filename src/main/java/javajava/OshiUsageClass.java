package javajava;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oshi.SystemInfoFFM;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.NetworkParams;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * initiating OSHI package
 */
public final class OshiUsageClass {
    /**
     * Hardware info
     */
    public static final SystemInfoFFM SYSTEM_INFO = new SystemInfoFFM();
    /**
     * Map with predefined network physical types
     */
    private static final Map<Integer, String> MEDIUM_TYPES;

    static {
        // Initialize the concurrent map
        final Map<Integer, String> tempMap = new ConcurrentHashMap<>();
        tempMap.put(0, "Unspecified (e.g., satellite feed)");
        tempMap.put(1, "Wireless LAN (802.11)");
        tempMap.put(2, "Cable Modem (DOCSIS)");
        tempMap.put(3, "Phone Line (HomePNA)");
        tempMap.put(4, "Power Line (data over electrical wiring)");
        tempMap.put(5, "DSL (ADSL, G.Lite)");
        tempMap.put(6, "Fibre Channel (high-speed storage interconnect)");
        tempMap.put(7, "IEEE 1394 (FireWire)");
        tempMap.put(8, "Wireless WAN (CDMA, GPRS)");
        tempMap.put(9, "Native 802.11 (modern Wi-Fi interface)");
        tempMap.put(10, "Bluetooth (short-range wireless)");
        tempMap.put(11, "InfiniBand (high-speed interconnect)");
        tempMap.put(12, "Ultra Wideband (UWB)");
        tempMap.put(13, "Ethernet (802.3)");
        // Make the map unmodifiable
        MEDIUM_TYPES = Collections.unmodifiableMap(tempMap);
    }

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
            strJsonString.append(JsonOperationsClass.getMapIntoJsonString(Map.of(
                "Description", fileStore.getDescription(),
                "Label", fileStore.getLabel(),
                "Logical Volume", fileStore.getLogicalVolume(),
                "Mount", fileStore.getMount().replace("\\", "\\\\"),
                BasicStructuresClass.STR_NAME, fileStore.getName(),
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
     * Sensors Information
     * @param intPhysMedType number for NDIS Physical Medium Type
     * @return String
     */
    public static String getNetworkPhysicalMediumType(final int intPhysMedType) {
        return JsonOperationsClass.getMapIntoJsonString(
                Map.of("Numeric", intPhysMedType,
                        BasicStructuresClass.STR_NAME, 
                        MEDIUM_TYPES.getOrDefault(intPhysMedType, "Unknown"))
        );
    }

    /**
     * initiating Hardware package
     */
    public static final class OshiHardware {

        /**
         * Hardware info
         */
        private static HardwareAbstractionLayer getOshiHardware() {
            return SYSTEM_INFO.getHardware();
        }

        /**
         * Computer System info
         * @return ComputerSystem
         */
        public static ComputerSystem getOshiComputerSystem() {
            return getOshiHardware().getComputerSystem();
        }

        /**
         * Computer System Firmware
         * @return Firmware
         */
        public static Firmware getOshiFirmware() {
            return getOshiComputerSystem().getFirmware();
        }

        /**
         * get Video card attributes
         * @return List of GraphicsCard
         */
        public static List<GraphicsCard> getOshiGraphicsCards() {
            return getOshiHardware().getGraphicsCards();
        }

        /**
         * Computer System Motherboard
         * @return Baseboard
         */
        public static Baseboard getOshiMotherboard() {
            return getOshiComputerSystem().getBaseboard();
        }

        /**
         * get RAM attributes
         * @return GlobalMemory
         */
        public static GlobalMemory getOshiMemory() {
            return getOshiHardware().getMemory();
        }

        /**
         * get Video card attributes
         * @return List of Display
         */
        public static List<Display> getOshiMonitor() {
            return getOshiHardware().getDisplays();
        }

        /**
         * get Network attributes
         * @return List of NetworkIF
         */
        private static List<NetworkIF> getOshiNetworkInterfacesRaw() {
            return getOshiHardware().getNetworkIFs();
        }

        /**
         * get Network attributes and filter to retain UP ones
         * @return List of NetworkIF
         */
        public static List<NetworkIF> getOshiNetworkInterfaces() {
            return getOshiNetworkInterfacesRaw().stream()
                    .filter(net -> net.getIfOperStatus() == NetworkIF.IfOperStatus.UP)
                    .filter(net -> net.getIPv4addr().length != 0 || net.getIPv6addr().length != 0)
                    .toList();
        }

        /**
         * get CPU attributes
         * @return CentralProcessor
         */
        public static CentralProcessor getOshiProcessor() {
            return getOshiHardware().getProcessor();
        }

        /**
         * get CPU identifier
         * @return CentralProcessor
         */
        public static CentralProcessor.ProcessorIdentifier getOshiProcessorIdentifier() {
            return getOshiProcessor().getProcessorIdentifier();
        }

        /**
         * get Virtual Memory
         * @return VirtualMemory
         */
        public static VirtualMemory getOshiVirtualMemory() {
            return getOshiMemory().getVirtualMemory();
        }

        /**
         * Constructor
         */
        private OshiHardware() {
            // intentionally left blank
        }

    }

    /**
     * initiating Software package
     */
    public static final class OshiSoftware {

        /**
         * Software info
         */
        private static OperatingSystem getOshiSoftware() {
            return SYSTEM_INFO.getOperatingSystem();
        }

        /**
         * get OS Family
         * @return OperatingSystem Family
         */
        public static String getOshiFamily() {
            return getOshiSoftware().getFamily();
        }

        /**
         * get File System attributes
         * @return FileSystem
         */
        public static FileSystem getOshiFileSystem() {
            return getOshiSoftware().getFileSystem();
        }

        /**
         * get OS Manufacturer
         * @return OperatingSystem Manufacturer
         */
        public static String getOshiManufacturer() {
            return getOshiSoftware().getManufacturer();
        }

        /**
         * get NetworkParameters
         * @return Network parameters
         */
        public static NetworkParams getOshiNetworkParameters() {
            return getOshiSoftware().getNetworkParams();
        }

        /**
         * get Version information
         * @return OperatingSystem.OSVersionInfo
         */
        public static OperatingSystem.OSVersionInfo getOshiVersionInfo() {
            return getOshiSoftware().getVersionInfo();
        }

        /**
         * Constructor
         */
        private OshiSoftware() {
            // intentionally left blank
        }

    }

    /**
     * Constructor empty
     */
    private OshiUsageClass() {
        // no init required
    }

}

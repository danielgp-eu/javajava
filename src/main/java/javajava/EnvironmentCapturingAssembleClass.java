package javajava;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oshi.SystemInfo;
import oshi.SystemInfoFFM;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingAssembleClass {

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        final StringBuilder strJsonString = new StringBuilder();
        final String strFeedback = LocalizationClass.getMessage("i18nAppInformationCapturing");
        LogExposureClass.LOGGER.info(strFeedback);
        return strJsonString.append('{')
                .append(getHardwareDetails())
                .append(',')
                .append(getSoftwareDetails())
                .append(',')
                .append(ProjectClass.Application.getApplicationDetails())
                .append(',')
                .append(getEnvironmentDetails())
                .append('}').toString();
    }

    /**
     * Environment details
     * @return String
     */
    private static String getEnvironmentDetails() {
        final String strDetails = String.format("\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME"));
        final String strFeedbackEnv = LocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        return strDetails;
    }

    /**
     * Hardware details
     * @return String
     */
    private static String getHardwareDetails() {
        final String strDetails = String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardwareClass.getDetailsAboutCentralPowerUnit(), EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory(), EnvironmentSoftwareClass.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardwareClass.getDetailsAboutGraphicCards(), EnvironmentHardwareClass.getDetailsAboutMonitor(), EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces());
        final String strFeedback = LocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Software details
     * @return String
     */
    private static String getSoftwareDetails() {
        final String strDetails = String.format("\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftwareClass.getDetailsAboutOperatingSystem(), EnvironmentSoftwareClass.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftwareClass.getDetailsAboutSoftwareUser());
        final String strFeedback = LocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Capturing current environment software details
     */
    private static final class EnvironmentSoftwareClass {

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
            return JsonOperationsClass.getMapIntoJsonString(Map.of(
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
            return JsonOperationsClass.getMapIntoJsonString(Map.of(
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
            return JsonOperationsClass.getMapIntoJsonString(Map.of(
                "Date", System.getProperty("java.version.date"),
                "Release", System.getProperty("java.vendor.version"),
                "Runtime", System.getProperty("java.runtime.name"),
                "Version", System.getProperty("java.version"),
                "Vendor", System.getProperty("java.vendor"),
                "VM", System.getProperty("java.vm.name")
            ));
        }

    }

    /**
     * Capturing current environment hardware details
     */
    private static final class EnvironmentHardwareClass {
        /**
         * string constant
         */
        private static final String STR_ACTV_PXLS = "Active Pixels";
        /**
         * string constant
         */
        private static final String STR_MONITOR_NAME = "Monitor Name";
        /**
         * string constant
         */
        private static final String STR_PHYSC_DIM = "Physical Dimensions";
        /**
         * string constant
         */
        private static final String STR_PRFRD_TM_CLCK = "Preferred Timing Clock";
        /**
         * string constant
         */
        private static final String STR_RANGE_LMTS = "Range Limits";
        /**
         * string constant
         */
        private static final String STR_SRL_NUM = "Serial Number";
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
                    final int intPixelPos = strSlimLine.indexOf(STR_ACTV_PXLS);
                    arrayAttributes.put(STR_PRFRD_TM_CLCK, strSlimLine.substring(intClockLen, intPixelPos).trim());
                    arrayAttributes.put(STR_ACTV_PXLS, strSlimLine.substring(intPixelPos)
                        .replace(STR_ACTV_PXLS + " ", "").trim());
                }
                if (strSlimLine.startsWith(STR_RANGE_LMTS)) {
                    arrayAttributes.put(STR_RANGE_LMTS, strSlimLine.replace(STR_RANGE_LMTS + " ", ""));
                }
                if (strSlimLine.startsWith(STR_SRL_NUM)) {
                    arrayAttributes.put(STR_SRL_NUM, strSlimLine.replace(STR_SRL_NUM + " ", ""));
                }
            }
            return JsonOperationsClass.getMapIntoJsonString(arrayAttributes);
        }

        /**
         * Capturing details about CPU
         * @return String
         */
        public static String getDetailsAboutCentralPowerUnit() {
            final CentralProcessor processor = OshiUsageClass.OshiHardware.getOshiProcessor();
            final CentralProcessor.ProcessorIdentifier procIdentif = OshiUsageClass.OshiHardware.getOshiProcessorIdentifier();
            return JsonOperationsClass.getMapIntoJsonString(Map.of(
                "Feature Flags", processor.getFeatureFlags().toString().replace("[", "[\"").replace(", ", "\",\"").replace("]", "\"]"),
                "Family", procIdentif.getFamily(),
                "Identifier", procIdentif.getIdentifier(),
                "Local Processors", processor.getLogicalProcessorCount(),
                "Model", procIdentif.getModel(),
                OshiUsageClass.STR_NAME, procIdentif.getName(),
                "Physical", processor.getPhysicalProcessorCount()
            ));
        }

        /**
         * GPU info
         *
         * @return String
         */
        public static String getDetailsAboutGraphicCards() {
            final StringBuilder strJsonString = new StringBuilder(50);
            final List<GraphicsCard> graphicCards = OshiUsageClass.OshiHardware.getOshiGraphicsCards();
            int intCounter = 0;
            for (final GraphicsCard  graphicCard : graphicCards) {
                if (intCounter > 0) {
                    strJsonString.append(',');
                }
                strJsonString.append(JsonOperationsClass.getMapIntoJsonString(Map.of(
                    "Device Id", graphicCard.getDeviceId(),
                    OshiUsageClass.STR_NAME, graphicCard.getName(),
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
        public static String getDetailsAboutMonitor() {
            final StringBuilder strJsonString = new StringBuilder();
            final List<Display> displays = OshiUsageClass.OshiHardware.getOshiMonitor();
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
         * Sensors Information
         *
         * @return String
         */
        public static String getDetailsAboutNetworkInterfaces() {
            final List<NetworkIF> networkIFs = OshiUsageClass.OshiHardware.getOshiNetworkInterfaces();
            final StringBuilder strJsonString = new StringBuilder();
            int intCounter = 0;
            strJsonString.append('[');
            for (final NetworkIF net : networkIFs) {
                net.updateAttributes(); // Refresh interface stats
                if (intCounter > 0) {
                    strJsonString.append(',');
                }
                strJsonString.append(JsonOperationsClass.getMapIntoJsonString(Map.of(
                    OshiUsageClass.STR_NAME, net.getName(),
                    "Display Name", net.getDisplayName(),
                    "MAC Address", net.getMacaddr(),
                    "IPv4", String.join(", ", net.getIPv4addr()),
                    "IPv6", String.join(", ", net.getIPv6addr()),
                    "Status", net.getIfOperStatus(),
                    "Speed", FormatUtil.formatBytes(net.getSpeed()),
                    "NDIS Physical Medium Type", NetworkTypesClass.getNetworkPhysicalMediumType(net.getNdisPhysicalMediumType())
                )));
                intCounter++;
            }
            strJsonString.append(']');
            return strJsonString.toString();
        }

        /**
         * Capturing RAM information
         *
         * @return String
         */
        public static String getDetailsAboutRandomAccessMemory() {
            final GlobalMemory globalMemory = OshiUsageClass.OshiHardware.getOshiMemory();
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
                strJsonString.append(JsonOperationsClass.getMapIntoJsonString(Map.of(
                    "Bank/Slot Label", physicalMemory.getBankLabel(),
                    "Capacity", FormatUtil.formatBytes(physicalMemory.getCapacity()),
                    "Clock Speed", FormatUtil.formatHertz(physicalMemory.getClockSpeed()),
                    "Manufacturer", physicalMemory.getManufacturer(),
                    "Type", physicalMemory.getMemoryType(),
                    "Part Number", physicalMemory.getPartNumber().trim(),
                    STR_SRL_NUM, physicalMemory.getSerialNumber()
                )));
                intCounter++;
            }
            strJsonString.append("]}");
            return strJsonString.toString();
        }

        /**
         * Network type class
         */
        private static final class NetworkTypesClass {
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
             * Sensors Information
             * @param intPhysMedType number for NDIS Physical Medium Type
             * @return String
             */
            public static String getNetworkPhysicalMediumType(final int intPhysMedType) {
                return JsonOperationsClass.getMapIntoJsonString(
                        Map.of("Numeric", intPhysMedType,
                                OshiUsageClass.STR_NAME, MEDIUM_TYPES.getOrDefault(intPhysMedType, "Unknown"))
                );
            }
        }

    }

    /**
     * initiating OSHI package
     */
    public static class OshiUsageClass {
        /**
         * Hardware info
         */
        public static final SystemInfoFFM SYSTEM_INFO = new SystemInfoFFM();
        /**
         * standard String
         */
        public static final String STR_NAME = "Name";

        /**
         * Constructor empty
         */
        protected OshiUsageClass() {
            // no init required
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
             * get Video card attributes
             * @return List of GraphicsCard
             */
            public static List<GraphicsCard> getOshiGraphicsCards() {
                return getOshiHardware().getGraphicsCards();
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
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

package javajava;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import oshi.SystemInfo;
import oshi.hardware.Baseboard;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.hardware.Firmware;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.NetworkIF;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.software.os.NetworkParams;
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
        final CentralProcessor processor = OshiUsageClass.OshiHardware.getOshiProcessor();
        final CentralProcessor.ProcessorIdentifier procIdentif = OshiUsageClass.OshiHardware.getOshiProcessorIdentifier();
        final String strHardware = "\"Hardware\":{"
                + "\"CPU\":" + JsonOperationsClass.getMapIntoJsonString(Map.of(
                        "Feature Flags", processor.getFeatureFlags().toString().replace("[", "[\"").replace(", ", "\",\"").replace("]", "\"]"),
                        "Family", procIdentif.getFamily(),
                        "Identifier", procIdentif.getIdentifier(),
                        "Logical Processors", processor.getLogicalProcessorCount(),
                        BasicStructuresClass.STR_MODEL, procIdentif.getModel(),
                        BasicStructuresClass.STR_NAME, procIdentif.getName(),
                        "Physical Processors", processor.getPhysicalProcessorCount(),
                        "Maximum Frequency", FormatUtil.formatHertz(processor.getMaxFreq())))
                + "\"RAM\":" + EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory()
                + "\"GPU\":" + EnvironmentHardwareClass.getDetailsAboutGraphicCards()
                + "\"Monitor\":" + EnvironmentHardwareClass.getDetailsAboutMonitor()
                + "\"Network Interface\":" + EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces()
                + "\"Storage\":" + OshiUsageClass.getDetailsAboutAvailableStoragePartitions()
                + "\"System\"" + EnvironmentHardwareClass.getDetailsAboutComputerSystem()
                + "}";
        final String strFeedbackH = LocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackH);
        final OperatingSystem.OSVersionInfo version = OshiUsageClass.OshiSoftware.getOshiVersionInfo();
        final NetworkParams networkParams = OshiUsageClass.OshiSoftware.getOshiNetworkParameters();
        final String strSoftware = "\"Software\":{"
                + "\"OS\":" + JsonOperationsClass.getMapIntoJsonString(Map.of(
                        "Architecture", System.getProperty("os.arch"),
                        "Build", version.getBuildNumber(),
                        "Code", version.getCodeName(),
                        "Family", OshiUsageClass.OshiSoftware.getOshiFamily(),
                        BasicStructuresClass.STR_MANUFACTURER, OshiUsageClass.OshiSoftware.getOshiManufacturer(),
                        BasicStructuresClass.STR_NAME, System.getProperty("os.name"),
                        "Platform", SystemInfo.getCurrentPlatform().toString(),
                        BasicStructuresClass.STR_VERSION, version.getVersion()))
                + "\"Java\":" + JsonOperationsClass.getMapIntoJsonString(Map.of(
                        "Date", System.getProperty("java.version.date"),
                        "Release", System.getProperty("java.vendor.version"),
                        "Runtime", System.getProperty("java.runtime.name"),
                        BasicStructuresClass.STR_VERSION, System.getProperty("java.version"),
                        "Vendor", System.getProperty("java.vendor"),
                        "VM", System.getProperty("java.vm.name")))
                + "\"Network\":" + JsonOperationsClass.getMapIntoJsonString(Map.of(
                        "DNS Servers", String.join(", ", networkParams.getDnsServers()),
                        "Domain Name", networkParams.getDomainName(),
                        "Host Name", networkParams.getHostName(),
                        "IPv4 Gateway", networkParams.getIpv4DefaultGateway(),
                        "IPv6 Gateway", networkParams.getIpv6DefaultGateway()))
                + "\"User\":" + JsonOperationsClass.getMapIntoJsonString(Map.of(
                        "Country", System.getProperty("user.country"),
                        "Country.Format", System.getProperty("user.country.format"),
                        "Language", System.getProperty("user.language"),
                        "Language.Format", System.getProperty("user.language.format"),
                        "Home", System.getProperty("user.home").replace("\\", "\\\\"),
                        "Name", System.getProperty("user.name"),
                        "Timezone", System.getProperty("user.timezone")))
                + "}";
        final String strFeedbackS = LocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackS);
        final String strEnvironment = String.format("\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME"));
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
     * Capturing current environment hardware details
     */
    private static final class EnvironmentHardwareClass {
        /**
         * Display details
         *
         * @param crtDisplay current Display object
         * @return String
         */
        private static String digestSingleDisplayDetails(final Display crtDisplay) {
            final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
            final String[] arrayDetails = crtDisplay.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\n");
            for (final String crtLine : arrayDetails) {
                final String strSlimLine = crtLine.trim();
                if (strSlimLine.endsWith(" in") && strSlimLine.contains(" cm ")) {
                    final int intCmPos = strSlimLine.indexOf(" cm ");
                    arrayAttributes.put(BasicStructuresClass.STR_PHYSC_DIM + " [in]", strSlimLine.substring(0, intCmPos));
                    final int intInPos = strSlimLine.indexOf(" in");
                    arrayAttributes.put(BasicStructuresClass.STR_PHYSC_DIM + " [cm]", strSlimLine.substring(intCmPos + 4, intInPos));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_MONITOR_NAME)) {
                    arrayAttributes.put(BasicStructuresClass.STR_MONITOR_NAME, strSlimLine.replace(BasicStructuresClass.STR_MONITOR_NAME + " ", ""));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_PRFRD_TM_CLCK)) {
                    final int intClockLen = BasicStructuresClass.STR_PRFRD_TM_CLCK.length();
                    final int intPixelPos = strSlimLine.indexOf(BasicStructuresClass.STR_ACTV_PXLS);
                    arrayAttributes.put(BasicStructuresClass.STR_PRFRD_TM_CLCK, strSlimLine.substring(intClockLen, intPixelPos).trim());
                    arrayAttributes.put(BasicStructuresClass.STR_ACTV_PXLS, strSlimLine.substring(intPixelPos)
                        .replace(BasicStructuresClass.STR_ACTV_PXLS + " ", "").trim());
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_RANGE_LMTS)) {
                    arrayAttributes.put(BasicStructuresClass.STR_RANGE_LMTS, strSlimLine.replace(BasicStructuresClass.STR_RANGE_LMTS + " ", ""));
                }
                if (strSlimLine.startsWith(BasicStructuresClass.STR_SRL_NUM)) {
                    arrayAttributes.put(BasicStructuresClass.STR_SRL_NUM, strSlimLine.replace(BasicStructuresClass.STR_SRL_NUM + " ", ""));
                }
            }
            return JsonOperationsClass.getMapIntoJsonString(arrayAttributes);
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
                    BasicStructuresClass.STR_NAME, graphicCard.getName(),
                    "Vendor", graphicCard.getVendor(),
                    "VRAM", FormatUtil.formatBytes(graphicCard.getVRam()),
                    "Driver Version", graphicCard.getVersionInfo()
                )));
                intCounter++;
            }
            return String.format("[%s]", strJsonString);
        }

        /**
         * Computer System
         * 
         * @return details on computer system as JSON
         */
        public static String getDetailsAboutComputerSystem() {
            final StringBuilder strJsonString = new StringBuilder(50);
            final ComputerSystem computerSystem = OshiUsageClass.OshiHardware.getOshiComputerSystem();
            final Firmware firmware = OshiUsageClass.OshiHardware.getOshiFirmware();
            final String strFirmware = JsonOperationsClass.getMapIntoJsonString(Map.of(
                    BasicStructuresClass.STR_MANUFACTURER, firmware.getManufacturer(),
                    "Name", firmware.getName(),
                    "Description", firmware.getDescription(),
                    BasicStructuresClass.STR_VERSION, firmware.getVersion(),
                    "Release Date", firmware.getReleaseDate() == null ? "unknown" : firmware.getReleaseDate()));
            final Baseboard baseboard = OshiUsageClass.OshiHardware.getOshiMotherboard();
            final String strMotherBoard = JsonOperationsClass.getMapIntoJsonString(Map.of(
                    BasicStructuresClass.STR_MANUFACTURER, baseboard.getManufacturer(),
                    BasicStructuresClass.STR_MODEL, baseboard.getModel(),
                    BasicStructuresClass.STR_VERSION, baseboard.getVersion(),
                    BasicStructuresClass.STR_SRL_NUM, baseboard.getSerialNumber()));
            strJsonString.append(JsonOperationsClass.getMapIntoJsonString(Map.of(
                    BasicStructuresClass.STR_MANUFACTURER, computerSystem.getManufacturer(),
                    BasicStructuresClass.STR_MODEL, computerSystem.getModel(),
                    BasicStructuresClass.STR_SRL_NUM, computerSystem.getSerialNumber(),
                    "Firmware", strFirmware,
                    "Mainboard", strMotherBoard
            )));
            return strJsonString.toString();
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
                strJsonString.append(digestSingleDisplayDetails(crtDisplay));
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
                        BasicStructuresClass.STR_NAME, net.getName(),
                        "Display Name", net.getDisplayName(),
                        "IPv4", String.join(", ", net.getIPv4addr()),
                        "IPv6", String.join(", ", net.getIPv6addr()),
                        "MAC Address", net.getMacaddr(),
                        "MTU", net.getMTU(),
                        "Status", net.getIfOperStatus(),
                        "Speed", FormatUtil.formatBytes(net.getSpeed()),
                        "NDIS Physical Medium Type", OshiUsageClass.getNetworkPhysicalMediumType(net.getNdisPhysicalMediumType())
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
            final VirtualMemory virtualMemory = OshiUsageClass.OshiHardware.getOshiVirtualMemory();
            final StringBuilder strJsonString = new StringBuilder();
            strJsonString.append(String.format("{\"Total\":{\"Total\":\"%s\",\"Available\":\"%s\",\"Page Size\":\"%s\",\"Swap Used\":\"%s\",\"Swap Total\":\"%s\"}"
                , FormatUtil.formatBytes(globalMemory.getTotal())
                , FormatUtil.formatBytes(globalMemory.getAvailable())
                , FormatUtil.formatBytes(globalMemory.getPageSize())
                , FormatUtil.formatBytes(virtualMemory.getSwapUsed())
                , FormatUtil.formatBytes(virtualMemory.getSwapTotal())));
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
                    BasicStructuresClass.STR_MANUFACTURER, physicalMemory.getManufacturer(),
                    "Type", physicalMemory.getMemoryType(),
                    "Part Number", physicalMemory.getPartNumber().trim(),
                    BasicStructuresClass.STR_SRL_NUM, physicalMemory.getSerialNumber()
                )));
                intCounter++;
            }
            strJsonString.append("]}");
            return strJsonString.toString();
        }

    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}

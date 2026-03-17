package javajava;

import java.util.Base64;
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
 * Hardware class
 */
final public class HardwareClass {

    /**
     * Display details
     *
     * @param crtDisplay current Display object
     * @return String
     */
    private static Map<String, Object> digestSingleDisplayDetails(final Display crtDisplay) {
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
        return arrayAttributes;
    }

    /**
     * Environment details gathered
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutCentralProcessorUnit() {
        final CentralProcessor processor = OshiUsageClass.OshiHardware.getOshiProcessor();
        final CentralProcessor.ProcessorIdentifier procIdentif = OshiUsageClass.OshiHardware.getOshiProcessorIdentifier();
        final List<String> featureFlags = processor.getFeatureFlags().stream()
                .sorted()
                .toList();
        return Map.of(
                "CPU Identifier", procIdentif.getIdentifier(),
                "Family", procIdentif.getFamily(),
                "Feature Flags", featureFlags.toString().replace("[", "[\"").replace(", ", "\", \"").replace("]", "\"]"),
                "Logical Processors", processor.getLogicalProcessorCount(),
                "Maximum Frequency", FormatUtil.formatHertz(processor.getMaxFreq()),
                BasicStructuresClass.STR_MODEL, procIdentif.getModel(),
                BasicStructuresClass.STR_NAME, procIdentif.getName(),
                "Processor ID", procIdentif.getProcessorID(),
                "Physical Processors", processor.getPhysicalProcessorCount(),
                "Vendor", procIdentif.getVendor());
    }

    /**
     * GPU info
     *
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutGraphicCards() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final List<GraphicsCard> graphicCards = OshiUsageClass.OshiHardware.getOshiGraphicsCards();
        for (final GraphicsCard  graphicCard : graphicCards) {
            final String strIdentifier = "Video Card ID#" + BasicStructuresClass.StringTransformationClass.computeStringSignature(graphicCard.getName()) + " ";
            arrayAttributes.putAll(Map.of(
                    strIdentifier + BasicStructuresClass.STR_NAME, graphicCard.getName(),
                    strIdentifier + "Vendor", graphicCard.getVendor(),
                    strIdentifier + "VRAM", FormatUtil.formatBytes(graphicCard.getVRam()),
                    strIdentifier + "Driver Version", graphicCard.getVersionInfo()
            ));
        }
        return arrayAttributes;
    }

    /**
     * Mainboard details gathered
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutMainboard() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final Baseboard baseboard = OshiUsageClass.OshiHardware.getOshiMotherboard();
        arrayAttributes.putAll(Map.of(
                BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_MANUFACTURER, baseboard.getManufacturer(),
                BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_MODEL, baseboard.getModel(),
                BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_VERSION, baseboard.getVersion(),
                BasicStructuresClass.STR_MAINBOARD + " " + BasicStructuresClass.STR_SRL_NUM, baseboard.getSerialNumber()));
        final Firmware firmware = OshiUsageClass.OshiHardware.getOshiFirmware();
        arrayAttributes.putAll(Map.of(
                BasicStructuresClass.STR_FIRMWARE + " " + BasicStructuresClass.STR_MANUFACTURER, firmware.getManufacturer(),
                BasicStructuresClass.STR_FIRMWARE + " " + "Name", firmware.getName(),
                BasicStructuresClass.STR_FIRMWARE + " " + "Description", firmware.getDescription(),
                BasicStructuresClass.STR_FIRMWARE + " " + BasicStructuresClass.STR_VERSION, firmware.getVersion(),
                BasicStructuresClass.STR_FIRMWARE + " " + "Release Date", firmware.getReleaseDate() == null ? "unknown" : firmware.getReleaseDate()));
        final ComputerSystem computerSystem = OshiUsageClass.OshiHardware.getOshiComputerSystem();
        arrayAttributes.putAll(Map.of(
                BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_MANUFACTURER, computerSystem.getManufacturer(),
                BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_MODEL, computerSystem.getModel(),
                BasicStructuresClass.STR_SYSTEM + " " + BasicStructuresClass.STR_SRL_NUM, computerSystem.getSerialNumber()));
        return arrayAttributes;
    }

    /**
     * Monitors info as Map
     *
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutMonitor() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final List<Display> displays = OshiUsageClass.OshiHardware.getOshiMonitor();
        for (final Display crtDisplay : displays) {// The EDID is the "fingerprint" of the monitor hardware
            final byte[] edid = crtDisplay.getEdid();
            final String uniqueId = "Monitor #" + BasicStructuresClass.StringTransformationClass.computeStringSignature(Base64.getEncoder().encodeToString(edid));
            final Map<String, Object> crtMonitor = digestSingleDisplayDetails(crtDisplay);
            crtMonitor.forEach((strKey, strValue) -> {
                arrayAttributes.put(uniqueId + " " + strKey, strValue);
            });
        }
        return arrayAttributes;
    }

    /**
     * Network details gathered
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutNetwork() {
        final NetworkParams networkParams = OshiUsageClass.OshiSoftware.getOshiNetworkParameters();
        return Map.of(
                "DNS Servers", String.join(", ", networkParams.getDnsServers()),
                "Domain Name", networkParams.getDomainName(),
                "Host Name", networkParams.getHostName(),
                "IPv4 Gateway", networkParams.getIpv4DefaultGateway(),
                "IPv6 Gateway", networkParams.getIpv6DefaultGateway());
    }

    /**
     * Sensors Information
     *
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutNetworkInterfaces() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final List<NetworkIF> networkIFs = OshiUsageClass.OshiHardware.getOshiNetworkInterfaces();
        for (final NetworkIF net : networkIFs) {
            net.updateAttributes(); // Refresh interface stats
            final String strIdentifier = "Memory MAC#" + net.getMacaddr() + " ";
            arrayAttributes.putAll(Map.of(
                    strIdentifier + BasicStructuresClass.STR_NAME, net.getName(),
                    strIdentifier + "Display Name", net.getDisplayName(),
                    strIdentifier + "IPv4", String.join(", ", net.getIPv4addr()),
                    strIdentifier + "IPv6", String.join(", ", net.getIPv6addr()),
                    strIdentifier + "MTU", net.getMTU(),
                    strIdentifier + "NDIS Physical Medium Type", OshiUsageClass.getNetworkPhysicalMediumType(net.getNdisPhysicalMediumType()),
                    strIdentifier + "Status", net.getIfOperStatus(),
                    strIdentifier + "Speed", FormatUtil.formatBytes(net.getSpeed())));
        }
        return arrayAttributes;
    }

    /**
     * Operating System details gathered
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutOperatingSystem() {
        final OperatingSystem.OSVersionInfo version = OshiUsageClass.OshiSoftware.getOshiVersionInfo();
        return Map.of(
                "Architecture", System.getProperty("os.arch"),
                "Build", version.getBuildNumber(),
                "Code", version.getCodeName(),
                "Family", OshiUsageClass.OshiSoftware.getOshiFamily(),
                BasicStructuresClass.STR_MANUFACTURER, OshiUsageClass.OshiSoftware.getOshiManufacturer(),
                BasicStructuresClass.STR_NAME, System.getProperty("os.name"),
                "Platform", SystemInfo.getCurrentPlatform().toString(),
                BasicStructuresClass.STR_VERSION, version.getVersion());
    }

    /**
     * Capturing RAM information
     *
     * @return Map
     */
    public static Map<String, Object> getDetailsAboutRandomAccessMemory() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final GlobalMemory globalMemory = OshiUsageClass.OshiHardware.getOshiMemory();
        final VirtualMemory virtualMemory = OshiUsageClass.OshiHardware.getOshiVirtualMemory();
        arrayAttributes.putAll(Map.of(
                "Available", FormatUtil.formatBytes(globalMemory.getAvailable()),
                "Page Size", FormatUtil.formatBytes(globalMemory.getPageSize()),
                "Total", FormatUtil.formatBytes(globalMemory.getTotal()),
                "Virtual Memory Swap In Use", FormatUtil.formatBytes(virtualMemory.getVirtualInUse()),
                "Virtual Memory Swap Used", FormatUtil.formatBytes(virtualMemory.getSwapUsed()),
                "Virtual Memory Swap Total", FormatUtil.formatBytes(virtualMemory.getSwapTotal())));
        final List<PhysicalMemory> physicalMemories = globalMemory.getPhysicalMemory();
        for (final PhysicalMemory physicalMemory : physicalMemories) {
            final String strIdentifier = "Bank SN#" + physicalMemory.getSerialNumber() + " ";
            arrayAttributes.putAll(Map.of(
                    strIdentifier + "Bank/Slot Label", physicalMemory.getBankLabel(),
                    strIdentifier + "Capacity", FormatUtil.formatBytes(physicalMemory.getCapacity()),
                    strIdentifier + "Clock Speed", FormatUtil.formatHertz(physicalMemory.getClockSpeed()),
                    strIdentifier + BasicStructuresClass.STR_MANUFACTURER, physicalMemory.getManufacturer(),
                    strIdentifier + "Type", physicalMemory.getMemoryType(),
                    strIdentifier + "Part Number", physicalMemory.getPartNumber().trim()));
        }
        return arrayAttributes;
    }

    /**
     * Constructor
     */
    private HardwareClass() {
        // intentionally blank
    }

}

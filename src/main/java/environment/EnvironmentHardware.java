package environment;

import javajava.Common;
import javajava.StringManipulationClass;
import oshi.hardware.*;
import oshi.util.FormatUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capturing current environment hardware details
 */
public class EnvironmentHardware extends OshiUsage.OshiHardware {
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
    public static String digestDisplayDetails(final Display crtDisplay) {
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
        return StringManipulationClass.getMapIntoJsonString(arrayAttributes);
    }

    /**
     * Capturing details about CPU
     * 
     * @return String
     */
    public static String getDetailsAboutCentralPowerUnit() {
        final CentralProcessor processor = getOshiProcessor();
        final CentralProcessor.ProcessorIdentifier procIdentif = getOshiProcessorIdentifier();
        return StringManipulationClass.getMapIntoJsonString(Map.of(
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
    public static String getDetailsAboutGraphicCards() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final List<GraphicsCard> graphicCards = getOshiGraphicsCards();
        int intCounter = 0;
        for (final GraphicsCard  graphicCard : graphicCards) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(StringManipulationClass.getMapIntoJsonString(Map.of(
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
    public static String getDetailsAboutMonitor() {
        final StringBuilder strJsonString = new StringBuilder();
        final List<Display> displays = getOshiMonitor();
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
        final List<NetworkIF> networkIFs = getOshiNetworkInterfaces();
        final StringBuilder strJsonString = new StringBuilder();
        int intCounter = 0;
        strJsonString.append('[');
        for (final NetworkIF net : networkIFs) {
            net.updateAttributes(); // Refresh interface stats
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(StringManipulationClass.getMapIntoJsonString(Map.of(
                Common.STR_NAME, net.getName(),
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
        final GlobalMemory globalMemory = getOshiMemory();
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
            strJsonString.append(StringManipulationClass.getMapIntoJsonString(Map.of(
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
     * Constructor empty
     */
    protected EnvironmentHardware() {
        super();
    }

}

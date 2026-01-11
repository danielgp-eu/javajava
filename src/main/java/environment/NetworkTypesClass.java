package environment;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import json.JsoningClass;

/**
 * Network type class
 */
public final class NetworkTypesClass {
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
        return JsoningClass.getMapIntoJsonString(
                Map.of("Numeric", intPhysMedType,
                        OshiUsageClass.STR_NAME, MEDIUM_TYPES.getOrDefault(intPhysMedType, "Unknown"))
        );
    }

    /**
     * Constructor
     */
    private NetworkTypesClass() {
        // intentionally left blank
    }
}

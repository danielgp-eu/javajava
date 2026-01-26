package environment;

import java.util.List;

import oshi.SystemInfoFFM;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OperatingSystem;

/**
 * initiating OSHI package
 */
public class OshiUsageClass {
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
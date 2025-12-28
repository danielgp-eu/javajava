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
public class OshiUsage {
    /**
     * Hardware info
     */
    public static final SystemInfoFFM SYSTEM_INFO = new SystemInfoFFM();

    /**
     * initiating Hardware package
     */
    public static class OshiHardware {
        /**
         * Hardware info
         */
        private static final HardwareAbstractionLayer oshi_harware = SYSTEM_INFO.getHardware();

        /**
         * get Video card attributes
         * @return List of GraphicsCard
         */
        public static List<GraphicsCard> getOshiGraphicsCards() {
            return oshi_harware.getGraphicsCards();
        }

        /**
         * get RAM attributes
         * @return GlobalMemory
         */
        public static GlobalMemory getOshiMemory() {
            return oshi_harware.getMemory();
        }

        /**
         * get Video card attributes
         * @return List of Display
         */
        public static List<Display> getOshiMonitor() {
            return oshi_harware.getDisplays();
        }

        /**
         * get Video card attributes
         * @return List of NetworkIF
         */
        public static List<NetworkIF> getOshiNetworkInterfaces() {
            return oshi_harware.getNetworkIFs().stream()
                    .filter(net -> net.getIfOperStatus() == NetworkIF.IfOperStatus.UP)
                    .toList();
        }

        /**
         * get CPU attributes
         * @return CentralProcessor
         */
        public static CentralProcessor getOshiProcessor() {
            return oshi_harware.getProcessor();
        }

        /**
         * get CPU identifier
         * @return CentralProcessor
         */
        public static CentralProcessor.ProcessorIdentifier getOshiProcessorIdentifier() {
            return getOshiProcessor().getProcessorIdentifier();
        }
    }

    /**
     * initiating Software package
     */
    public static class OshiSoftware {
        /**
         * OS info
         */
        private static final OperatingSystem oshi_os = SYSTEM_INFO.getOperatingSystem();

        /**
         * get OS Family
         * @return OperatingSystem Family
         */
        public static String getOshiFamily() {
            return oshi_os.getFamily();
        }

        /**
         * get File System attributes
         * @return FileSystem
         */
        public static FileSystem getOshiFileSystem() {
            return oshi_os.getFileSystem();
        }

        /**
         * get OS Manufacturer
         * @return OperatingSystem Manufacturer
         */
        public static String getOshiManufacturer() {
            return oshi_os.getManufacturer();
        }

        /**
         * get Version information
         * @return OperatingSystem.OSVersionInfo
         */
        public static OperatingSystem.OSVersionInfo getOshiVersionInfo() {
            return oshi_os.getVersionInfo();
        }

    }
}
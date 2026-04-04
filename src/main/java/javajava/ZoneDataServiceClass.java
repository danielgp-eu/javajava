package javajava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Time Zones and associated coordinates handler
 */
public final class ZoneDataServiceClass {
    /**
     * Number of elements where coordinates are present
     */
    private static final int LINE_W_COORDINATE = 3;
    /**
     * Session Config handle
     */
    private static final List<String> SUPPORTED_TZ = new java.util.ArrayList<>(List.of("America/Los_Angeles", "America/Phoenix", "America/Denver", "America/Chicago", "America/New_York", "Europe/Dublin", "Europe/London", "Europe/Prague", "Europe/Berlin", "Europe/Bucharest", "Asia/Kolkata", "Asia/Shanghai", "Asia/Tokyo", "Australia/Melbourne"));
    /**
     * Cached zones
     */
    private static final Map<String, ZoneInfo> CACHE = new ConcurrentHashMap<>();

    static {
        loadIanaZones();
    }

    /**
     * IANA zone logic
     */
    private static void loadIanaZones() {
        final String propertyFileName = "/data/zone1970.tab";
        try (InputStream inputStream = ZoneDataServiceClass.class.getResourceAsStream(propertyFileName);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bReader = new BufferedReader(inputStreamReader)) {
            bReader.lines()
                .filter(line -> !line.startsWith("#") && !line.isBlank())
                .forEach(line -> {
                    final String[] parts = line.split("\t");
                    if (parts.length >= LINE_W_COORDINATE) {
                        processLine(parts[0], parts[1], parts[2]);
                    }
                });
        } catch (IOException ei) {
            final Path ptPrjProps = Path.of(propertyFileName);
            final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, ptPrjProps.getParent(), ptPrjProps.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Populates a time-zone list sorted in chronological order
     * @return SequencedMap
     */
    public static SequencedMap<String, String> loadSupportedTimeZones() {
        final Collection<ZoneInfo> allTimeZones = getAll();
        // ensure current user time-zone is also populated
        final String crtUserTimeZone = System.getProperty("user.timezone");
        final String strFeedback = String.format("Your time zone is %s", crtUserTimeZone);
        LogExposureClass.LOGGER.info(strFeedback);
        if (crtUserTimeZone != null
                && !SUPPORTED_TZ.contains(crtUserTimeZone)
                && allTimeZones.stream().anyMatch(z -> z.zoneId().equals(crtUserTimeZone))) {
            SUPPORTED_TZ.add(crtUserTimeZone);
        }
        final Map<String, String> mapBeforeUtc = new ConcurrentHashMap<>();
        final Map<String, String> mapAfterUtc = new ConcurrentHashMap<>();
        for (final String crtTimeZone : SUPPORTED_TZ) {
            final String friendlyTimeZone = TimingClass.getFriendlyOffset(crtTimeZone);
            if (friendlyTimeZone.startsWith("UTC-")) {
                mapBeforeUtc.put(crtTimeZone, friendlyTimeZone + " " + crtTimeZone);
            }
            if (friendlyTimeZone.startsWith("UTC+")) {
                mapAfterUtc.put(crtTimeZone, friendlyTimeZone + " " + crtTimeZone);
            }
        }
        // building final TimeZone list
        final SequencedMap<String, String> sortedTimeZones = mapBeforeUtc.entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, _) -> oldValue,
                        LinkedHashMap::new // preserve sorted order
                ));
        final SequencedMap<String, String> sortedAfterUtc = mapAfterUtc.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, _) -> oldValue,
                        LinkedHashMap::new // preserve sorted order
                ));
        sortedTimeZones.putAll(sortedAfterUtc);
        return sortedTimeZones;
    }

    /**
     * Coordinates parser
     * @param countries countries list as single string separated by comma
     * @param coords coordinates raw
     * @param zoneId IANA zone identifier
     */
    private static void processLine(final String countries, final String coords, final String zoneId) {
        // Parse Countries (Java 19 Locale.of)
        final List<String> codes = Arrays.asList(countries.split(","));
        final List<String> names = codes.stream()
                .map(code -> Locale.of("", code).getDisplayCountry(Locale.ENGLISH))
                .toList();
        // 2. Parse Coordinates (ISO 6709)
        int splitIdx = coords.indexOf('-', 1);
        if (splitIdx == -1) {
            splitIdx = coords.indexOf('+', 1);
        }
        final double lat = RegularExpressionsClass.dmsToDecimal(coords.substring(0, splitIdx), false);
        final double lon = RegularExpressionsClass.dmsToDecimal(coords.substring(splitIdx), true);
        // 3. Get Current Offset String
        final String offsetStr = "UTC" + ZonedDateTime.now(ZoneId.of(zoneId)).getOffset().getId();
        CACHE.put(zoneId, new ZoneInfo(zoneId, lat, lon, codes, names, offsetStr));
    }

    /**
     * Getter for ZoneInfo
     * @param zoneId string with IANA location
     * @return ZoneInfo
     */
    public static ZoneInfo get(final String zoneId) {
        return CACHE.get(zoneId);
    }

    /**
     * Getter for ZoneInfo
     * @return Collection of ZoneInfo
     */
    public static Collection<ZoneInfo> getAll() {
        return CACHE.values();
    }

    /**
     * Constructor
     */
    private ZoneDataServiceClass() {
        // intentionally blank
    }

}

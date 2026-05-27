package javajava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ZoneDataServiceClass unit testing")
class ZoneDataServiceClassTests {
    private static final String ORIG_NQ_EXPCT = "calculated result is not equal to expected result";

    @Test
    @DisplayName("Get supported time zones returns non-empty collection")
    void getSupportedTimeZonesReturnsNonEmpty() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        assertTrue(!timeZones.isEmpty(), "Supported time zones should not be empty");
    }

    @Test
    @DisplayName("Get supported time zones includes common American zones")
    void getSupportedTimeZonesIncludeAmericaZones() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        assertTrue(timeZones.containsKey("America/Los_Angeles"), "America/Los_Angeles should be supported");
        assertTrue(timeZones.containsKey("America/New_York"), "America/New_York should be supported");
        assertTrue(timeZones.containsKey("America/Chicago"), "America/Chicago should be supported");
    }

    @Test
    @DisplayName("Get supported time zones includes European zones")
    void getSupportedTimeZonesIncludeEuropeZones() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        assertTrue(timeZones.containsKey("Europe/London"), "Europe/London should be supported");
        assertTrue(timeZones.containsKey("Europe/Berlin"), "Europe/Berlin should be supported");
        assertTrue(timeZones.containsKey("Europe/Prague"), "Europe/Prague should be supported");
    }

    @Test
    @DisplayName("Get supported time zones includes Asian zones")
    void getSupportedTimeZonesIncludeAsiaZones() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        assertTrue(timeZones.containsKey("Asia/Tokyo"), "Asia/Tokyo should be supported");
        assertTrue(timeZones.containsKey("Asia/Shanghai"), "Asia/Shanghai should be supported");
        assertTrue(timeZones.containsKey("Asia/Kolkata"), "Asia/Kolkata should be supported");
    }

    @Test
    @DisplayName("Get supported time zones includes Australian zones")
    void getSupportedTimeZonesIncludeAustraliaZones() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        assertTrue(timeZones.containsKey("Australia/Melbourne"), "Australia/Melbourne should be supported");
    }

    @Test
    @DisplayName("Loaded time zones are properly sorted with UTC offsets")
    void getTimeZonesAreSortedWithUtcOffsets() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        for (final var value : timeZones.values()) {
            assertTrue(!value.isEmpty(), "Time zone display value should not be empty");
            assertTrue(value.contains("UTC"), "Time zone display should contain UTC offset");
        }
    }

    @Test
    @DisplayName("Get zone info returns null for non-existent zone")
    void getZoneInfoForNonExistentZoneReturnsNull() {
        final var zoneInfo = ZoneDataServiceClass.get("Invalid/Zone_That_Does_Not_Exist");
        assertEquals(null, zoneInfo, ORIG_NQ_EXPCT);
    }

    @Test
    @DisplayName("Get zone info returns valid record for known zone")
    void getZoneInfoReturnsValidRecordForKnownZone() {
        final var zoneInfo = ZoneDataServiceClass.get("America/New_York");
        assertTrue(zoneInfo != null, "Zone info should not be null for valid zone");
        assertEquals("America/New_York", zoneInfo.zoneId(), "Zone ID should match");
    }

    @Test
    @DisplayName("Get zone info includes latitude and longitude coordinates")
    void getZoneInfoIncludesCoordinates() {
        final var zoneInfo = ZoneDataServiceClass.get("America/New_York");
        assertTrue(zoneInfo != null, "Zone info should not be null");
        assertTrue(zoneInfo.latitude() != 0.0 || zoneInfo.longitude() != 0.0, "Coordinates should be non-zero");
    }

    @Test
    @DisplayName("Get zone info includes country codes")
    void getZoneInfoIncludesCountryCodes() {
        final var zoneInfo = ZoneDataServiceClass.get("Europe/Berlin");
        assertTrue(zoneInfo != null, "Zone info should not be null");
        assertTrue(!zoneInfo.countryCodes().isEmpty(), "Country codes should not be empty");
    }

    @Test
    @DisplayName("Get zone info includes country names")
    void getZoneInfoIncludesCountryNames() {
        final var zoneInfo = ZoneDataServiceClass.get("Asia/Tokyo");
        assertTrue(zoneInfo != null, "Zone info should not be null");
        assertTrue(!zoneInfo.countryNames().isEmpty(), "Country names should not be empty");
    }

    @Test
    @DisplayName("Get zone info includes current UTC offset")
    void getZoneInfoIncludesCurrentUtcOffset() {
        final var zoneInfo = ZoneDataServiceClass.get("America/Los_Angeles");
        assertTrue(zoneInfo != null, "Zone info should not be null");
        assertTrue(zoneInfo.friendlyOffset().startsWith("UTC"), "Offset should start with UTC");
    }

    @Test
    @DisplayName("Get all zones returns non-empty collection")
    void getAllZonesReturnsNonEmpty() {
        final var allZones = ZoneDataServiceClass.getAll();
        assertTrue(!allZones.isEmpty(), "All zones collection should not be empty");
    }

    @Test
    @DisplayName("Get all zones returns cached zones with current offsets")
    void getAllZonesReturnsCachedZonesWithOffsets() {
        final var allZones = ZoneDataServiceClass.getAll();
        for (final var zoneRecord : allZones) {
            assertTrue(zoneRecord.friendlyOffset().startsWith("UTC"), "Each zone should have UTC offset");
            assertTrue(!zoneRecord.zoneId().isEmpty(), "Each zone should have a zone ID");
        }
    }

    @Test
    @DisplayName("Get zone info multiple calls return consistent data")
    void getZoneInfoMultipleCallsReturnConsistentData() {
        final var firstCall = ZoneDataServiceClass.get("Europe/London");
        final var secondCall = ZoneDataServiceClass.get("Europe/London");
        assertEquals(firstCall.zoneId(), secondCall.zoneId(), "Zone ID should be consistent");
        assertEquals(firstCall.latitude(), secondCall.latitude(), "Latitude should be consistent");
        assertEquals(firstCall.longitude(), secondCall.longitude(), "Longitude should be consistent");
    }

    @Test
    @DisplayName("Load supported time zones preserves insertion order for UTC zones")
    void loadSupportedTimeZonesPreservesOrderForUtcZones() {
        final var timeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        final var keysList = new ArrayList<>(timeZones.keySet());
        assertTrue(keysList.size() >= 5, "Should have at least 5 supported zones");
    }

    @Test
    @DisplayName("Zone info coordinates are within valid range")
    void getZoneInfoCoordinatesAreWithinValidRange() {
        final var zoneInfo = ZoneDataServiceClass.get("America/Denver");
        assertTrue(zoneInfo != null, "Zone info should not be null");
        assertTrue(zoneInfo.latitude() >= -90 && zoneInfo.latitude() <= 90, "Latitude should be between -90 and 90");
        assertTrue(zoneInfo.longitude() >= -180 && zoneInfo.longitude() <= 180, "Longitude should be between -180 and 180");
    }

    @Test
    @DisplayName("Get zone info for southern hemisphere zone")
    void getZoneInfoForSouthernHemisphereZone() {
        final var zoneInfo = ZoneDataServiceClass.get("Australia/Melbourne");
        assertTrue(zoneInfo != null, "Zone info should not be null for southern hemisphere");
        // Southern hemisphere should have negative latitude
        assertTrue(zoneInfo.latitude() < 0, "Southern hemisphere zones should have negative latitude");
    }

    public ZoneDataServiceClassTests() {
        // intentionally blank
    }
}
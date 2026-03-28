package javajava;

import java.util.List;

public record ZoneInfo(
        String zoneId,
        double latitude,
        double longitude,
        List<String> countryCodes,
        List<String> countryNames,
        String friendlyOffset) {}

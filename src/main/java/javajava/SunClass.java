package javajava;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static java.lang.Math.*;

/**
 * Sun position class
 */
public final class SunClass {
    /**
     * Zenith for official sunrise/sunset (90° 50')
     */
    private static final double ZENITH = 90.833;
    /**
     * Latitude variable
     */
    private static double dblLatitude;
    /**
     * Longitude variable
     */
    private static double dblLongitude;
    /**
     * ZoneId variable
     */
    private static ZoneId internalZoneId;
    /**
     * Zone Name variable
     */
    private static String internalZoneName;

    /**
     * Calculates SunRize and SunSet for a given location
     * @param inLatitude
     * @param inLongitude
     * @param inZoneName
     * @return Properties
     */
    public static Properties getSunRiseAndSet(final String crtLocationDetail) {
        final String[] arrayLocationPieces = crtLocationDetail.split(",");
        final Properties outProperties = new Properties();
        final ZonedDateTime nowZ = ZonedDateTime.now(internalZoneId);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        outProperties.put("Location", arrayLocationPieces[0]);
        outProperties.put("Country Name", arrayLocationPieces[3]);
        outProperties.put("Division Name", arrayLocationPieces[2]);
        outProperties.put("Place Name", arrayLocationPieces[1]);
        outProperties.put("Zone Name", internalZoneName);
        outProperties.put("Local Timestamp", nowZ.format(formatter));
        final LocalDate dateOnly = nowZ.toLocalDate();
        final ZonedDateTime sunrise = calculateSunSetOrRise(dateOnly, true);
        final DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss z");
        outProperties.put("Sunrise", sunrise.format(formatterTime));
        final ZonedDateTime sunset = calculateSunSetOrRise(dateOnly, false);
        outProperties.put("Sunset", sunset.format(formatterTime));
        final Duration objDurationSun = Duration.between(sunrise, sunset);
        outProperties.put("Daylight time", String.format("%s aka. %s", objDurationSun, TimingClass.convertNanosecondsIntoSomething(objDurationSun, "HumanReadableTime")));
        if (nowZ.isBefore(sunrise)) {
            outProperties.put("Sun situation", "DOWN");
            outProperties.put("Current Situation", "Before sunrize");
            final Duration objDuration = Duration.between(nowZ, sunrise);
            outProperties.put("Time until next event", String.format("%s aka. %s", objDuration, TimingClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")));
        } else if (nowZ.isBefore(sunset)) {
            outProperties.put("Sun situation", "UP");
            outProperties.put("Current Situation", "In between sunrize and sunset");
            final Duration objDuration = Duration.between(nowZ, sunset);
            outProperties.put("Time until next event", String.format("%s aka. %s", objDuration, TimingClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")));
        } else {
            outProperties.put("Sun situation", "DOWN");
            outProperties.put("Current Situation", "After sunset");
            final Duration objDuration = Duration.between(sunset, nowZ);
            outProperties.put("Time since last event", String.format("%s aka. %s", objDuration, TimingClass.convertNanosecondsIntoSomething(objDuration, "HumanReadableTime")));
        }
        return outProperties;
    }

    /**
     * Calculate Sunrise/Sunset
     * @param inLocalDate input date
     * @param isSunrise boolean if Sunrise 
     * @return ZonedDateTime
     */
    private static ZonedDateTime calculateSunSetOrRise(final LocalDate inLocalDate, final boolean isSunrise) {
        final int dayOfYear = inLocalDate.getDayOfYear();
        // 1. Convert longitude to hour value and estimate time
        final double lonHour = dblLongitude / 15.0;
        final double estimatedTime = dayOfYear + ((isSunrise ? 6.0 : 18.0) - lonHour) / 24.0;
        // 2. Sun's mean anomaly
        final double sunMeanAnomaly = (0.9856 * estimatedTime) - 3.289;
        // 3. Sun's true longitude
        double sunLongitude = sunMeanAnomaly
                + (1.916 * sin(toRadians(sunMeanAnomaly)))
                + (0.020 * sin(toRadians(2 * sunMeanAnomaly)))
                + 282.634;
        sunLongitude = (sunLongitude + 360) % 360;
        // 4. Sun's right ascension
        double sunRightAscension = toDegrees(atan(0.917_64 * tan(toRadians(sunLongitude))));
        sunRightAscension = (sunRightAscension + 360) % 360;
        // Adjust quadrant of sunRightAscension
        final double lQuadrant = floor(sunLongitude / 90) * 90;
        final double raQuadrant = floor(sunRightAscension / 90) * 90;
        sunRightAscension = (sunRightAscension + (lQuadrant - raQuadrant)) / 15.0;
        // 5. Sun's declination
        final double sinDec = 0.397_82 * sin(toRadians(sunLongitude));
        final double cosDec = cos(asin(sinDec));
        // 6. Local hour angle
        final double cosH = (cos(toRadians(ZENITH))
                - (sinDec * sin(toRadians(dblLatitude)))) / (cosDec * cos(toRadians(dblLatitude)));
        ZonedDateTime outZonedDateTime = null;
        if ((cosH >= -1) || (cosH <= 1)) { // only if Sun rises/sets
            // 7. Local mean time
            final double localMeanHour = (isSunrise ? (360 - toDegrees(acos(cosH))) : toDegrees(acos(cosH))) / 15.0;
            final double localMeanTime = localMeanHour + sunRightAscension - (0.065_71 * estimatedTime) - 6.622;
            // 8. UTC time
            final double utcTime = (localMeanTime - lonHour + 24) % 24;
            // 9. Convert to ZonedDateTime
            final LocalTime finalTime = LocalTime.ofNanoOfDay((long)(utcTime * 3_600_000_000_000L));
            outZonedDateTime = ZonedDateTime.of(inLocalDate, finalTime, ZoneOffset.UTC).withZoneSameInstant(internalZoneId);
        }
        return outZonedDateTime;
    }

    /**
     * Setter for dblLatitude
     * @param inLatitude
     */
    public static void setLatitude(final double inLatitude) {
        dblLatitude = inLatitude;
    }

    /**
     * Setter for dblLongitude
     * @param inLongitude
     */
    public static void setLongitude(final double inLongitude) {
        dblLongitude = inLongitude;
    }

    /**
     * Setter for strZoneName
     * @param inZoneName
     */
    public static void setZoneId(final String inZoneName) {
        try {
            internalZoneName = inZoneName;
            // Pre-cache available IDs for high-performance lookup
            // private static final Set<String> AVAILABLE_IDS = ZoneId.getAvailableZoneIds();
            final ZoneId zoneId = ZoneId.of(inZoneName);
            final String strFeedback = String.format("Given zone name %s has the corresponding ZoneId %s", inZoneName, zoneId);
            LogExposureClass.LOGGER.debug(strFeedback);
            internalZoneId = zoneId;
        } catch (DateTimeException e) {
            final String strFeedback = String.format("Given zone name %s does not seem to be a valid one...", inZoneName);
            LogExposureClass.LOGGER.debug(strFeedback);
        }
    }

    private SunClass() {
        // intentionally blank
    }

}

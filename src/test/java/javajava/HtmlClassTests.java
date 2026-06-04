package javajava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

/**
 * HtmlClass tests
 */
class HtmlClassTests {

    @Test
    @DisplayName("buildGeographicalCoordinatesFromTimeZone returns coordinates for known zone")
    void buildGeographicalCoordinatesKnownZoneReturnsCoordinates() {
        final String coords = HtmlClass.buildGeographicalCoordinatesFromTimeZone("Europe/London");
        assertAll("Coordinates for known zone",
                () -> assertNotNull(coords, "Coordinates should not be null"),
                () -> assertNotEquals("0,0", coords, "Known zone should not return 0,0"),
                () -> assertTrue(coords.contains(","), "Coordinates should contain comma-separated latitude and longitude")
        );
    }

    @Test
    @DisplayName("buildGeographicalCoordinatesFromTimeZone returns 0,0 for unknown zone")
    void buildGeographicalCoordinatesUnknownZoneReturnsZeroZero() {
        final String coords = HtmlClass.buildGeographicalCoordinatesFromTimeZone("Invalid/NonExistent_Zone");
        assertEquals("0,0", coords, "Unknown time zone should return default 0,0");
    }

    @Test
    @DisplayName("SelectInputSubClass.buildSelectInput produces select with selected option and correct attributes")
    void buildSelectInputGeneratesSelectWithSelectedOption() {
        final java.util.SequencedMap<String, String> mapValues = new java.util.LinkedHashMap<>();
        mapValues.put("tz1", "Timezone One");
        mapValues.put("tz2", "Timezone Two");
        final java.util.Properties props = new java.util.Properties();
        props.put("Label", "Time Zones");
        props.put("Name", "TZ");
        props.put("Id", "TZ");
        props.put("Default", "tz1");
        props.put("Size", "1");
        final String html = HtmlClass.SelectInputSubClass.buildSelectInput(mapValues, props);
        assertAll("Select HTML correctness",
                () -> assertTrue(html.contains("<label for=\"TZ"), "Label should exist point to actual select id"),
                () -> assertTrue(html.contains("<select"), "HTML should contain select tag"),
                () -> assertTrue(html.contains("id=\"TZ\""), "Select should contain provided id"),
                () -> assertTrue(html.contains("value=\"tz1\"") && html.contains(">Timezone One<"), "Option for tz1 should be present"),
                () -> assertTrue(html.contains("selected"), "Default option should be marked selected")
        );
    }

    @Test
    @DisplayName("HtmlClass.buildFileInfoBox produces div with time zones")
    void buildFileInfoBox() {
        final Path fileName = Path.of("/pom.xml");
        final String myInfoBox = HtmlClass.buildFileInfoBox(fileName);
        assertAll("Select HTML correctness",
                () -> assertTrue(myInfoBox.contains("<div class=\"infoBox\""), "HTML should contain div tag with infoBox class"),
                () -> assertTrue(myInfoBox.contains("bytes, last modified time-stamp on"), "Select contains final sequence text pieces")
        );
    }

    @Test
    @DisplayName("HtmlClass.buildMenuString produces select with time zones")
    void buildMenuString() {
        final java.util.SequencedMap<String, java.util.Map<String, String>> inMapMenu = JavaJavaWebClass.getMenu();
        final String myMenu = HtmlClass.buildMenuString(inMapMenu);
        assertAll("Select HTML correctness",
                () -> assertTrue(myMenu.contains("<li>"), "HTML should contain li tag"),
                () -> assertTrue(myMenu.contains("<a href=\"?page=home\""), "Select should contain provided id")
        );
    }

    @Test
    @DisplayName("HtmlClass.buildTimeZoneSelect produces select with time zones")
    void buildTimeZoneSelect() {
        final String strHtml = HtmlClass.buildTimeZoneSelect("Europe/Bucharest");
        assertAll("Select HTML correctness from " + strHtml,
                () -> assertTrue(strHtml.contains("<select"), "HTML should contain select tag"),
                () -> assertTrue(strHtml.contains("id=\"TZ\""), "Select should contain provided id")
        );
    }

    @Test
    @DisplayName("TableSubClass.getListOfSequencedMapIntoHtmlTable generates table rows, headers and tabs when New Tab feature is used")
    void testGetListOfSequencedMapIntoHtmlTableProducesTableAndTabs() {
        final java.util.SequencedMap<Object, Object> rec1 = new java.util.LinkedHashMap<>();
        rec1.put("Category", "A");
        rec1.put("Name", "Item1");
        rec1.put("Value", "10");
        final java.util.SequencedMap<Object, Object> rec2 = new java.util.LinkedHashMap<>();
        rec2.put("Category", "B");
        rec2.put("Name", "Item2");
        rec2.put("Value", "");
        final java.util.List<java.util.SequencedMap<Object, Object>> records = java.util.List.of(rec1, rec2);
        final java.util.Properties features = new java.util.Properties();
        features.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        features.put("Counter", "1");
        final String html = HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(records, features);
        assertAll("HTML table with tabs and counter",
                () -> assertTrue(html.contains("<table"), "Output should contain table markup"),
                () -> assertTrue(html.contains("<tr>"), "Output should contain table rows"),
                () -> assertTrue(html.contains("tabber"), "Output should include tab container when New Tab feature is used"),
                () -> assertTrue(html.contains("<th>#</th>"), "Counter column header should be present when Counter feature is used"),
                () -> assertTrue(html.contains("</tbody></table>"), "Table should be properly closed")
        );
    }

    /**
     * Constructor
     */
    public HtmlClassTests() {
        // intentionally blank
    }

}

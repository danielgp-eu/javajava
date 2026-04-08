package javajava;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.Session;
import io.undertow.util.Headers;
import io.undertow.util.Sessions;
import io.undertow.util.StatusCodes;

/**
 * Web interface class
 */
public final class JavaJavaWebClass {
    /**
     * Menu
     */
    private static final SequencedMap<String, Map<String, String>> mapMenu = Stream.of(
            Map.entry("home", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "Home",
                    BasicStructuresClass.STR_TITLE, "Home Page")),
            Map.entry("SoftwareReleases", Map.of(BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, "Releases",
                    BasicStructuresClass.STR_TITLE, "Software Releases")),
            Map.entry(BasicStructuresClass.STR_TS, Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-square-poll-horizontal",
                    BasicStructuresClass.STR_MENU, "TStatistics",
                    BasicStructuresClass.STR_TITLE, "Table Statistics")),
            Map.entry("FilesHashing", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-hashtag",
                    BasicStructuresClass.STR_MENU, "Hashing",
                    BasicStructuresClass.STR_TITLE, "Downloads File Hashing")),
            Map.entry("EnvironmentDetails", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-computer",
                    BasicStructuresClass.STR_MENU, "Environment",
                    BasicStructuresClass.STR_TITLE, "Environment Details"))
    ).collect(
            Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (v1, _) -> v1, 
                    LinkedHashMap::new  // Ensures it returns a SequencedMap
            ));

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    private static String getFileHashingAsHtmlTable() {
        final String[] inAlgorithms = {"SHA-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        final List<Properties> crtFileStatistics = FileStatisticsClass.getFileStatisticsIntoMap("C:/www/Downloads/");
        final List<String> desiredOrder = List.of("Folder", "File", "Size [bytes]", "Last Modified Time", "SHA-256");
        final List<SequencedMap<Object, Object>> orderedList = crtFileStatistics.stream()
                .map(prop -> BasicStructuresClass.ListAndMapClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.Table.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());  
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    private static String getSoftwareReleasesIntoHtml() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Profile");
        final List<Properties> softwareReleases = SoftwareReleasesClass.consolidateSoftwareReleases();
        final List<String> desiredOrder = List.of("Organization", "Product", "Version", "Date", "Files");
        final List<SequencedMap<Object, Object>> orderedList = softwareReleases.stream()
                .map(prop -> BasicStructuresClass.ListAndMapClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.Table.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Handle web content
     * @return PathHandler web content
     */
    public static HttpHandler handleWebContent() {
        return exchange -> {
            // initialize session
            Session session = Sessions.getSession(exchange);
            // Create a new session if one doesn't exist
            if (session == null) {
                session = Sessions.getOrCreateSession(exchange);
            }
            // Get the 'page' query parameter (Deques are used for multi-value parameters)
            final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
            final Deque<String> pageParams = queryParams.get("page");
            final String page = (pageParams != null) ? pageParams.getFirst() : "home";
            final String strTitle = BasicStructuresClass.STR_LOCALIZATION.equalsIgnoreCase(page) ? page : mapMenu.get(page).get(BasicStructuresClass.STR_TITLE);
            UndertowClass.handleTimeZoneSession(queryParams, session);
            if (queryParams.get("redirectAction") != null) {
                exchange.setStatusCode(StatusCodes.SEE_OTHER); // 303 Redirect
                exchange.getResponseHeaders().put(Headers.LOCATION, "/?"
                        + queryParams.get("redirectAction").getFirst());
                exchange.endExchange();
            }
            final String sessionTimeZone = session.getAttribute("TZ").toString();
            HtmlClass.Table.setTimeZone(sessionTimeZone);
            switch(page) {
                case "FilesHashing":
                    // intentionally blank
                case "SoftwareReleases":
                    // intentionally blank
                case BasicStructuresClass.STR_TS:
                    TimingClass.Localization.setInputTimeZone("UTC");
                    break;
                default:
                    // intentionally blank
                    break;
            }
            HtmlClass.Table.setTimeZone(sessionTimeZone);
            final gg.jte.Content bodyContent = output -> output.writeContent(switch(page) {
                case "EnvironmentDetails"        -> HtmlClass.getEnvironmentDetailsAsHtmlTable();
                case "FilesHashing"              -> getFileHashingAsHtmlTable();
                case "SoftwareReleases"          -> getSoftwareReleasesIntoHtml();
                case BasicStructuresClass.STR_TS -> HtmlClass.getTableStatisticsAsHtmlTable();
                default                          -> String.format("Welcome %s", System.getProperty("user.name"));
            });
            final TemplateEngine templateEngine = UndertowClass.createTemplateEngine();
            final Utf8ByteOutput output = new Utf8ByteOutput();
            UndertowClass.TemplateRendering.setOutput(output);
            UndertowClass.TemplateRendering.setServerExchange(exchange);
            UndertowClass.TemplateRendering.packParameter("page", page);
            UndertowClass.TemplateRendering.packParameter("title", strTitle);
            UndertowClass.TemplateRendering.packParameter("menu", HtmlClass.CommonWebElements.buildMenu(mapMenu));
            UndertowClass.TemplateRendering.packParameter("content", bodyContent);
            UndertowClass.TemplateRendering.packParameter("timeZoneSelect", HtmlClass.CommonWebElements.buildTimeZoneSelect(sessionTimeZone));
            UndertowClass.TemplateRendering.packParameter("currentPageQuery", exchange.getQueryString());
            UndertowClass.TemplateRendering.packParameter("geoCoordinates", HtmlClass.CommonWebElements.buildGeographicalCoordinatesFromTimeZone(sessionTimeZone));
            UndertowClass.TemplateRendering.packParameter("timeNow", HtmlClass.CommonWebElements.buildCurrentTimestamp(sessionTimeZone));
            UndertowClass.TemplateRendering.renderTemplate(templateEngine, "index.jte");
        };
    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

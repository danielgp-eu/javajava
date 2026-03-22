package javajava;

import java.util.*;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.undertow.server.HttpHandler;

/**
 * Web interface class
 */
public final class JavaJavaWebClass {
    /**
     * Menu
     */
    private static final Map<String, Map<String, String>> mapMenu = Map.of(
            "home", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "Home",
                    BasicStructuresClass.STR_TITLE, "Home Page"),
            "SoftwareReleases", Map.of(BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, "Releases",
                    BasicStructuresClass.STR_TITLE, "Software Releases"),
            BasicStructuresClass.STR_TS, Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-square-poll-horizontal",
                    BasicStructuresClass.STR_MENU, "TStatistics",
                    BasicStructuresClass.STR_TITLE, "Table Statistics"),
            "FilesHashing", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-hashtag",
                    BasicStructuresClass.STR_MENU, "Hashing",
                    BasicStructuresClass.STR_TITLE, "Downloads File Hashing"),
            "EnvironmentDetails", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-computer",
                    BasicStructuresClass.STR_MENU, "Environment",
                    BasicStructuresClass.STR_TITLE, "Environment Details")
            );

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
    private static HttpHandler handleWebContent() {
        return exchange -> {
            // Get the 'page' query parameter (Deques are used for multi-value parameters)
            final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
            final Deque<String> pageParams = queryParams.get("page");
            final String page = (pageParams != null) ? pageParams.getFirst() : "home";
            final String strTitle = mapMenu.get(page).get(BasicStructuresClass.STR_TITLE);
            final gg.jte.Content menuContent = output -> output.writeContent(UndertowClass.buildMenuContent());
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
            UndertowClass.TemplateRendering.packParameter("menu", menuContent);
            UndertowClass.TemplateRendering.packParameter("content", bodyContent);
            UndertowClass.TemplateRendering.packParameter("timeNow", TimingClass.getCurrentTimestamp("EEEE, dd MMMM yyyy HH:mm:ss.SSS"));
            UndertowClass.TemplateRendering.renderTemplate(templateEngine, "index.jte");
        };
    }

    /**
     * Execution
     * @param args input arguments
     */
    public static void main(final String[] args) {
        UndertowClass.setWebPort("8075");
        UndertowClass.setMapMenu(mapMenu);
        DatabaseOperationsClass.SpecificSqLiteClass.setInternalDatabase(args[0]);
        SoftwareReleasesClass.setReleasesDatabase(args[0]);
        UndertowClass.setRootHandler(handleWebContent());
        UndertowClass.runWebServer();
    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

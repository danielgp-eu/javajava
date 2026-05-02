package javajava;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final SequencedMap<String, Map<String, String>> mapMenu = Stream.of(
            Map.entry("home", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "Home",
                    BasicStructuresClass.STR_TITLE, "Home Page")),
            Map.entry("SoftwareReleases", Map.of(BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, "Software Releases",
                    BasicStructuresClass.STR_TITLE, "Software Releases")),
            Map.entry(BasicStructuresClass.STR_TS, Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-square-poll-horizontal",
                    BasicStructuresClass.STR_MENU, "SQLite Table Statistics",
                    BasicStructuresClass.STR_TITLE, "SQLite Table Statistics")),
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
     * Body content handler
     * @return web Content
     */
    public static gg.jte.Content handleBodyContent() {
        final String page = UndertowClass.ParametersClass.getPageParameter();
        return output -> output.writeContent(switch(page) {
            case "EnvironmentDetails"        -> HtmlClass.getEnvironmentDetailsAsHtmlTable();
            case "FilesHashing"              -> getFileHashingAsHtmlTable();
            case "SoftwareReleases"          -> getSoftwareReleasesIntoHtml();
            case BasicStructuresClass.STR_TS -> HtmlClass.getTableStatisticsAsHtmlTable();
            default                          -> String.format("Welcome %s", System.getProperty("user.name"));
        });
    }

    /**
     * Handle web content
     * @return PathHandler web content
     */
    public static HttpHandler handleWebContent() {
        return exchange -> {
            UndertowClass.handleCommonThings(exchange);
            final String page = UndertowClass.ParametersClass.getPageParameter();
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
            final TemplateEngine templateEngine = UndertowClass.createTemplateEngine();
            final Utf8ByteOutput output = new Utf8ByteOutput();
            UndertowClass.TemplateRenderingClass.setOutput(output);
            UndertowClass.TemplateRenderingClass.setServerExchange(exchange);
            packAllParameters();
            UndertowClass.TemplateRenderingClass.renderTemplate(templateEngine, "index.jte");
        };
    }

    /**
     * Packing all parameters to Template
     */
    private static void packAllParameters() {
        final String page = UndertowClass.ParametersClass.getPageParameter();
        UndertowClass.TemplateRenderingClass.packParameter("page", page);
        UndertowClass.TemplateRenderingClass.packParameter("title", BasicStructuresClass.STR_LOCALIZATION.equalsIgnoreCase(page) ? page : mapMenu.get(page).get(BasicStructuresClass.STR_TITLE));
        UndertowClass.TemplateRenderingClass.packParameter("menu", HtmlClass.CommonWebElements.buildMenu(mapMenu));
        UndertowClass.TemplateRenderingClass.packParameter("content", handleBodyContent());
        UndertowClass.TemplateRenderingClass.packCommonParameters();
    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

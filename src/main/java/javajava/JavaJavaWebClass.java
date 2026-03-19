package javajava;

import java.util.Map;
import java.util.Properties;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Deque;
import java.util.List;

/**
 * Web interface class
 */
public final class JavaJavaWebClass {
    /**
     * Menu
     */
    private final static Map<String, Map<String, String>> mapMenu = Map.of(
            "home", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "Home",
                    BasicStructuresClass.STR_TITLE, "Home Page"),
            "SoftwareReleases", Map.of(BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, "Releases",
                    BasicStructuresClass.STR_TITLE, "Software Releases"),
            "FilesHashing", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-hashtag",
                    BasicStructuresClass.STR_MENU, "Hashing",
                    BasicStructuresClass.STR_TITLE, "Downloads File Hashing"),
            "EnvironmentDetails", Map.of(BasicStructuresClass.STR_ICON, "fa-solid fa-computer",
                    BasicStructuresClass.STR_MENU, "Environment",
                    BasicStructuresClass.STR_TITLE, "Environment Details")
            );

    /**
     * Outputs file statistics into a HTML table
     * @return String
     */
    public static String getEnvironmentDetailsAsHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        final List<Properties> envDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        return UndertowClass.HyperTextMarkupLanguageTable.getListOfPropertiesIntoHtmlTable(envDetails, objFeatures);
    }

    /**
     * Outputs file statistics into a HTML table
     * @return String
     */
    public static String getFileHashingAsHtmlTable() {
        final String[] inAlgorithms = {"SHA-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        final List<Properties> crtFileStatistics = FileStatisticsClass.getFileStatisticsIntoMap("C:/www/Downloads/");
        return UndertowClass.HyperTextMarkupLanguageTable.getListOfPropertiesIntoHtmlTable(crtFileStatistics, new Properties());
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    public static String getSoftwareReleasesIntoHtml() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Profile");
        return UndertowClass.HyperTextMarkupLanguageTable.getListOfPropertiesIntoHtmlTable(SoftwareReleasesClass.consolidateSoftwareReleases(), objFeatures);
    }

    /**
     * Handle web content
     * @param resourceManager resource manager
     * @return PathHandler web content
     */
    private static HttpHandler handleWebContent() {
        return new HttpHandler() {
            @Override
            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                // Get the 'page' query parameter (Deques are used for multi-value parameters)
                final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
                final Deque<String> pageParams = queryParams.get("page");
                final String page = (pageParams != null) ? pageParams.getFirst() : "home";
                final String strTitle = mapMenu.get(page).get(BasicStructuresClass.STR_TITLE);
                final gg.jte.Content menuContent = output -> {
                    output.writeContent(UndertowClass.buildMenuContent());
                };
                final gg.jte.Content bodyContent = output -> {
                    output.writeContent(switch(page) {
                        case "EnvironmentDetails"   -> getEnvironmentDetailsAsHtmlTable();
                        case "FilesHashing"         -> getFileHashingAsHtmlTable();
                        case "SoftwareReleases"     -> getSoftwareReleasesIntoHtml();
                        default                     -> String.format("Welcome %s", System.getProperty("user.name"));
                    });
                };
                final TemplateEngine templateEngine = UndertowClass.createTemplateEngine();
                final Utf8ByteOutput output = new Utf8ByteOutput();
                UndertowClass.TemplateRendering.setOutput(output);
                UndertowClass.TemplateRendering.setServerExchange(exchange);
                UndertowClass.TemplateRendering.packParameter("page", page);
                UndertowClass.TemplateRendering.packParameter("title", strTitle);
                UndertowClass.TemplateRendering.packParameter("menu", menuContent);
                UndertowClass.TemplateRendering.packParameter("content", bodyContent);
                UndertowClass.TemplateRendering.renderTemplate(templateEngine, "index.jte");
            }
        };
    }

    /**
     * Execution
     * @param args
     */
    public static void main(String[] args) {
        UndertowClass.setMapMenu(mapMenu);
        SoftwareReleasesClass.setReleasesDatabase(args[0]);
        UndertowClass.setRootHandler(handleWebContent());
        UndertowClass.runWebServer();
    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

package javajava;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import io.undertow.server.HttpHandler;
import javajava.HtmlClass.TableSubClass;

/**
 * Web interface class
 */
public final class JavaJavaWebClass {
    /**
     * Menu
     */
    private static final SequencedMap<String, Map<String, String>> MAP_MENU = Stream.of(
            Map.entry("home", Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-house-user",
                    BasicStructuresClass.STR_MENU, "JavaJava",
                    BasicStructuresClass.STR_TITLE, "JavaJava")),
            Map.entry(BasicStructuresClass.STR_SOFTWARE_RLS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-brands fa-dev",
                    BasicStructuresClass.STR_MENU, "Software Releases",
                    BasicStructuresClass.STR_TITLE, "Software Releases")),
            Map.entry(BasicStructuresClass.STR_TS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-square-poll-horizontal",
                    BasicStructuresClass.STR_MENU, "SQLite Table Statistics",
                    BasicStructuresClass.STR_TITLE, "SQLite Table Statistics")),
            Map.entry(BasicStructuresClass.STR_FILE_HASHING, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-hashtag",
                    BasicStructuresClass.STR_MENU, "Downloads File Hashing",
                    BasicStructuresClass.STR_TITLE, "Downloads File Hashing")),
            Map.entry(BasicStructuresClass.STR_ENV_DTLS, Map.of(
                    BasicStructuresClass.STR_ICON, "fa-solid fa-computer",
                    BasicStructuresClass.STR_MENU, "Environment Details",
                    BasicStructuresClass.STR_TITLE, "Environment Details"))
    ).collect(
            Collectors.toMap(
                    Map.Entry::getKey, 
                    Map.Entry::getValue, 
                    (v1, _) -> v1, 
                    LinkedHashMap::new)  // Ensures it returns a SequencedMap
    );

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    public static String getEnvironmentDetailsAsHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        final List<Properties> envDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        final List<String> desiredOrder = List.of("Category", "Element", "Value");
        final List<SequencedMap<Object, Object>> orderedList = envDetails.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

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
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());  
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    private static String getSoftwareReleasesIntoHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Profile");
        final List<Properties> softwareReleases = SoftwareReleasesClass.consolidateSoftwareReleases();
        final List<String> desiredOrder = List.of("Organization", "Product", "Version", "Date", "Files");
        final List<SequencedMap<Object, Object>> orderedList = softwareReleases.stream()
                .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                .toList();
        return HtmlClass.TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Body content handler
     * @return web Content
     */
    public static gg.jte.Content handleBodyContent() {
        final String page = UndertowClass.ParametersSubClass.getPageParameter();
        return output -> output.writeContent(switch(page) {
            case BasicStructuresClass.STR_ENV_DTLS      -> getEnvironmentDetailsAsHtmlTable()
                    + ProjectClass.buildProductObjectModelFileInfoBox();
            case BasicStructuresClass.STR_FILE_HASHING  -> getFileHashingAsHtmlTable();
            case BasicStructuresClass.STR_SOFTWARE_RLS  -> getSoftwareReleasesIntoHtmlTable()
                    + SqLiteStatisticsSubClass.buildSqLiteFileInfoBox();
            case BasicStructuresClass.STR_TS            -> SqLiteStatisticsSubClass.getTableStatisticsAsHtmlTable()
                    + SqLiteStatisticsSubClass.buildSqLiteFileInfoBox();
            default                                     -> String.format("Welcome %s", System.getProperty("user.name"));
        });
    }

    /**
     * Handle web content
     * @return PathHandler web content
     */
    public static HttpHandler handleWebContent() {
        return exchange -> {
            UndertowClass.handleCommonThings(exchange);
            final String page = UndertowClass.ParametersSubClass.getPageParameter();
            switch(page) {
                case BasicStructuresClass.STR_FILE_HASHING, BasicStructuresClass.STR_SOFTWARE_RLS, BasicStructuresClass.STR_TS:
                    TimingClass.LocalizationSubClass.setInputTimeZone("UTC");
                    break;
                default:
                    // intentionally blank
                    break;
            }
            final TemplateEngine templateEngine = UndertowClass.createTemplateEngine();
            final Utf8ByteOutput output = new Utf8ByteOutput();
            UndertowClass.TemplateRenderingSubClass.setOutput(output);
            UndertowClass.TemplateRenderingSubClass.setServerExchange(exchange);
            packAllParameters();
            UndertowClass.TemplateRenderingSubClass.renderTemplate(templateEngine, "index.jte");
        };
    }

    /**
     * Packing all parameters to Template
     */
    private static void packAllParameters() {
        final String page = UndertowClass.ParametersSubClass.getPageParameter();
        UndertowClass.TemplateRenderingSubClass.packParameter("page", page);
        UndertowClass.TemplateRenderingSubClass.packParameter("title", BasicStructuresClass.STR_LOCALIZATION.equalsIgnoreCase(page) ? page : MAP_MENU.get(page).get(BasicStructuresClass.STR_TITLE));
        UndertowClass.TemplateRenderingSubClass.packParameter("menu", HtmlClass.buildMenu(MAP_MENU));
        UndertowClass.TemplateRenderingSubClass.packParameter("content", handleBodyContent());
        UndertowClass.TemplateRenderingSubClass.packCommonParameters();
    }

    /**
     * List and Maps management
     */
    public static final class SqLiteStatisticsSubClass {

        /**
         * Build Information Box
         * @return String
         */
        private static String buildSqLiteFileInfoBox() {
            final Path fileName = Path.of(DatabaseOperationsClass.SpecificSqLiteSubClass.getInternalDatabase());
            return HtmlClass.buildFileInfoBox(fileName);
        }

        /**
         * read SQLite tables and their record count
         * @return StringBuilder
         */
        private static StringBuilder buildTableRecordCounting() {
            final String strQueryCount = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTableRecordCounting");
            final StringBuilder strQueryRaw = new StringBuilder(1000);
            final List<Properties> resultTables = getTablesAndTheirSequence();
            resultTables.forEach(objProperty -> {
                if (!strQueryRaw.isEmpty()) {
                    strQueryRaw.append(" UNION ALL ");
                }
                strQueryRaw.append(String.format(strQueryCount,
                        objProperty.get(BasicStructuresClass.STR_TABLE),
                        objProperty.get("Sequence"),
                        objProperty.get(BasicStructuresClass.STR_TABLE)));
            });
            return strQueryRaw;
        }

        /**
         * read SQLite tables and their sequence
         * @return List<Properties>
         */
        private static List<Properties> getTablesAndTheirSequence() {
            final String queryTables = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTablesAndTheirSequence");
            final String strFeedback = String.format("Table list and their sequence query is: %s", queryTables);
            LogExposureClass.LOGGER.debug(strFeedback);
            return DatabaseOperationsClass.SpecificSqLiteSubClass.getSqLiteResultSetValues("Table list and their sequence", queryTables);
        }

        /**
         * Outputs table statistics into an HTML table
         * @return String
         */
        public static String getTableStatisticsAsHtmlTable() {
            final StringBuilder queryRecordCount = buildTableRecordCounting();
            final String queryTableStats = DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "StatisticsTables");
            final String strFinalQuery =  String.format(queryTableStats, queryRecordCount);
            final List<Properties> resultTableStats = DatabaseOperationsClass.SpecificSqLiteSubClass.getSqLiteResultSetValues("Table Statistics", strFinalQuery);
            final List<String> desiredOrder = List.of("#", BasicStructuresClass.STR_TABLE, "Records", "Sequence", "Gap");
            final List<SequencedMap<Object, Object>> orderedList = resultTableStats.stream()
                    .map(prop -> BasicStructuresClass.ListAndMapSubClass.sortProperties(prop, desiredOrder))
                    .toList();
            return TableSubClass.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());
        }

        /**
         * constructor
         */
        private SqLiteStatisticsSubClass() {
            // intentionally left blank
        }

    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

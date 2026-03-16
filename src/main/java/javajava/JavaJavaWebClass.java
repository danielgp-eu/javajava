package javajava;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import gg.jte.output.Utf8ByteOutput; // Crucial for binary throughput
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

/**
 * Web interface class
 */
public final class JavaJavaWebClass {
    /**
     * Path for Web templates
     */
    private static String pathTemplates = "web/templates";
    /**
     * Path for static Web components
     */
    private static String pathStatic = "web/static";

    /**
     * Initiating Template Engine
     * @return TemplateEngine
     */
    private static TemplateEngine createTemplateEngine() {
        final ResourceCodeResolver resolver = new ResourceCodeResolver(pathTemplates);
        final TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        return templateEngine;
    }


    /**
     * HTML Table logic
     */
    public static final class HyperTextMarkupLanguageTable {

        /**
         * Table Body row logic
         * @param recordProperties
         * @return String
         */
        private static String buildTableBodyRow(final String strRememberKey, final Properties recordProperties) {
            final StringBuilder strHtmlTable = new StringBuilder(1000);
            strHtmlTable.append("<tr>");
            recordProperties.forEach((strKey, strValue) -> {
                if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                        && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                    String cellStyle = "";
                    if (recordProperties.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                        cellStyle = recordProperties.get(BasicStructuresClass.STR_ROW_STYLE).toString();
                    }
                    if (BasicStructuresClass.StringEvaluationClass.isStringActuallyInteger(strValue.toString())) {
                        cellStyle = cellStyle + "text-align:right;";
                    }
                    if (cellStyle.isEmpty()) {
                        strHtmlTable.append(String.format("<td>%s</td>", strValue));
                    } else {
                        strHtmlTable.append(String.format("<td style=\"%s\">%s</td>", cellStyle, strValue));
                    }
                }
            });
            strHtmlTable.append("</tr>");
            return strHtmlTable.toString();
        }

        /**
         * Generate HTML from a Map of values
         * @param inList values stored as a list
         * @return String
         */
        public static String getListOfPropertiesIntoHtmlTable(final List<Properties> inList, final Properties objFeatures) {
            final StringBuilder strHeaderTable = new StringBuilder(100);
            final StringBuilder strHtmlTable = new StringBuilder(1000);
            final String strRememberKey = getRememberKey(objFeatures);
            String[] strRememberValue = { "None" };
            inList.forEach( recordProperties -> {
                if (strHeaderTable.isEmpty()) {
                    strHeaderTable.append("<table><thead>");
                    recordProperties.forEach((strKey, _) -> {
                        if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                                && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                            strHeaderTable.append(String.format("<th>%s</th>", strKey));
                        }
                    });
                    strHeaderTable.append("</thead><tbody>");
                }
                if (strRememberKey.isEmpty()) {
                    if (strHtmlTable.isEmpty()) {
                        strHtmlTable.append(strHeaderTable);
                    }
                } else {
                    final String crtValueForTab = recordProperties.get(strRememberKey).toString();
                    if (!strRememberValue[0].equalsIgnoreCase(crtValueForTab)) {
                        if (strHtmlTable.isEmpty()) {
                            strHtmlTable.append("<div id=\"tabStandard\" class=\"tabber\">");
                        } else {
                            strHtmlTable.append(String.format("</tbody></table></div><!-- %s -->", crtValueForTab));
                        }
                        strHtmlTable.append(String.format("<div class=\"tabbertab\" title=\"%s\">" + strHeaderTable, crtValueForTab));
                        strRememberValue[0] = crtValueForTab;
                    }
                }
                strHtmlTable.append(buildTableBodyRow(strRememberKey, recordProperties));
            });
            strHtmlTable.append("</tbody></table>");
            if (!strRememberKey.isEmpty()) {
                strHtmlTable.append(String.format("</div><!-- %s --></div><!-- tabStandard -->", strRememberValue[0]));
            }
            return strHtmlTable.toString();
        }

        // Private constructor to prevent instantiation
        private HyperTextMarkupLanguageTable() {
            // intentional empty
        }

    }

    /**
     * establishing the Key to Remember if relevant
     * @param objFeatures optional HTML Table features
     * @return String
     */
    private static String getRememberKey(final Properties objFeatures) {
        String strRememberKey = "";
        if (objFeatures.containsKey("New Tab and Table on column value change")) {
            strRememberKey = objFeatures.get("New Tab and Table on column value change").toString();
        }
        return strRememberKey;
    }

    /**
     * expose Software Release details from internal DB
     * @return String software releases details
     */
    public static String getSoftwareReleasesIntoHtml() {
        final Properties objFeatures = new Properties();
        objFeatures.put("New Tab and Table on column value change", "Profile");
        return HyperTextMarkupLanguageTable.getListOfPropertiesIntoHtmlTable(SoftwareReleases.consolidateSoftwareReleases(), objFeatures);
    }

    /**
     * Outputs file statistics into a HTML table
     * @return String
     */
    public static String getFileHashingAsHtmlTable() {
        final String[] inAlgorithms = {"SHA-256"};
        FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
        final List<Properties> crtFileStatistics = FileStatisticsClass.getFileStatisticsIntoMap("C:/www/Downloads/");
        return HyperTextMarkupLanguageTable.getListOfPropertiesIntoHtmlTable(crtFileStatistics, new Properties());
    }

    /**
     * Handle web content
     * @param resourceManager resource manager
     * @return PathHandler web content
     */
    private static PathHandler handleWebContent(final ClassPathResourceManager resourceManager) {
        final TemplateEngine templateEngine = createTemplateEngine();
        final ResourceHandler staticHandler = new ResourceHandler(resourceManager)
                .setDirectoryListingEnabled(false);
        final HttpHandler rootHandler = new HttpHandler() {
            @Override
            public void handleRequest(final HttpServerExchange exchange) throws Exception {
                // Get the 'page' query parameter (Deques are used for multi-value parameters)
                final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
                final Deque<String> pageParams = queryParams.get("page");
                final String page = (pageParams != null) ? pageParams.getFirst() : "HOME";
                final gg.jte.Content bodyContent = output -> {
                    output.writeContent(switch(page) {
                        case "SoftwareReleases" -> getSoftwareReleasesIntoHtml();
                        case "FilesHashing"     -> getFileHashingAsHtmlTable();
                        default -> "Work In Progress";
                    });
                };
                final Utf8ByteOutput output = new Utf8ByteOutput();
                TemplateRendering.setOutput(output);
                TemplateRendering.setServerExchange(exchange);
                TemplateRendering.packParameter("page", page);
                TemplateRendering.packParameter("content", bodyContent);
                TemplateRendering.renderTemplate(templateEngine, "index.jte");
            }
        };
        return Handlers.path()
                .addPrefixPath("/" + pathStatic, staticHandler)
                .addPrefixPath("/", rootHandler);
    }

    /**
     * Execution
     * @param args
     */
    public static void main(String[] args) {
        try (ClassPathResourceManager resourceManager = new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(),
                pathStatic)) {
            final PathHandler routesHandler = handleWebContent(resourceManager);
            final String[] varsToPick = {"webIp", "wepPort"};
            final Properties webProperties = BasicStructuresClass.PropertiesReaderClass.getVariableFromProjectProperties("/project.properties", varsToPick);
            final Undertow.Builder builder = Undertow.builder()
                    .addHttpListener(Integer.parseInt(webProperties.get("wepPort").toString()),
                            webProperties.get("webIp").toString())
                    // Increase worker threads based on your CPU cores
                    //.setWorkerThreads(Runtime.getRuntime().availableProcessors() * 8)
                    .setHandler(routesHandler);
            final Undertow server = builder.build();
            final String strFeedback = "Server running at http://localhost:8080";
            LogExposureClass.LOGGER.info(strFeedback);
            server.start();
        } catch (IOException ex) {
            final String strFeedbackErr = String.format("Error on getting static resources... %s", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
    }

    /**
     * Template management
     */
    public static final class TemplateRendering {
        /**
         * server exchange
         */
        private static HttpServerExchange exchange;
        /**
         * page parameters
         */
        final private static Map<String, Object> params = new ConcurrentHashMap<>();
        /**
         * output handler
         */
        private static Utf8ByteOutput output;

        /**
         * Helper method with explicit typing to handle rendering
         */
        public static void renderTemplate(final TemplateEngine engine, final String fileName) {
            engine.render(fileName, params, output);
            final HeaderMap header = exchange.getResponseHeaders();
            handleResponseHeader(header);
            final Sender response = exchange.getResponseSender();
            handleResponseSender(response);
        }

        /**
         * handle Response Header
         */
        private static void handleResponseHeader(final HeaderMap header) {
            final long contentLength = output.getContentLength();
            header.put(Headers.CONTENT_TYPE, "text/html");
            header.put(Headers.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        /**
         * handle Response Sender
         */
        private static void handleResponseSender(final Sender response) {
            response.send(ByteBuffer.wrap(output.toByteArray()));
        }

        /**
         * pack Parameter
         */
        public static void packParameter(final String name, final Object value) {
            params.put(name, value);
        }

        /**
         * Setter for Server Exchange
         * @param inExchange input Exchange
         */
        public static void setServerExchange(final HttpServerExchange inExchange) {
           exchange = inExchange;
        }
        /**
         * Setter for output
         * @param inOutput Output
         */
        public static void setOutput(final Utf8ByteOutput inOutput) {
           output = inOutput;
        }

        // Private constructor to prevent instantiation
        private TemplateRendering() {
            // intentional empty
        }

    }

    private JavaJavaWebClass() {
        // intentionally blank
    }

}

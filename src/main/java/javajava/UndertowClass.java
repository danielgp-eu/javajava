package javajava;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;
import java.util.concurrent.ConcurrentHashMap;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.ResourceCodeResolver;
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
 * Undertow common class
 */
public final class UndertowClass {
    /**
     * Menu
     */
    private static Map<String, Map<String, String>> mapMenu;
    /**
     * Path for Web templates
     */
    private static String pathTemplates = "web/templates";
    /**
     * Path for static Web components
     */
    private static String pathStatic = "web/static";
    /**
     * Root handle variable
     */
    private static HttpHandler rootHandler;
    /**
     * Web IP variable
     */
    private static String webIp;
    /**
     * Web port variable
     */
    private static String webPort;

    /**
     * Building HTML menu
     * @return String
     */
    public static String buildMenuContent() {
        final StringBuilder strMenuContent = new StringBuilder(1000);
        mapMenu.forEach((strKey, mapValue) -> {
            strMenuContent.append(String.format("<li><a href=\"?page=%s\"><i class=\"%s\"></i>%s</a></li>", strKey, mapValue.get(BasicStructuresClass.STR_ICON), mapValue.get(BasicStructuresClass.STR_MENU)));
        });
        return strMenuContent.toString();
    }

    /**
     * Initiating Template Engine
     * @return TemplateEngine
     */
    public static TemplateEngine createTemplateEngine() {
        final ResourceCodeResolver resolver = new ResourceCodeResolver(pathTemplates);
        final TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        return templateEngine;
    }

    /**
     * Reading Project Properties
     */
    private static void readWebConfigurationFromProjectProperties() {
        final String[] varsToPick = {"webIp", "wepPort"};
        final Properties webProperties = BasicStructuresClass.PropertiesReaderClass.getVariableFromProjectProperties("/project.properties", varsToPick);
        if (webPort == null) {
            webPort = webProperties.get("wepPort").toString();
        }
        webIp = webProperties.get("webIp").toString();
    }

    /**
     * Web server logic
     */
    public static void runWebServer() {
        readWebConfigurationFromProjectProperties();
        try (ClassPathResourceManager resourceManager = new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(),
                pathStatic)) {
            final ResourceHandler staticHandler = new ResourceHandler(resourceManager)
                    .setDirectoryListingEnabled(false);
            final PathHandler routesHandler = Handlers.path()
                    .addPrefixPath("/" + pathStatic, staticHandler)
                    .addPrefixPath("/", rootHandler);
            final Undertow.Builder builder = Undertow.builder()
                    .addHttpListener(Integer.parseInt(webPort), webIp)
                    // Increase worker threads based on your CPU cores
                    //.setWorkerThreads(Runtime.getRuntime().availableProcessors() * 8)
                    .setHandler(routesHandler);
            final Undertow server = builder.build();
            final String strFeedback = String.format("Server running at http://%s:%s", webIp, webPort);
            LogExposureClass.LOGGER.info(strFeedback);
            server.start();
        } catch (IOException ex) {
            final String strFeedbackErr = String.format("Error on getting static resources... %s", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
    }

    /**
     * setter for Menu Content 
     * @param inMapMenu map with menu content
     */
    public static void setRootHandler(final HttpHandler inRootHandler) {
        rootHandler = inRootHandler;
    }

    /**
     * setter for webPort
     * @param inWebPort web port to use
     */
    public static void setWebPort(final String inWebPort) {
        webPort = inWebPort;
    }

    /**
     * setter for Menu Content 
     * @param inMapMenu map with menu content
     */
    public static void setMapMenu(final Map<String, Map<String, String>> inMapMenu) {
        mapMenu = inMapMenu;
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
        private static String buildTableBodyRow(final String strRememberKey, final SequencedMap<Object, Object> recordProperties) {
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
        public static String getListOfSequencedMapIntoHtmlTable(final List<SequencedMap<Object, Object>> inList, final Properties objFeatures) {
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

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String getRememberKey(final Properties objFeatures) {
            String strRememberKey = "";
            if (objFeatures.containsKey(BasicStructuresClass.STR_NEW_TAB)) {
                strRememberKey = objFeatures.get(BasicStructuresClass.STR_NEW_TAB).toString();
            }
            return strRememberKey;
        }

        // Private constructor to prevent instantiation
        private HyperTextMarkupLanguageTable() {
            // intentional empty
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

    private UndertowClass() {
        // intentionally blank
    }

}

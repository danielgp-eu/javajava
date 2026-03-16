package javajava;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
     * Internal database name
     */
    private static final String RELEASES_DATABASE = "C:\\www\\Data\\GitRepositories\\GitHub\\danielgp\\PHP\\control_center\\source\\SoftwareReleases\\config\\configuration.sqlite";

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
     * Generate HTML from a Map of values
     * @param inList values stored as a list
     * @return String
     */
    private static String getMapIntoHtmlTable(final List<Properties> inList, final Properties objFeatures) {
        final StringBuilder strHeaderTable = new StringBuilder(100);
        final StringBuilder strHtmlTable = new StringBuilder(1000);
        final String strRememberKey = getRememberKey(objFeatures);
        String[] strRememberValue = { "None" };
        inList.forEach( fileProperties -> {
            if (strHeaderTable.isEmpty()) {
                strHeaderTable.append("<table><thead>");
                fileProperties.forEach((strKey, _) -> {
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
                final String crtValueForTab = fileProperties.get(strRememberKey).toString();
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
            strHtmlTable.append("<tr>");
            fileProperties.forEach((strKey, strValue) -> {
                if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                        && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                    if (fileProperties.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                        strHtmlTable.append(String.format("<td style=\"%s\">%s</td>", fileProperties.get(BasicStructuresClass.STR_ROW_STYLE), strValue));
                    } else {
                        strHtmlTable.append(String.format("<td>%s</td>", strValue));
                    }
                }
            });
            strHtmlTable.append("</tr>");
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
        if (objFeatures.containsKey("New Tab and Table on column value change")) {
            strRememberKey = objFeatures.get("New Tab and Table on column value change").toString();
        }
        return strRememberKey;
    }

    /**
     * List and Maps management
     */
    public static final class SoftwareReleases {

        /**
         * friendly Aging logic
         * @param agingDays 
         * @return String aging
         */
        private static String calculateAging(final String agingDays) {
            String strAging = "";
            if (!agingDays.isEmpty()) {
                final int intAging = Integer.parseInt(agingDays);
                strAging = switch(intAging) {
                    case 0 -> "TODAY";
                    case 1 -> "YESTERDAY";
                    case 2 -> "the day before yesterday";
                    default -> agingDays + " days ago";
                };
            }
            return strAging;
        }

        /**
         * expose Software Release details from internal DB
         * @return List software releases details
         */
        private static List<Properties> consolidateSoftwareReleases() {
            final List<Properties> softwareReleases = new ArrayList<>();
            final List<Properties> resultReleases = getSoftwareReleasesFromDatabase();
            resultReleases.forEach( recordProperties -> {
                final Properties newProperties = new Properties();
                newProperties.put("Organization", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("OrganizationName"), recordProperties.get("OrganizationId")));
                newProperties.put("Product", String.format("<a href=\"%s\" target=\"_blank\"><span style=\"float:left;\">%s<br/>[%s]</span><span style=\"float:right;text-align:right;\">%s<br/>[%s]</span></a>", recordProperties.get("Releases"), recordProperties.get("ProductName"), recordProperties.get("ProductId"), recordProperties.get("BranchName"), recordProperties.get("BranchId")));
                newProperties.put("Version", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("Latest release version"), recordProperties.get("VersionId")));
                final String agingDays = recordProperties.get("Latest release aging").toString().replaceAll("\\.0", ""); 
                newProperties.put("Date", String.format("%s<br>==> %s", recordProperties.get("Latest release date"), calculateAging(agingDays)));
                newProperties.put("Files", String.format("%s [%s]<br/>==> %s [%s]", recordProperties.get("File Kit Name"), recordProperties.get("File Kit Id"), recordProperties.get("File Installed Name"), recordProperties.get("File Installed Id")));
                newProperties.put("Profile", recordProperties.get("Profile Name"));
                newProperties.put(BasicStructuresClass.STR_ROW_STYLE, establishRowStyle(agingDays));
                softwareReleases.add(newProperties);
            });
            return softwareReleases;
        }

        /**
         * Row Style logic
         * @param agingDays 
         * @return String row style
         */
        private static String establishRowStyle(final String agingDays) {
            String strRowColor = "#fff";
            if (!agingDays.isEmpty()) {
                final long[] longRanges = {14, 30, 90};
                final long longAging = Long.parseLong(agingDays);
                if (longAging <= longRanges[0]) {
                    strRowColor = "#51ff6d";
                } else if (longAging <= longRanges[1]) {
                    strRowColor = "#ccffe8";
                } else if (longAging <= longRanges[2]) {
                    strRowColor = "#fdffcc";
                }
            }
            return String.format("background-color:%s;", strRowColor);
        }

        /**
         * Outputs file statistics into a HTML table
         * @return String
         */
        public static String getFileHashingAsHtmlTable() {
            final String[] inAlgorithms = {"SHA-256"};
            FileStatisticsClass.setChecksumAlgorithms(inAlgorithms);
            final List<Properties> crtFileStatistics = FileStatisticsClass.getFileStatisticsIntoMap("C:/www/Downloads/");
            return getMapIntoHtmlTable(crtFileStatistics, new Properties());
        }

        /**
         * expose Software Release details from internal DB
         * @return List software releases details
         */
        private static List<Properties> getSoftwareReleasesFromDatabase() {
            List<Properties> resultReleases = new ArrayList<>();
            try (Connection objConnection = DatabaseOperationsClass.SpecificSqLiteClass.getSqLiteConnection(RELEASES_DATABASE);
                    Statement objStatement = DatabaseOperationsClass.ConnectivityClass.createSqlStatement(BasicStructuresClass.STR_SQLITE, objConnection);) {
                final Properties rsProperties = new Properties();
                rsProperties.put("Which", "Software Releases"); // purpose
                rsProperties.put("QueryToUse", DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "ListProductBranches"));
                rsProperties.put("Kind", "Values");
                final Properties queryProperties = new Properties();
                resultReleases = DatabaseOperationsClass.ResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
            } catch (SQLException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationFailedLight"), BasicStructuresClass.STR_SQLITE, e.getLocalizedMessage());
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return resultReleases;
        }

        /**
         * expose Software Release details from internal DB
         * @return String software releases details
         */
        public static String getSoftwareReleasesIntoHtml() {
            final Properties objFeatures = new Properties();
            objFeatures.put("New Tab and Table on column value change", "Profile");
            return getMapIntoHtmlTable(consolidateSoftwareReleases(), objFeatures);
        }

        // Private constructor to prevent instantiation
        private SoftwareReleases() {
            // intentional empty
        }

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
                        case "SoftwareReleases" -> SoftwareReleases.getSoftwareReleasesIntoHtml();
                        case "FilesHashing"     -> SoftwareReleases.getFileHashingAsHtmlTable();
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
     * List and Maps management
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

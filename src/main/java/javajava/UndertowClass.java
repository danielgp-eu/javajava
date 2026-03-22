package javajava;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
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
        mapMenu.forEach((strKey, mapValue) -> strMenuContent.append(String.format("<li><a href=\"?page=%s\"><i class=\"%s\"></i>%s</a></li>", strKey, mapValue.get(BasicStructuresClass.STR_ICON), mapValue.get(BasicStructuresClass.STR_MENU))));
        return strMenuContent.toString();
    }

    /**
     * Initiating Template Engine
     * @return TemplateEngine
     */
    public static TemplateEngine createTemplateEngine() {
        final ResourceCodeResolver resolver = new ResourceCodeResolver("web/templates");
        final TemplateEngine templateEngine = TemplateEngine.create(resolver, ContentType.Html);
        templateEngine.setBinaryStaticContent(true);
        return templateEngine;
    }

    /**
     * Reading Project Properties
     */
    private static void readWebConfigurationFromProjectProperties() {
        final String[] varsToPick = {"webIp"};
        final Properties webProperties = BasicStructuresClass.PropertiesReaderClass.getVariableFromProjectProperties("/project.properties", varsToPick);
        webIp = webProperties.get("webIp").toString();
    }

    /**
     * Web server logic
     */
    public static void runWebServer() {
        readWebConfigurationFromProjectProperties();
        final String pathStatic = "web/static";
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
    public static void setMapMenu(final Map<String, Map<String, String>> inMapMenu) {
        mapMenu = inMapMenu;
    }

    /**
     * setter for Root Handler
     * @param inRootHandler map with root handler
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
        private static final Map<String, Object> params = new ConcurrentHashMap<>();
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

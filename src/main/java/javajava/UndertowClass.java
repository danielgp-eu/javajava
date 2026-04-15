package javajava;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;
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
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

/**
 * Undertow common class
 */
public final class UndertowClass {
    /**
     * Session Manager handle
     */
    private static final InMemorySessionManager SESSION_MANAGER = new InMemorySessionManager("SESSION_MANAGER");
    /**
     * Session Config handle
     */
    private static final SessionCookieConfig SESSION_CONFIG = new SessionCookieConfig();
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
     * Getter for SESSION_MANAGER
     * @return InMemorySessionManager
     */
    public static SessionCookieConfig getSessionConfig() {
        return SESSION_CONFIG;
    }

    /**
     * Getter for SESSION_MANAGER
     * @return InMemorySessionManager
     */
    public static InMemorySessionManager getSessionManager() {
        return SESSION_MANAGER;
    }

    /**
     * Time Zone set logic
     * @param queryParams page parameters
     * @param session session
     */
    public static void handleTimeZoneSession(final Map<String, Deque<String>> queryParams, final Session session) {
        if (queryParams.get("TZ") != null) {
            session.setAttribute("TZ", queryParams.get("TZ").getFirst());
        }
        if (session.getAttribute("TZ") == null) {
            final SequencedMap<String, String> sortedTimeZones = ZoneDataServiceClass.loadSupportedTimeZones();
            final String crtUserTimeZone = System.getProperty("user.timezone");
            if (crtUserTimeZone != null
                    && !sortedTimeZones.getOrDefault(crtUserTimeZone, "").isEmpty()) {
                session.setAttribute("TZ", crtUserTimeZone);
            } else {
                session.setAttribute("TZ", "Asia/Kolkata");
            }
        }
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
            // handle static content
            final ResourceHandler staticHandler = new ResourceHandler(resourceManager)
                    .setDirectoryListingEnabled(false);
            // handle static + dynamic content
            final PathHandler routesHandler = Handlers.path()
                    .addPrefixPath("/" + pathStatic, staticHandler)
                    .addPrefixPath("/", rootHandler);
            // finally package everything to consider Session handler
            final HttpHandler sessionHandler = new SessionAttachmentHandler(
                routesHandler, 
                SESSION_MANAGER, 
                SESSION_CONFIG
            );
            final Undertow.Builder builder = Undertow.builder()
                    .addHttpListener(Integer.parseInt(webPort), webIp)
                    .setHandler(sessionHandler);
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

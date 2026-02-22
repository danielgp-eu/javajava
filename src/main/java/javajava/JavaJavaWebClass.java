package javajava;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;

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
     * Listening Internet Protocol address
     */
    private static String listeningIp = "0.0.0.0";
    /**
     * Listening port number
     */
    private static int listeningPort = 8080;
    /**
     * Path for Web templates
     */
    private static String pathTemplates = "web/templates";
    /**
     * Path for static Web components
     */
    private static String pathStatic = "web/static";
    /**
     * Path for static Web components Bootstrap
     */
    private static String pathStaticB = "web/static/boostrap";
    /**
     * Path for static Web components Fontawesome
     */
    private static String pathStaticF = "web/static/fontawesome";

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
     * execution
     * @param args
     */
    public static void main(String[] args) {
        final TemplateEngine templateEngine = createTemplateEngine();
        try (ClassPathResourceManager resourceManager = new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(),
                pathStatic);
                ClassPathResourceManager resourceManagerB = new ClassPathResourceManager(
                        Thread.currentThread().getContextClassLoader(),
                        pathStaticB);
                ClassPathResourceManager resourceManagerF = new ClassPathResourceManager(
                        Thread.currentThread().getContextClassLoader(),
                        pathStaticF);) {
            final ResourceHandler staticHandler = new ResourceHandler(resourceManager)
                    .setDirectoryListingEnabled(false);
            final ResourceHandler staticHandlerB = new ResourceHandler(resourceManagerB)
                    .setDirectoryListingEnabled(false);
            final ResourceHandler staticHandlerF = new ResourceHandler(resourceManagerF)
                    .setDirectoryListingEnabled(false);
            final HttpHandler roottHandler = new HttpHandler() {
                @Override
                public void handleRequest(final HttpServerExchange exchange) throws Exception {
                    final Utf8ByteOutput output = new Utf8ByteOutput();
                    TemplateRendering.setOutput(output);
                    TemplateRendering.setServerExchange(exchange);
                    // Get the 'page' query parameter (Deques are used for multi-value parameters)
                    final Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
                    final Deque<String> pageParams = queryParams.get("page");
                    final String page = (pageParams != null) ? pageParams.getFirst() : "home";
                    TemplateRendering.packParameter("page", page);
                    TemplateRendering.packParameter("message", "");
                    TemplateRendering.renderTemplate(templateEngine, "index.jte");
                }
            };
            // Route requests: /static/* goes to files, everything else to JTE
            final PathHandler routesHandler = Handlers.path()
                    .addPrefixPath("/" + pathStatic, staticHandler)
                    .addPrefixPath("/" + pathStaticB, staticHandlerB)
                    .addPrefixPath("/" + pathStaticF, staticHandlerF)
                    .addPrefixPath("/", roottHandler);
            final Undertow.Builder builder = Undertow.builder()
                    .addHttpListener(listeningPort, listeningIp)
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
        private static Map<String, Object> params = new ConcurrentHashMap<>();
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

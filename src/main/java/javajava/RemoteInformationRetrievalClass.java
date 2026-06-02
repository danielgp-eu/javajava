package javajava;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * XML management
 */
public final class RemoteInformationRetrievalClass {

    /**
     * building Central Maven Repository as URL
     * @param inPackage input Maven package
     * @return URL to Central Maven Repository
     */
    private static URL buildMavenCentralRepositoryUniformResourceLocatorFromPackage(final String inPackage) {
        final String strWebSite = RegularExpressionsClass.buildCentralMavenRepositoryUniformResourceLocator(inPackage) + "maven-metadata.xml";
        final String strFeedback = String.format("Uniform Resource Locator from Central Maven Repository for %s package is: %s", inPackage, strWebSite);
        LogExposureClass.LOGGER.info(strFeedback);
        return buildUniformResourceLocatorFromString(strWebSite);
    }

    /**
     * build URL from Strin
     * @param strWebSite input URL as String
     * @return URL
     */
    public static URL buildUniformResourceLocatorFromString(final String strWebSite) {
        URL urlReturn = null;
        try {
            urlReturn = URI.create(strWebSite).toURL();
        } catch (MalformedURLException e) {
            final String strFeedback = String.format("Malformed Exception encountered on URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return urlReturn;
    }

    /**
     * get latest version if a Maven Package
     * @param inPackage input Maven package
     * @return String as version
     */
    public static String getLatestVersionFromMavenCentralRepository(final String inPackage) {
        final URL url = buildMavenCentralRepositoryUniformResourceLocatorFromPackage(inPackage);
        String strLatestVersion = "";
        try (InputStream inStream = url.openStream()) {
            final Document doc = getDocumentFromInputStream(inStream);
            if (doc != null) {
                final Node latest = doc.getElementsByTagName("latest").item(0);
                final Node release = doc.getElementsByTagName("release").item(0);
                strLatestVersion = latest != null ? latest.getTextContent() : "";
                if (strLatestVersion.isBlank()) {
                    strLatestVersion = release != null ? release.getTextContent() : "";
                }
            }
        } catch (IOException e) {
            final String strFeedback = String.format("IO Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strLatestVersion;
    }

    /**
     * get Document from inStream
     * @param inStream Input Stream
     * @return Document
     */
    private static Document getDocumentFromInputStream(final InputStream inStream) {
        Document doc = null;
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            docBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            docBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            docBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            docBuilderFactory.setExpandEntityReferences(false);
            doc = parseDocumentFromInputStram(inStream, docBuilderFactory);
        } catch (ParserConfigurationException e) {
            final String strFeedback = String.format("Parser Configuration Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return doc;
    }

    /**
     * capture remote file attributes
     * @param strRemoteFileUrl URL for remote file
     * @return multiple attributes as Properties
     */
    public static Properties getRemoteFileAttributes(final String strRemoteFileUrl) {
        final Properties fileProperties = new Properties();
        try (HttpClient client = HttpClient.newHttpClient()) {
            final HttpRequest request = HttpRequest.newBuilder(URI.create(strRemoteFileUrl))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            final HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            final String lastModified = response.headers()
                    .firstValue("Last-Modified")
                    .orElse("");
            if (!lastModified.isBlank() ) {
                fileProperties.put("Last Modified", TimingClass.ConversionSubClass.convertTimeFormat(lastModified, DateTimeFormatter.RFC_1123_DATE_TIME, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            }
            fileProperties.put("Size", response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(-1L));
        } catch (IOException e) {
            final String strFeedback = String.format("Input/Output Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        } catch (InterruptedException ei) {
            final String strFeedback = String.format("Execution was interrupted... %s", Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
        final String fileChecksum = getRemoteFileContent(strRemoteFileUrl + ".sha256");
        if (!fileChecksum.isBlank() ) {
            fileProperties.put("Checksum SHA-256", fileChecksum.trim().toLowerCase(Locale.ENGLISH));
        }
        return fileProperties;
    }

    /**
     * capture remote file content
     * @param strRemoteFileUrl URL for remote file
     * @return content file as String
     */
    public static String getRemoteFileContent(final String strRemoteFileUrl) {
        String fileContent = "";
        try (HttpClient client = HttpClient.newHttpClient()) {
            fileContent = client.send(
                    HttpRequest.newBuilder(URI.create(strRemoteFileUrl)).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            ).body();
        } catch (IOException e) {
             final String strFeedback = String.format("Input/Output Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
             LogExposureClass.LOGGER.error(strFeedback);
         } catch (InterruptedException ei) {
             final String strFeedback = String.format("Execution was interrupted... %s", Arrays.toString(ei.getStackTrace()));
             LogExposureClass.LOGGER.warn(strFeedback);
             /* Clean up whatever needs to be handled before interrupting  */
             Thread.currentThread().interrupt();
         }
        return fileContent;
    }

    /**
     * parse Doc from Input Stream
     * @param inStream Input Stream
     * @param docBuilderFactory DocumentBuilderFactory
     * @return Document
     */
    private static Document parseDocumentFromInputStram(final InputStream inStream, final DocumentBuilderFactory docBuilderFactory) {
        Document doc = null;
        try {
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            doc = docBuilder.parse(inStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            final String strFeedback = String.format("Exception while attempting to read remote XML from an URL... %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return doc;
    }

    // Private constructor to prevent instantiation
    private RemoteInformationRetrievalClass() {
        // intentionally blank
    }

}

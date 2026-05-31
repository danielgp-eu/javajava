package javajava;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * XML management
 */
public final class ExtensibleMarkupLanguageClass {

    /**
     * building Central Maven Repository as URL
     * @param inPackage input Maven package
     * @return URL to Central Maven Repository
     */
    private static URL buildMavenCentralRepositoryUniformResourceLocatorFromPackage(final String inPackage) {
        final String strWebSite = RegularExpressionsClass.buildCentralMavenRepositoryUniformResourceLocator(inPackage) + "maven-metadata.xml";
        final String strFeedback = String.format("Uniform Resource Locator from Central Maven Repository for %s package is: %s", inPackage, strWebSite);
        LogExposureClass.LOGGER.error(strFeedback);
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
            final String strFeedback = String.format("ParserConfigurationException Exception while attempting to read remote XML from an URL as %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return doc;
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
    private ExtensibleMarkupLanguageClass() {
        // intentionally blank
    }

}

package javajava;
/* Input/Output classes */
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
/* Utility classes */
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
/* XML classes */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/* for XML exception */
import org.xml.sax.SAXException;
/* W3C DOM classes */
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Capturing details of dependencies from current Maven POM file
 */
public final class DependenciesClass {
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(DependenciesClass.class);
    /**
     * current class path
     */
    /* default */ final static String classPath = System.getProperty("java.class.path");

    /**
     * Gets a complete list of dependencies as JSON string
     * 
     * @return String
     */
    public static String getCurrentDependencies() {
        final String strDependencyFile = getDependencyFile();
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final Document doc = getDocumentWithDependencies(strDependencyFile);
        final NodeList nodeList = doc.getElementsByTagName("dependency");
        final int intListSize = nodeList.getLength();
        for (int i = 0; i < intListSize; ++i) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element tElement = (Element)node;
                arrayAttributes.put(tElement.getElementsByTagName("groupId").item(0).getTextContent()
                    + "/"
                    + tElement.getElementsByTagName("artifactId").item(0).getTextContent()
                    , tElement.getElementsByTagName("version").item(0).getTextContent());
            }
        }
        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileDependencyDetailsSuccess"), strDependencyFile);
        LOGGER.debug(strFeedback);
        return Common.getMapIntoJsonString(arrayAttributes);
    }

    /**
     * get Dependency file
     * @return String
     */
    private static String getDependencyFile() {
        FileHandlingClass.loadProjectFolder();
        String strDependencyFile = FileHandlingClass.APP_FOLDER + "/pom.xml";
        if (!classPath.contains(";")) {
            strDependencyFile = "META-INF/maven/com.compliance.central/compliance-snowflake/pom.xml";
        }
        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileDependencyIdentified"), strDependencyFile);
        LOGGER.debug(strFeedback);
        return strDependencyFile;
    }

    /**
     * Dependencies Document
     * @param strDependencyFile string with dependency details
     * @return Document
     */
    private static Document getDocumentWithDependencies(final String strDependencyFile) {
        Document doc = null; // NOPMD by Daniel Popiniuc on 30.04.2025, 02:18
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            if (classPath.contains(";")) {
                doc = docBuilder.parse(new File(strDependencyFile));
                doc.getDocumentElement().normalize();
            } else {
                try (InputStream xmlContent = FileHandlingClass.getIncludedFileContentIntoInputStream(strDependencyFile)) {
                    LOGGER.debug(xmlContent);
                    doc = docBuilder.parse(xmlContent);
                } catch (IOException e) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentError"), strDependencyFile, Arrays.toString(e.getStackTrace()));
                    LOGGER.error(strFeedback);
                }
            }
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentError"), strDependencyFile, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return doc;
    }

    // Private constructor to prevent instantiation
    private DependenciesClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

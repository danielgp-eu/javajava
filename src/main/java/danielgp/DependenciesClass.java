package danielgp;
/* Input/Output classes */
import java.io.File;
import java.io.IOException;
/* Utility classes */
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
/* XML classes */
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/* LOGGing classes */
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
     * Gets a complete list of dependencies as JSON string
     * 
     * @return String
     */
    public static String getCurrentDependencies() {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        FileHandlingClass.loadProjectFolder();
        final String strDependencyFile = FileHandlingClass.strProjectFolder + File.separator + "pom.xml";
        String strFeedback = String.format("Will get dependency details from %s file", strDependencyFile);
        LOGGER.debug(strFeedback);
        final File fileDependency = new File(strDependencyFile);
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try { 
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder(); 
            final Document doc = docBuilder.parse(fileDependency); 
            doc.getDocumentElement().normalize(); 
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
            strFeedback = String.format("Dependency details from %s file were sucessfully captured!", strDependencyFile);
            LOGGER.debug(strFeedback);
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            strFeedback = String.format("Error encountered... %s", ex.getStackTrace().toString());
            LOGGER.error(strFeedback);
        }
        return Common.getMapIntoJsonString(arrayAttributes);
    }

    // Private constructor to prevent instantiation
    private DependenciesClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

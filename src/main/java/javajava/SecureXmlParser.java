package javajava;
/* I/O classes */
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
/* XML classes */
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
/* DOM class */
import org.w3c.dom.Document;
/* XML Exception class */
import org.xml.sax.SAXException;

/**
 * Secured XML parser
 */
public final class SecureXmlParser {

    /**
     * Get Feature by Name
     * @param strFeatureName
     * @return String
     */
    private static String getFeatureByName(final String strFeatureName) {
        return switch (strFeatureName) {
            // **1. Disable DTDs completely (Strongest protection)**
            // This is the most effective way to prevent XXE, as DTDs are the primary
            // mechanism for external entities. If your application doesn't rely on DTDs,
            // this is the recommended approach.
            case "disallow-doctype-decl" -> "http://apache.org/xml/features/disallow-doctype-decl";
            // **2. Disable external general entities**
            // This prevents the parser from processing external general entities.
            case "external-general-entities" -> "http://xml.org/sax/features/external-general-entities";
            // **3. Disable external parameter entities**
            // This prevents the parser from processing external parameter entities.
            case "external-parameter-entities" -> "http://xml.org/sax/features/external-parameter-entities";
            // **4. Set secure processing feature (Java 1.6+ recommended)**
            // This instructs the parser to process XML securely and limit the use of
            // external resources. It's a good general-purpose security setting.
            case "secure-processing" -> XMLConstants.FEATURE_SECURE_PROCESSING;
            // mandatory default
            default -> "unknown";
        };
    }

    /**
     * Parsing XML safely
     * @param strDependencyFile name of Dependency file
     * @return Document
     * @throws ParserConfigurationException Error Exception possible 
     * @throws SAXException Error Exception possible
     * @throws IOException Error Exception possible
     */
    public static Document parseXmlSafely(final String strDependencyFile)
            throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        final List<String> listFeatures = Arrays.asList("disallow-doctype-decl", "external-general-entities", "external-parameter-entities", "secure-processing");
        listFeatures.forEach(strFeature -> {
            boolean bolSettingType = true;
            if (strFeature.startsWith("external-")) {
                bolSettingType = false;
            }
            try {
                docBuilderFactory.setFeature(getFeatureByName(strFeature), bolSettingType);
            } catch (ParserConfigurationException e) {
                // Handle the exception if the feature is not supported by the parser
                final String strFeedback = String.format("Parser does not support the feature %s... %s", strFeature, e.getMessage());
                Common.levelProvider.logError(strFeedback);
                // Fallback to other protective measures if this isn't supported
            }
        });
        // **5. Disable XInclude (if not needed)**
        // XInclude allows embedding XML documents within others. If your application
        // doesn't use XInclude, disable it as it can also be a vector for XXE.
        docBuilderFactory.setXIncludeAware(false);
        // **6. Disable entity expansion (for older parsers or specific needs)**
        // This prevents the parser from expanding entity references. While the above
        // features are generally more comprehensive, this can be a fallback.
        docBuilderFactory.setExpandEntityReferences(false);
        // instantiate the Document Builder
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        // If you are providing your own EntityResolver, ensure it's secure
        docBuilder.setEntityResolver(new org.xml.sax.helpers.DefaultHandler());
        // Securely configured docBuilder
        return docBuilder.parse(new File(strDependencyFile));
    }

    /**
     * Constructor
     */
    private SecureXmlParser() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}
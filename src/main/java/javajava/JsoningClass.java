package javajava;
/* Jackson classes for fast JSON handling */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/* I/O classes */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
/* Utility classes */
import java.util.*;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * JSON handling
 */
public class JsoningClass { // NOPMD by Daniel Popiniuc on 17.04.2025, 16:28
    /**
     * pointer for all logs
     */
    protected static final Logger LOGGER = LogManager.getLogger(JsoningClass.class);

    /**
     * Load all JSON nodes from String
     * 
     * @param strJson Input stream as source
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final InputStream strJson) {
        JsonNode jsonRootNode = null;
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRootNode = objectMapper.readTree(strJson);
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONstringLoaded"), strJson);
            LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONloadErrorInputStream"), strJson, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return jsonRootNode;
    }

    /**
     * Load all JSON nodes from main configuration file
     * 
     * @param jsonFile file with expected JSON content
     * @return JsonNode
     */
    @SuppressWarnings("unused") // NOPMD by Daniel Popiniuc on 02.06.2025, 00:13
    public static JsonNode getJsonFileNodes(final File jsonFile) {
        JsonNode jsonRootNode = null;
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRootNode = objectMapper.readTree(jsonFile);
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONstringLoaded"), jsonFile.toString());
            LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONloadErrorFile"), jsonFile.toString(), Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return jsonRootNode;
    }

    /**
     * get Sub-node from Tree
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return JsonNode
     */
    protected static JsonNode getJsonNodeFromTree(final JsonNode givenJsonNode, final String strJsonNodeName) {
        String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchAttempt"), strJsonNodeName);
        LOGGER.debug(strFeedback);
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchNotFound"), strJsonNodeName, givenJsonNode);
            LOGGER.error(strFeedback);
        } else {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFound"), givenJsonNode);
            LOGGER.debug(strFeedback);
        }
        return jsonNode;
    }

    /**
     * Node into List of Properties
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return List of Properties
     */
    @SuppressWarnings("unused")
    public static List<Properties> getJsonNodeNameListOfProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<Properties> listProperties = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(arrayElement->{
                final Properties properties = new Properties();
                for (final Map.Entry<String, JsonNode> entry : arrayElement.properties()) {
                    properties.put(entry.getKey(), entry.getValue());
                }
                listProperties.add(properties);
            });
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFoundX"), "List of Properties", strJsonNodeName, listProperties);
            LOGGER.debug(strFeedback);
        }
        return listProperties;
    }

    /**
     * get list of String from a JSON node
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return List of String
     */
    @SuppressWarnings("unused")
    public static List<String> getJsonNodeNameListOfStrings(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<String> listStrings = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(jsonSingleNode-> listStrings.add(jsonSingleNode.asText()));
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFoundX"), "List of Strings", strJsonNodeName, listStrings);
            LOGGER.debug(strFeedback);
        }
        return listStrings;
    }

    /**
     * Properties from a JSON node 
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return Properties
     */
    @SuppressWarnings("unused")
    public static Properties getJsonNodeNameProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        final Properties properties = new Properties();
        for (final Map.Entry<String, JsonNode> entry : jsonNode.properties()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFoundX"), "Properties", strJsonNodeName, properties);
        LOGGER.debug(strFeedback);
        return properties;
    }

    /**
     * Single value from a JSON node
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNode name to search
     * @return String
     */
    public static String getJsonValue(final JsonNode givenJsonNode, final String strJsonNode) {
        return getJsonNodeFromTree(givenJsonNode, strJsonNode).asText();
    }

    /**
     * Constructor
     */
    protected JsoningClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

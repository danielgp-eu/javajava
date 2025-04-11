package danielgp;
/* I/O classes */
import java.io.File;
import java.io.IOException;
/* Utility classes */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
/* Jackson classes for fast JSON handling */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON handling
 */
public class JsoningClass {

    /**
     * Load all JSON nodes from main configuration file
     * 
     * @param jsonFile
     * @return JsonNode
     */
    protected static JsonNode getJsonFileNodes(final File jsonFile) {
        JsonNode jsonRootNode = null;
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            jsonRootNode = objectMapper.readTree(jsonFile);
            final String strFeedback = String.format("JSON file %s loaded...", jsonFile.toString());
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format("Error encountered when attempting to load \"%s\" JSON file... %s", jsonFile.toString(), ex.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return jsonRootNode;
    }

    /**
     * get Sub-node from Tree
     * 
     * @param givenJsonNode
     * @param strJsonNodeName
     * @return JsonNode
     */
    private static JsonNode getJsonNodeFromTree(final JsonNode givenJsonNode, final String strJsonNodeName) {
        String strFeedback = String.format("Will attempt to search for node named \"%s\"...", givenJsonNode);
        LogHandlingClass.LOGGER.debug(strFeedback);
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            strFeedback = String.format("Relevant node \"%s\" was NOT found within \"%s\"...", strJsonNodeName, givenJsonNode.toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        } else {
            strFeedback = String.format("Relevant node \"%s\" was found...", givenJsonNode);
            LogHandlingClass.LOGGER.debug(strFeedback);
        }
        return jsonNode;
    }

    /**
     * node capture into Properties
     * 
     * @param givenJsonNode
     * @param strJsonNodeName
     * @return Properties
     */
    public static Properties getJsonNodeNameIntoProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final Properties properties = new Properties();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            final Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
            fields.forEachRemaining(field->{
                properties.put(field.getKey(), field.getValue());
            });
            final String strFeedback = String.format("For node \"%s\" we found following List of Properties: %s", strJsonNodeName, properties.toString());
            LogHandlingClass.LOGGER.debug(strFeedback);
        }
        return properties;
    }

    /**
     * Node into List of Properties
     * 
     * @param givenJsonNode
     * @param strJsonNodeName
     * @return List<Properties>
     */
    public static List<Properties> getJsonNodeNameListOfProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<Properties> listProperties = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(arrayElement->{
                final Properties properties = new Properties();
                final Iterator<Map.Entry<String, JsonNode>> fields = arrayElement.fields();
                fields.forEachRemaining(field->{ properties.put(field.getKey(), field.getValue()); });
                listProperties.add(properties);
            });
            final String strFeedback = String.format("For node \"%s\" we found following List of Properties: %s", strJsonNodeName, listProperties.toString());
            LogHandlingClass.LOGGER.debug(strFeedback);
        }
        return listProperties;
    }

    /**
     * get list of String from a JSON node
     * 
     * @param givenJsonNode
     * @param strJsonNodeName
     * @return List<String>
     */
    protected static List<String> getJsonNodeNameListOfStrings(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<String> listStrings = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(jsonSingleNode->{
                listStrings.add(jsonSingleNode.asText());
            });
            final String strFeedback = String.format("For node \"%s\" we found following List of Strings: %s", strJsonNodeName, listStrings.toString());
            LogHandlingClass.LOGGER.debug(strFeedback);
        }
        return listStrings;
    }

    /**
     * Properties from a JSON node 
     * 
     * @param givenJsonNode
     * @param strJsonNodeName
     * @return Properties
     */
    protected static Properties getJsonNodeNameProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        final Properties properties = new Properties();
        final Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        fields.forEachRemaining(field -> {
            properties.put(field.getKey(), field.getValue());
        });
        final String strFeedback = String.format("For node \"%s\" following Properties were found %s", strJsonNodeName, properties.toString());
        LogHandlingClass.LOGGER.debug(strFeedback);
        return properties;
    }

    /**
     * Single value from a JSON node
     * 
     * @param givenJsonNode
     * @param strJsonNode
     * @return String
     */
    public static String getJsonValue(final JsonNode givenJsonNode, final String strJsonNode) {
        return getJsonNodeFromTree(givenJsonNode, strJsonNode).asText();
    }

    // constructor
    protected JsoningClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

package javajava;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * JSON handling
 */
public class JsoningClass { // NOPMD by Daniel Popiniuc on 17.04.2025, 16:28

    /**
     * Load all JSON nodes from String
     * 
     * @param strJson Input stream as source
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final InputStream strJson) {
        JsonNode jsonRootNode;
        final ObjectMapper objectMapper = new ObjectMapper();
        jsonRootNode = objectMapper.readTree(strJson);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONstringLoaded"), strJson);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return jsonRootNode;
    }

    /**
     * Load all JSON nodes from main configuration file
     * 
     * @param jsonFile file with expected JSON content
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final File jsonFile) {
        JsonNode jsonRootNode;
        final ObjectMapper objectMapper = new ObjectMapper();
        jsonRootNode = objectMapper.readTree(jsonFile);
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONstringLoaded"), jsonFile.toString());
            LoggerLevelProvider.LOGGER.debug(strFeedback);
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
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchAttempt"), strJsonNodeName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchNotFound"), strJsonNodeName, givenJsonNode);
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        } else {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFound"), strJsonNodeName);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
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
            setNodeRetrievingToDebugLog("List of Properties", strJsonNodeName, listProperties);
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
    public static List<String> getJsonNodeNameListOfStrings(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final List<String> listStrings = new ArrayList<>();
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        if(!jsonNode.isEmpty()) {
            jsonNode.forEach(jsonSingleNode-> listStrings.add(jsonSingleNode.asString()));
            setNodeRetrievingToDebugLog("List of Strings", strJsonNodeName, listStrings);
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
    public static Properties getJsonNodeNameProperties(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final JsonNode jsonNode = getJsonNodeFromTree(givenJsonNode, strJsonNodeName);
        final Properties properties = new Properties();
        if(!jsonNode.isEmpty()) {
            for (final Map.Entry<String, JsonNode> entry : jsonNode.properties()) {
                properties.put(entry.getKey(), entry.getValue());
            }
            setNodeRetrievingToDebugLog("Properties", strJsonNodeName, properties);
        }
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
        return getJsonNodeFromTree(givenJsonNode, strJsonNode).asString();
    }

    /**
     * Logging node retrieval activity to Debug Log
     * @param strWhat meaning of search
     * @param strJsonNodeName JSON node to look into
     * @param objValues values found
     */
    private static void setNodeRetrievingToDebugLog(final String strWhat, final String strJsonNodeName, final Object objValues) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nJSONnodeSearchFoundX"), strWhat, strJsonNodeName, objValues);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
    }

    /**
     * Constructor
     */
    protected JsoningClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

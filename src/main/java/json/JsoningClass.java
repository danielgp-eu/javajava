package json;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import structure.StringManipulationClass;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * JSON handling
 */
public final class JsoningClass {

    /**
     * Load all JSON nodes from String
     * 
     * @param strJson Input stream as source
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final InputStream strJson) {
        final JsonNode jsonRootNode;
        final ObjectMapper objectMapper = new ObjectMapper();
        jsonRootNode = objectMapper.readTree(strJson);
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONstringLoaded"), strJson);
        LogExposureClass.LOGGER.debug(strFeedback);
        return jsonRootNode;
    }

    /**
     * Load all JSON nodes from main configuration file
     * 
     * @param jsonFile file with expected JSON content
     * @return JsonNode
     */
    public static JsonNode getJsonFileNodes(final File jsonFile) {
        final JsonNode jsonRootNode;
        final ObjectMapper objectMapper = new ObjectMapper();
        jsonRootNode = objectMapper.readTree(jsonFile);
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONstringLoaded"), jsonFile.toString());
        LogExposureClass.LOGGER.debug(strFeedback);
        return jsonRootNode;
    }

    /**
     * Build a pair of Key and Value for JSON
     * @param strKey Key to be used
     * @param objValue Value to be used
     * @return String with a pair of key and value
     */
    public static String getJsonKeyAndValue(final String strKey, final Object objValue) {
        final List<String> unquotedValues = Arrays.asList("null", "true", "false");
        final boolean needsQuotesAround = 
            (objValue instanceof Integer)
            || (objValue instanceof Double)
            || (objValue.toString().startsWith("[") && objValue.toString().endsWith("]"))
            || (objValue.toString().startsWith("{") && objValue.toString().endsWith("}"))
            || StringManipulationClass.isStringActuallyNumeric(objValue.toString())
            || StringManipulationClass.hasMatchingSubstring(objValue.toString(), unquotedValues);
        String strRaw = "\"%s\":\"%s\"";
        if (needsQuotesAround) {
            strRaw = "\"%s\":%s";
        }
        return String.format(strRaw, strKey, objValue);
    }

    /**
     * get Sub-node from Tree
     * 
     * @param givenJsonNode JSON node
     * @param strJsonNodeName name to search
     * @return JsonNode
     */
    private static JsonNode getJsonNodeFromTree(final JsonNode givenJsonNode, final String strJsonNodeName) {
        final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONnodeSearchAttempt"), strJsonNodeName);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONnodeSearchNotFound"), strJsonNodeName, givenJsonNode);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONnodeSearchFound"), strJsonNodeName);
            LogExposureClass.LOGGER.debug(strFeedback);
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
     * Cycle inside Map and build a JSON string out of it
     *
     * @param arrayAttrib array with attribute values
     * @return String
     */
    public static String getMapIntoJsonString(final Map<String, Object> arrayAttrib) {
        final StringBuilder strJsonSubString = new StringBuilder(100);
        arrayAttrib.forEach((strKey, objValue) -> {
            if (!strJsonSubString.isEmpty()) {
                strJsonSubString.append(',');
            }
            strJsonSubString.append(getJsonKeyAndValue(strKey, objValue));
        });
        return String.format("{%s}", strJsonSubString);
    }

    /**
     * Logging node retrieval activity to Debug Log
     * @param strWhat meaning of search
     * @param strJsonNodeName JSON node to look into
     * @param objValues values found
     */
    private static void setNodeRetrievingToDebugLog(final String strWhat, final String strJsonNodeName, final Object objValues) {
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nJSONnodeSearchFoundX"), strWhat, strJsonNodeName, objValues);
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * Constructor
     */
    private JsoningClass() {
        // intentionally left blank
    }
}

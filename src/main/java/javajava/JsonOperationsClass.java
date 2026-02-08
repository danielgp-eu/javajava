package javajava;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * JSON handling
 */
public final class JsonOperationsClass {

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
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nJSONstringLoaded"), strJson);
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
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nJSONstringLoaded"), jsonFile.toString());
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
            || BasicStructuresClass.StringEvaluationClass.isStringActuallyNumeric(objValue.toString())
            || BasicStructuresClass.StringEvaluationClass.hasMatchingSubstring(objValue.toString(), unquotedValues);
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
        final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nJSONnodeSearchAttempt"), strJsonNodeName);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        final JsonNode jsonNode = givenJsonNode.at(strJsonNodeName);
        if (jsonNode.isMissingNode()) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nJSONnodeSearchNotFound"), strJsonNodeName, givenJsonNode);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nJSONnodeSearchFound"), strJsonNodeName);
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
        final Map<String, Object> sortedMap = BasicStructuresClass.ListAndMapClass.sortMapByKey(arrayAttrib);
        sortedMap.forEach((strKey, objValue) -> {
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
        final String strFeedback = String.format(LocalizationClass.getMessage("i18nJSONnodeSearchFoundX"), strWhat, strJsonNodeName, objValues);
        LogExposureClass.LOGGER.debug(strFeedback);
    }

    /**
     * JSON array evaluate and split if too big
     */
    public static final class JsonArrayClass {
        /**
         * Minimum value length for bucketing
         */
        /* default */ private static final int MIN_BUCKET_LENGTH = 4;
        /**
         * Length for bucketing customizable
         */
        private static int intBucketLength;
        /**
         * variable for relevant field
         */
        private static String strRelevantField;
        /**
         * variable for input JSON file
         */
        private static String strInputJsonFile;
        /**
         * variable for destination folder
         */
        private static String strOutFolder;
        /**
         * Writer for JSON content
         */
        private static Writer writer;

        /**
         * Buckets values
         * @param inOriginalValue given original value
         * @return String
         */
        private static String bucketFieldValue(final String inOriginalValue) {
            final String usedValue;
            final int bucketLength;
            if (intBucketLength == 0) {
                bucketLength = MIN_BUCKET_LENGTH;
            } else {
                bucketLength = intBucketLength;
            }
            final int lengthValue = inOriginalValue.length();
            if (bucketLength == -1 || lengthValue < bucketLength) {
                usedValue = inOriginalValue;
            } else if (lengthValue == bucketLength) {
                usedValue = "x".repeat(bucketLength);
            } else {
                usedValue = inOriginalValue.substring(0, lengthValue - bucketLength)
                        + "x".repeat(bucketLength);
            }
            return usedValue;
        }

        /**
         * Bucketing destination file name based on suffix
         * @param strSuffix input JSON file name
         * @return Path
         */
        public static String buildDestinationFileName(final String strSuffix) {
            return strInputJsonFile.substring(
                    strInputJsonFile.lastIndexOf(File.separator) + 1,
                    strInputJsonFile.lastIndexOf('.'))
                    + "__" + strRelevantField + "_"
                    + bucketFieldValue(strSuffix)
                    + strInputJsonFile.substring(strInputJsonFile.lastIndexOf('.'));
        }

        /**
         * Bucketing destination full file name based on suffix
         * @param strSuffix input JSON file name
         * @return Path
         */
        private static Path buildDestinationFullFileName(final String strSuffix) {
            return Paths.get(strOutFolder).resolve(buildDestinationFileName(strSuffix));
        }

        /**
         * checks if JSON file is of an Array type
         * @param jsonParser JSON parser
         */
        private static void checkIfJsonFileIsOfArrayType(final JsonParser jsonParser) {
            // Expect: [ { ... }, { ... }, ... ]
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                final String strFeedback = "Root must be a JSON array but is not";
                LogExposureClass.LOGGER.error(strFeedback);
                throw new IllegalStateException(strFeedback);
            }
        }

        /**
         * closes Current file w. feedback
         * @param crtFile file name to close
         * @param writer file pointer to close
         * @param recordCounter records written
         */
        private static void closeCurrentFile(final Path crtFile, final Writer writer, final long recordCounter) {
            try {
                writer.write(']');
                writer.close();
                if (crtFile != null) {
                    final String strFeedback = String.format("I just closed file %s after %s records", crtFile, recordCounter);
                    LogExposureClass.LOGGER.info(strFeedback);
                }
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * getting Value and Record
         * @param jsonParser big JSON file handler
         * @param jsonFactory content
         * @return splitSize
         */
        private static Properties getValueAndRecord(final JsonParser jsonParser, final JsonFactory jsonFactory) {
            final Properties properties = new Properties();
            final ByteArrayOutputStream tempBuffer = new ByteArrayOutputStream();
            final ObjectWriteContext writeContext = ObjectWriteContext.empty();
            try (JsonGenerator tempGen = jsonFactory.createGenerator(writeContext, tempBuffer)) {
                int depth = 1;
                tempGen.copyCurrentEvent(jsonParser);
                while (depth > 0) {
                    final JsonToken crtRecord = jsonParser.nextToken();
                    tempGen.copyCurrentEvent(jsonParser);
                    if (crtRecord == JsonToken.PROPERTY_NAME && jsonParser.currentName().equals(strRelevantField)) {
                        jsonParser.nextToken(); // move to value
                        final String origValue = jsonParser.getValueAsString();
                        properties.put("Value", bucketFieldValue(origValue));
                        tempGen.copyCurrentEvent(jsonParser);
                    }
                    if (crtRecord == JsonToken.START_OBJECT || crtRecord == JsonToken.START_ARRAY) { depth++; }
                    if (crtRecord == JsonToken.END_OBJECT || crtRecord == JsonToken.END_ARRAY) {
                        tempGen.writeRaw(System.lineSeparator());
                        depth--;
                    }
                }
            }
            properties.put("Record", tempBuffer.toString(StandardCharsets.UTF_8));
            return properties;
        }

        /**
         * Split a JSON files into smaller pieces
         */
        public static void splitJsonIntoSmallerGrouped() {
            final JsonFactory jsonFactory = JsonFactory.builder().build();
            final ObjectReadContext readContext = ObjectReadContext.empty();
            FileOperationsClass.MassChangeClass.setSearchingFolder(strOutFolder); // used for Mass Change (if necessary)
            final String strFeedbackTemp = String.format("JSON file named %s will be split into smaller pieces...", strInputJsonFile);
            LogExposureClass.LOGGER.debug(strFeedbackTemp);
            String remeberedValue = null;
            long recordCounter = 0;
            // initiate JSON parsing
            try (JsonParser jsonParser = jsonFactory.createParser(readContext, Path.of(strInputJsonFile))) {
                checkIfJsonFileIsOfArrayType(jsonParser);
                Path crtFile = null;
                // Iterate through each object in the array
                while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                    // Buffer the object so we can inspect the field
                    final Properties objectProperties = getValueAndRecord(jsonParser, jsonFactory);
                    final String crtBucketValue = objectProperties.getProperty("Value");
                    final String strRecord = objectProperties.getProperty("Record");
                    // Start a new output file if the field value changed
                    if (!crtBucketValue.equals(remeberedValue)) {
                        if (writer != null) {
                            closeCurrentFile(crtFile, writer, recordCounter);
                            recordCounter = 0;
                        }
                        final Path outFile = buildDestinationFullFileName(crtBucketValue);
                        crtFile = outFile;
                        writeObjectStart(outFile);
                        remeberedValue = crtBucketValue;
                    }
                    // Write buffered object to the current output file
                    writeValueToNewBufferedWriter(recordCounter, writer, strRecord);
                    recordCounter++;
                }
                // Close the last writer
                if (writer != null) {
                    closeCurrentFile(crtFile, writer, recordCounter);
                }
            } catch(JacksonException ej) {
                final String strFeedback = String.format("Jackson exception on... %s", Arrays.toString(ej.getStackTrace()));
                LogExposureClass.LOGGER.debug(strFeedback);
            }
        }

        /**
         * Setter for intBucketLength
         * @param inBucketLength bucket length
         */
        public static void setBucketLength(final int inBucketLength) {
            intBucketLength = inBucketLength;
        }

        /**
         * Setter for strDestinationFolder
         * @param inOutFolder destination folder
         */
        public static void setDestinationFolder(final String inOutFolder) {
            strOutFolder = inOutFolder;
        }

        /**
         * Setter for strInputJsonFile
         * @param inJsonFile input JSON file
         */
        public static void setInputJsonFile(final String inJsonFile) {
            strInputJsonFile = inJsonFile;
        }

        /**
         * Setter for strRelevantField
         * @param inRelevantField relevant
         */
        public static void setRelevantField(final String inRelevantField) {
            strRelevantField = inRelevantField;
        }

        /**
         * Write object start
         * @param outFile smaller file
         */
        private static void writeObjectStart(final Path outFile) {
            boolean isFileNew = true;
            if (Files.exists(outFile)) {
                FileOperationsClass.MassChangeClass.setOldContent("]");
                FileOperationsClass.MassChangeClass.setNewContent(",");
                FileOperationsClass.MassChangeClass.setPattern(outFile.getFileName().toString());
                FileOperationsClass.MassChangeClass.massChangeToFilesWithinFolder();
                isFileNew = false;
            }
            try {
                writer = Files.newBufferedWriter(outFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                if (isFileNew) {
                    writer.write("[");
                    writer.write(System.lineSeparator());
                }
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Write value to newBufferedWriter
         * @param recordCounter important to know if preceding , is needed or not
         * @param writer NewBufferedWriter handle
         * @param strBuffer current record
         */
        private static void writeValueToNewBufferedWriter(final long recordCounter, final Writer writer, final String strBuffer) {
            try {
                if (recordCounter > 0) {
                    writer.write(",");
                }
                writer.write(strBuffer);
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Constructor
         */
        private JsonArrayClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private JsonOperationsClass() {
        // intentionally left blank
    }
}

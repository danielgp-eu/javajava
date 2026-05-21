package javajava;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;

import org.apache.maven.model.Model;

/**
 * HTML generating logic
 */
public final class HtmlClass {

    /**
     * Application Details
     * @return Content
     */
    public static gg.jte.Content buildApplicationDetail() {
        final Model prjModel = ProjectClass.getProjectModel();
        final String appDetails = String.format("%s&trade; v.%s &copy; by %s", prjModel.getName(), prjModel.getVersion(), prjModel.getDevelopers().getFirst().getName());
        final String strFeedback = String.format("I have just build application details: %s", appDetails);
        LogExposureClass.LOGGER.info(strFeedback);
        return output -> output.writeContent(appDetails);
    }

    /**
     * Geographical Coordinates from TZ
     * @return String
     */
    public static String buildGeographicalCoordinatesFromTimeZone(final String sessionTimeZone) {
        final ZoneInfoRecord zInfo = ZoneDataServiceClass.get(sessionTimeZone);
        return zInfo.latitude() + "," + zInfo.longitude();
    }

    /**
     * Building Time-Zone select
     * @return Content
     */
    public static gg.jte.Content buildMenu(final SequencedMap<String, Map<String, String>> inMapMenu) {
        final StringBuilder strMenuContent = new StringBuilder(1000);
        inMapMenu.forEach((strKey, mapValue) -> {
            if (!mapValue.getOrDefault(BasicStructuresClass.STR_MENU, "").isEmpty()) {
                strMenuContent.append(String.format("<li><a href=\"?page=%s\"><i class=\"%s\"></i>%s</a></li>", strKey, mapValue.get(BasicStructuresClass.STR_ICON), mapValue.get(BasicStructuresClass.STR_MENU)));
            }
        });
        return output -> output.writeContent(strMenuContent.toString());
    }

    /**
     * Building Time-Zone select
     * @return Content
     */
    public static gg.jte.Content buildTimeZoneSelect(final String inTimeZone) {
        final SequencedMap<String, String> sortedTimeZones = ZoneDataServiceClass.loadSupportedTimeZones();
        final Properties selectProps = new Properties();
        selectProps.put("Name", "TZ");
        selectProps.put("Id", "TZ");
        selectProps.put("Default", inTimeZone);
        return output -> output.writeContent(SelectInputSubClass.buildSelectInput(sortedTimeZones, selectProps));
    }

    /**
     * List and Maps management
     */
    public static final class SelectInputSubClass {
        /**
         * Variable for Defaults
         */
        private static List<String> defaults = new ArrayList<>();
        /**
         * Variable for Additional Attributes
         */
        private static String additionalAttrib = "";

        /**
         * build Label as HTML tag 
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String buildLabelTag(final Properties objFeatures) {
            final String strLabel = objFeatures.getOrDefault("Label", "").toString()
                    + (objFeatures.getOrDefault(BasicStructuresClass.STR_MULTIPLE, "").toString().isEmpty() ? "" : "<sup>(multiple values possible)</sup>");
            final String tagLabelRaw = "<label for=\"%s\"%s>%s:</label>";
            final String strLabelStyle = objFeatures.getOrDefault("Label Style", "").toString().isEmpty() ? "" : "style=\"" + objFeatures.get("Label Style").toString() + "\"";
            return String.format(tagLabelRaw, objFeatures.get("Id"), strLabelStyle, strLabel)
                    + (objFeatures.getOrDefault("Label on Same Line", "").toString().isEmpty() ? "<br/>" : "");
        }

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        public static String buildSelectInput(final SequencedMap<String, String> mapValues, final Properties objFeatures) {
            final List<String> outHtml = new ArrayList<>();
            if (!objFeatures.getOrDefault("Label", "").toString().isEmpty()) {
                outHtml.add(buildLabelTag(objFeatures));
            }
            manageAdditionalAttribuesAndDefaults(objFeatures);
            outHtml.add(String.format("<select name=\"%s\" id=\"%s\"%s>", objFeatures.get("Name"), objFeatures.get("Id"), additionalAttrib));
            mapValues.forEach((strValue, strText) -> {
                String strSelected = "";
                if (!defaults.isEmpty()
                        && defaults.contains(strValue)) {
                    strSelected = " selected";
                }
                outHtml.add(String.format("<option value=\"%s\"%s>%s</option>", strValue, strSelected, strText));
            });
            outHtml.add("</select>");
            return String.join("", outHtml);
        }

        private static void manageAdditionalAttribuesAndDefaults(final Properties objFeatures) {
            final String defaultValue = objFeatures.getOrDefault("Default", "").toString();
            String[] defaultVals = {defaultValue};
            if (!objFeatures.getOrDefault(BasicStructuresClass.STR_MULTIPLE, "").toString().isEmpty()) {
                additionalAttrib = String.format(" multiple size=\"%s\"", objFeatures.get(BasicStructuresClass.STR_MULTIPLE));
                if (!defaultValue.isEmpty()) {
                    defaultVals = defaultValue.split(",");
                }
            }
            defaults = Arrays.asList(defaultVals);
        }

        /**
         * constructor
         */
        private SelectInputSubClass() {
            // intentionally left blank
        }

    }

    /**
     * List and Maps management
     */
    public static final class TableSubClass {
        /**
         * CSS to align text to right
         */
        private static final String CSS_TEXT_RIGHT = "text-align:right;";
        /**
         * variable for Current Tab value
         */
        private static String currentTabValue;
        /**
         * variable for HTML Table
         */
        private static List<String> htmlTableLines = new ArrayList<>();
        /**
         * Time Zone variable
         */
        private static final long LARGE_STRING = 25;
        /**
         * variable for Remember Key
         */
        private static String rememberKey;
        /**
         * variable for row counter
         */
        private static int rowCounter;
        /**
         * variable for Table Header
         */
        private static String strTableHeader = "";
        /**
         * Time Zone variable
         */
        private static String strTimeZone;
        /**
         * variable for Counter usage
         */
        private static boolean useCounter;

        /**
         * Generate HTML from a Map of values
         * @param inList values stored as a list
         * @return String
         */
        public static String getListOfSequencedMapIntoHtmlTable(final List<SequencedMap<Object, Object>> inList, final Properties objFeatures) {
            if (strTimeZone == null) {
                setTimeZone(System.getProperty("user.timezone"));
            }
            htmlTableLines.clear();
            strTableHeader = "";
            rememberKey = getRememberKey(objFeatures);
            useCounter = !objFeatures.getOrDefault("Counter", "").toString().isEmpty();
            for (final SequencedMap<Object, Object> recordMap : inList) {
                processRecord(recordMap);
            }
            finish();
            return String.join("", htmlTableLines);
        }

        /**
         * final
         * @return String
         */
        public static void finish() {
            if (!strTableHeader.isEmpty()) {
                htmlTableLines.add("</tbody></table>");
                if (!rememberKey.isEmpty()) {
                    htmlTableLines.add(String.format("</div><!-- %s --></div><!-- tabStandard -->", currentTabValue));
                }
            }
        }

        /**
         * establishing the Key to Remember if relevant
         * @param objFeatures optional HTML Table features
         * @return String
         */
        private static String getRememberKey(final Properties objFeatures) {
            String strRememberKey = "";
            if (objFeatures.containsKey(BasicStructuresClass.STR_NEW_TAB)) {
                strRememberKey = objFeatures.get(BasicStructuresClass.STR_NEW_TAB).toString();
            }
            return strRememberKey;
        }

        /**
         * handle Tab switch
         * @param recordMap properties of the record to be transformed into HTML row
         */
        private static void handleTabSwitch(final SequencedMap<Object, Object> recordMap) {
            final Object valObj = recordMap.get(rememberKey);
            final String valueForTab = valObj == null ? "null" : valObj.toString();
            final String prev = currentTabValue == null ? "" : currentTabValue;
            if (!valueForTab.equalsIgnoreCase(prev)) {
                if (htmlTableLines.isEmpty()) {
                    // first tab: open tab container
                    htmlTableLines.add("<div id=\"tabStandard\" class=\"tabber\">");
                } else if (currentTabValue != null) {
                    // close previous tab's table
                    htmlTableLines.add(String.format("</tbody></table></div><!-- %s -->", currentTabValue));
                }
                // open new tab with header
                htmlTableLines.add(String.format("<div class=\"tabbertab\" title=\"%s\">%s", valueForTab, strTableHeader));
                currentTabValue = valueForTab;
                rowCounter = 0;
            }
        }

        /**
         * process each record
         * @param recordMap map with record content
         */
        private static void processRecord(final SequencedMap<Object, Object> recordMap) {
            HeaderSubSubClass.ensureHeaderExists(recordMap);
            if (rememberKey.isEmpty()) {
                HeaderSubSubClass.ensureHeaderAppended();
            } else {
                handleTabSwitch(recordMap);
            }
            if (useCounter) {
                rowCounter++;
                recordMap.put("#", String.valueOf(rowCounter));
            }
            htmlTableLines.add(RowSubSubClass.buildTableBodyRow(recordMap));
        }

        /**
         * Setter for strTimeZone
         * @param inTimeZone input time zone
         */
        public static void setTimeZone(final String inTimeZone) {
            strTimeZone = inTimeZone;
            TimingClass.LocalizationSubClass.setOutputTimeZone(inTimeZone);
        }

        /**
         * Rows logic
         */
        public static final class RowSubSubClass {

            /**
             * Table Body row logic
             * @param recordMap properties of the record to be transformed into HTML row
             * @return String
             */
            private static String buildTableBodyRow(final SequencedMap<Object, Object> recordMap) {
                final StringBuilder strTableRow = new StringBuilder(1000);
                strTableRow.append("<tr>");
                recordMap.forEach((strKey, objValue) -> {
                    if (!rememberKey.equalsIgnoreCase(strKey.toString())
                            && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                        final StringBuilder cellStyle = new StringBuilder(100);
                        if (recordMap.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                            cellStyle.append(recordMap.get(BasicStructuresClass.STR_ROW_STYLE).toString());
                        }
                        final Map<String, String> mapSmartLogic = manageCellStyleAndValue(objValue);
                        final String strValue = mapSmartLogic.get("value");
                        if (!mapSmartLogic.get(BasicStructuresClass.STR_STYLE).isEmpty()) {
                            cellStyle.append(mapSmartLogic.get(BasicStructuresClass.STR_STYLE));
                        }
                        if (cellStyle.isEmpty()) {
                            strTableRow.append(String.format("<td>%s</td>", strValue));
                        } else {
                            strTableRow.append(String.format("<td style=\"%s\">%s</td>", cellStyle, strValue));
                        }
                    }
                });
                strTableRow.append("</tr>");
                return strTableRow.toString();
            }

            /**
             * Manage Cell Style and Value
             * @param inValue input value
             * @return Map
             */
            private static Map<String, String> manageCellStyleAndValue(final Object inValue) {
                String cellStyle = "";
                String strValue = inValue.toString();
                if (BasicStructuresClass.STR_NULL.equalsIgnoreCase(strValue)) {
                    cellStyle = "color:LightGrey;font-style:italic;";
                    strValue = "&lt;NULL&gt;";
                } else if (strValue.isBlank()) {
                    cellStyle = "color:Grey;font-style:italic;";
                    strValue = "&lt;blank&gt;";
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDecimal(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,.2f", new BigDecimal(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyInteger(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,d", Integer.parseInt(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyLong(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = String.format(Locale.US, "%,d", Long.parseLong(strValue));
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyDate(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.formatDateFriendly(strValue, "yyyy-MM-dd", "EEE, dd MMM yyyy");
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestamp(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.convertTimestampFriendly(strValue, "yyyy-MM-dd HH:mm:ss", "EEE, dd MMM yyyy HH:mm:ss");
                } else if (BasicStructuresClass.StringEvaluationSubClass.isStringActuallyTimestampWithMilliseconds(strValue)) {
                    cellStyle = CSS_TEXT_RIGHT;
                    strValue = TimingClass.LocalizationSubClass.convertTimestampFriendly(strValue, "yyyy-MM-dd HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss.SSS");
                } else if (strValue.length() >= LARGE_STRING) {
                    strValue = TimingClass.LocalizationSubClass.replacePatterns(strValue);
                }
                return Map.of(
                        BasicStructuresClass.STR_STYLE, cellStyle,
                        "value", strValue);
            }

            /**
             * constructor
             */
            private RowSubSubClass() {
                // intentionally left blank
            }

        }

        /**
         * Rows logic
         */
        public static final class HeaderSubSubClass {

            /**
             * Table Body row logic
             * @param recordMap properties of the record to be transformed into HTML row
             * @return String
             */
            private static String buildTableHeader(final SequencedMap<Object, Object> recordMap) {
                final StringBuilder strBuilder = new StringBuilder(100);
                strBuilder.append("<table><thead>");
                recordMap.forEach((strKey, _) -> {
                    if (!rememberKey.equalsIgnoreCase(strKey.toString())
                            && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                        strBuilder.append(String.format("<th>%s</th>", strKey));
                    }
                });
                if (useCounter) {
                    strBuilder.append("<th>#</th>");
                }
                strBuilder.append("</thead><tbody>");
                return strBuilder.toString();
            }

            /**
             * ensuring Table Header is appended
             */
            private static void ensureHeaderAppended() {
                if (htmlTableLines.isEmpty()) {
                    htmlTableLines.add(strTableHeader);
                    rowCounter = 0;
                }
            }

            /**
             * initiating Table Header
             * @param recordMap records to parse
             */
            private static void ensureHeaderExists(final SequencedMap<Object, Object> recordMap) {
                if (strTableHeader.isEmpty()) {
                    strTableHeader = buildTableHeader(recordMap);
                }
            }

            /**
             * constructor
             */
            private HeaderSubSubClass() {
                // intentionally left blank
            }

        }

        /**
         * constructor
         */
        private TableSubClass() {
            // intentionally left blank
        }

    }

    /**
     * constructor
     */
    private HtmlClass() {
        // intentionally left blank
    }

}

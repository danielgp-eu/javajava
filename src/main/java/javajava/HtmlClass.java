package javajava;

import java.math.BigDecimal;
import java.util.ArrayList;
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
     * establishing the Key to Remember if relevant
     * @param objFeatures optional HTML Table features
     * @return String
     */
    public static String buildSelectInput(final SequencedMap<String, String> mapValues, final Properties objFeatures) {
        final List<String> outHtml = new ArrayList<>();
        if (!objFeatures.getOrDefault("Label", "").toString().isEmpty()) {
            if (objFeatures.getOrDefault("Label Style", "").toString().isEmpty()) {
                outHtml.add(String.format("<label for=\"%s\">%s:</label>", objFeatures.get("Id"), objFeatures.get("Label")));
            } else {
                outHtml.add(String.format("<label for=\"%s\" style=\"%s\">%s:</label>", objFeatures.get("Id"), objFeatures.get("Label Style").toString(), objFeatures.get("Label")));
            }
            if (objFeatures.getOrDefault("Label on Same Line", "").toString().isEmpty()) {
                outHtml.add("<br/>");
            }
        }
        outHtml.add(String.format("<select name=\"%s\" id=\"%s\">", objFeatures.get("Name"), objFeatures.get("Id")));
        mapValues.forEach((strValue, strText) -> {
            String strSelected = "";
            if (strValue.equalsIgnoreCase(objFeatures.getOrDefault("Default", "None").toString())) {
                strSelected = " selected";
            }
            outHtml.add(String.format("<option value=\"%s\"%s>%s</option>", strValue, strSelected, strText));
        });
        outHtml.add("</select>");
        return String.join("", outHtml);
    }

    /**
     * Outputs file statistics into an HTML table
     * @return String
     */
    public static String getEnvironmentDetailsAsHtmlTable() {
        final Properties objFeatures = new Properties();
        objFeatures.put(BasicStructuresClass.STR_NEW_TAB, "Category");
        final List<Properties> envDetails = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        final List<String> desiredOrder = List.of("Category", "Element", "Value");
        final List<SequencedMap<Object, Object>> orderedList = envDetails.stream()
                .map(prop -> BasicStructuresClass.ListAndMapClass.sortProperties(prop, desiredOrder))
                .toList();
        return Table.getListOfSequencedMapIntoHtmlTable(orderedList, objFeatures);
    }

    /**
     * Outputs table statistics into an HTML table
     * @return String
     */
    public static String getTableStatisticsAsHtmlTable() {
        final StringBuilder strQueryRaw = new StringBuilder(1000);
        final String strQuery = """
SELECT
      "m"."name"                                                                AS "Table"
    , IFNULL("q"."seq", 0)                                                      AS "Sequence"
FROM
    "sqlite_master"                                                             AS "m"
    LEFT JOIN "sqlite_sequence"                                                 AS "q"  ON
        "m"."name" = "q"."name"
WHERE
        "m"."type" = 'table'
    AND "m"."name" NOT LIKE 'sqlite_%';
""";
        final List<Properties> resultTables = DatabaseOperationsClass.SpecificSqLiteClass.getSqLiteResultSetValues("Table list and their sequence", strQuery);
        resultTables.forEach(objProperty -> {
            if (!strQueryRaw.isEmpty()) {
                strQueryRaw.append(" UNION ALL ");
            }
            strQueryRaw.append(String.format("""
        SELECT
              '%s'                                                              AS "Table"
            , COUNT(*)                                                          AS "Records"
            , %s                                                                AS "Sequence"
        FROM
            "%s"
""", objProperty.get(BasicStructuresClass.STR_TABLE), objProperty.get("Sequence"), objProperty.get(BasicStructuresClass.STR_TABLE)));
        });
        final String strFinalQuery = String.format("""
WITH
    "CTE__Raw"                                                                  AS (
        %s
    )
SELECT
      ROW_NUMBER () OVER (ORDER BY "Table")                                     AS "#"
    , "Table"                                                                   AS "Table"
    , "Records"                                                                 AS "Records"
    , "Sequence"                                                                AS "Sequence"
    , 'color:'
        || CASE
            WHEN "Sequence" = "Records" THEN
                'green'
            WHEN "Sequence" = 0         THEN
                'blue'
            ELSE
                'red'
            END || ';'                                                          AS "RowStyle"
    , "Sequence" - "Records"                                                    AS "Gap"
FROM
    "CTE__Raw";
""", strQueryRaw);
        final List<Properties> resultTableStats = DatabaseOperationsClass.SpecificSqLiteClass.getSqLiteResultSetValues("Table Statistics", strFinalQuery);
        final List<String> desiredOrder = List.of("#", BasicStructuresClass.STR_TABLE, "Records", "Sequence", "Gap");
        final List<SequencedMap<Object, Object>> orderedList = resultTableStats.stream()
                .map(prop -> BasicStructuresClass.ListAndMapClass.sortProperties(prop, desiredOrder))
                .toList();
        return Table.getListOfSequencedMapIntoHtmlTable(orderedList, new Properties());
    }

    /**
     * Common Web Elements
     */
    public static final class CommonWebElements {

        /**
         * Application Details
         * @return Content
         */
        public static gg.jte.Content buildApplicationDetail() {
            final Model prjModel = ProjectClass.getProjectModel();
            final gg.jte.Content appContent = output -> output.writeContent(String.format("%s&trade; v.%s &copy; by %s", prjModel.getName(), prjModel.getVersion(), prjModel.getDevelopers().getFirst().getName()));
            final String strFeedback = String.format("I have just build application details: %s", appContent);
            LogExposureClass.LOGGER.info(strFeedback);
            return appContent;
        }

        /**
         * Current Time-stamp from TZ
         * @return String
         */
        public static String buildCurrentTimestamp(final String sessionTimeZone) {
            return TimingClass.getCurrentTimestamp("EEE, dd MMM yyyy HH:mm:ss", sessionTimeZone);
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
            return output -> output.writeContent(buildSelectInput(sortedTimeZones, selectProps));
        }

        // Private constructor to prevent instantiation
        private CommonWebElements() {
            // intentional empty
        }

    }

    /**
     * List and Maps management
     */
    public static final class Table {
        /**
         * Time Zone variable
         */
        private static final long LARGE_STRING = 25;
        /**
         * CSS to align text to right
         */
        private static final String CSS_TEXT_RIGHT = "text-align:right;";
        /**
         * Time Zone variable
         */
        private static String strTimeZone;
        /**
         * Row Counter variable
         */
        private static int rowCounter;

        /**
         * Table Body row logic
         * @param strRememberKey value of Key remembered
         * @param recordProperties properties of the record to be transformed into HTML row
         * @return String
         */
        private static String buildTableBodyRow(final String strRememberKey, final SequencedMap<Object, Object> recordProperties) {
            final StringBuilder strHtmlTable = new StringBuilder(1000);
            strHtmlTable.append("<tr>");
            recordProperties.forEach((strKey, objValue) -> {
                if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                        && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                    final StringBuilder cellStyle = new StringBuilder(100);
                    if (recordProperties.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                        cellStyle.append(recordProperties.get(BasicStructuresClass.STR_ROW_STYLE).toString());
                    }
                    final Map<String, String> mapSmartLogic = manageCellStyleAndValue(objValue);
                    final String strValue = mapSmartLogic.get("value");
                    if (!mapSmartLogic.get("style").isEmpty()) {
                        cellStyle.append(mapSmartLogic.get("style"));
                    }
                    if (cellStyle.isEmpty()) {
                        strHtmlTable.append(String.format("<td>%s</td>", strValue));
                    } else {
                        strHtmlTable.append(String.format("<td style=\"%s\">%s</td>", cellStyle, strValue));
                    }
                }
            });
            strHtmlTable.append("</tr>");
            return strHtmlTable.toString();
        }

        /**
         * Generate HTML from a Map of values
         * @param inList values stored as a list
         * @return String
         */
        public static String getListOfSequencedMapIntoHtmlTable(final List<SequencedMap<Object, Object>> inList, final Properties objFeatures) {
            final StringBuilder strHeaderTable = new StringBuilder(100);
            final StringBuilder strHtmlTable = new StringBuilder(1000);
            if (strTimeZone == null) {
                setTimeZone(System.getProperty("user.timezone"));
            }
            final String strRememberKey = getRememberKey(objFeatures);
            final boolean bolCounter = !objFeatures.getOrDefault("Counter", "").toString().isEmpty();
            final String[] strRememberValue = { "None" };
            inList.forEach( recordProperties -> {
                if (strHeaderTable.isEmpty()) {
                    strHeaderTable.append("<table><thead>");
                    recordProperties.forEach((strKey, _) -> {
                        if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                                && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                            strHeaderTable.append(String.format("<th>%s</th>", strKey));
                        }
                    });
                    if (bolCounter) {
                        strHeaderTable.append("<th>#</th>");
                    }
                    strHeaderTable.append("</thead><tbody>");
                }
                if (strRememberKey.isEmpty()) {
                    if (strHtmlTable.isEmpty()) {
                        strHtmlTable.append(strHeaderTable);
                        rowCounter = 0;
                    }
                } else {
                    final String crtValueForTab = recordProperties.get(strRememberKey).toString();
                    if (!strRememberValue[0].equalsIgnoreCase(crtValueForTab)) {
                        if (strHtmlTable.isEmpty()) {
                            strHtmlTable.append("<div id=\"tabStandard\" class=\"tabber\">");
                        } else {
                            strHtmlTable.append(String.format("</tbody></table></div><!-- %s -->", crtValueForTab));
                        }
                        strHtmlTable.append(String.format("<div class=\"tabbertab\" title=\"%s\">%s", crtValueForTab, strHeaderTable));
                        strRememberValue[0] = crtValueForTab;
                        rowCounter = 0;
                    }
                }
                if (bolCounter) {
                    rowCounter++;
                    recordProperties.put("#", String.valueOf(rowCounter));
                }
                strHtmlTable.append(buildTableBodyRow(strRememberKey, recordProperties));
            });
            strHtmlTable.append("</tbody></table>");
            if (!strRememberKey.isEmpty()) {
                strHtmlTable.append(String.format("</div><!-- %s --></div><!-- tabStandard -->", strRememberValue[0]));
            }
            return strHtmlTable.toString();
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
            } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyDecimal(strValue)) {
                cellStyle = CSS_TEXT_RIGHT;
                strValue = String.format(Locale.US, "%,.2f", new BigDecimal(strValue));
            } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyInteger(strValue)) {
                cellStyle = CSS_TEXT_RIGHT;
                strValue = String.format(Locale.US, "%,d", Integer.parseInt(strValue));
            } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyDate(strValue)) {
                cellStyle = CSS_TEXT_RIGHT;
                strValue = TimingClass.Localization.formatDateFriendly(strValue, "yyyy-MM-dd", "EEE, dd MMM yyyy");
            } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyTimestamp(strValue)) {
                cellStyle = CSS_TEXT_RIGHT;
                strValue = TimingClass.Localization.convertTimestampFriendly(strValue, "yyyy-MM-dd HH:mm:ss", "EEE, dd MMM yyyy HH:mm:ss");
            } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyTimestampWithMilliseconds(strValue)) {
                cellStyle = CSS_TEXT_RIGHT;
                strValue = TimingClass.Localization.convertTimestampFriendly(strValue, "yyyy-MM-dd HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss.SSS");
            } else if (strValue.length() >= LARGE_STRING) {
                strValue = TimingClass.Localization.replacePatterns(strValue);
            }
            return Map.of(
                    "style", cellStyle,
                    "value", strValue);
        }

        /**
         * Setter for strTimeZone
         * @param inTimeZone input time zone
         */
        public static void setTimeZone(final String inTimeZone) {
            strTimeZone = inTimeZone;
            TimingClass.Localization.setOutputTimeZone(inTimeZone);
        }

        /**
         * constructor
         */
        private Table() {
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

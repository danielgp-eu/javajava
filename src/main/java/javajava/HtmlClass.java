package javajava;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.SequencedMap;

/**
 * HTML generating logic
 */
public final class HtmlClass {

    /**
     * establishing the Key to Remember if relevant
     * @param objFeatures optional HTML Table features
     * @return String
     */
    public static String buildSelectInput(final Map<String, String> mapValues, final Properties objFeatures) {
        final List<String> outHtml = new ArrayList<>();
        if (!objFeatures.getOrDefault("Label", "").toString().isEmpty()) {
            outHtml.add(String.format("<label for=\"%s\">%s:</label>", objFeatures.get("Id"), objFeatures.get("Label")));
            if (objFeatures.getOrDefault("SameLineLabel", "").toString().isEmpty()) {
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
     * Outputs table statistics into a HTML table
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
    , '<div style="text-align:right;color:'
        || CASE
            WHEN "Sequence" = "Records" THEN
                'green'
            WHEN "Sequence" = 0         THEN
                'blue'
            ELSE
                'red'
            END
        || '">'
        || ("Sequence" - "Records")
        || '</div>'                                                             AS "Gap"
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
     * List and Maps management
     */
    public static final class Table {
        /**
         * Time Zone variable
         */
        private static long LARGE_STRING = 25;
        /**
         * CSS to align text to right
         */
        private static String CSS_TEXT_RIGHT = "text-align:right;";
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
            recordProperties.forEach((strKey, strValue) -> {
                if (!strRememberKey.equalsIgnoreCase(strKey.toString())
                        && !BasicStructuresClass.STR_ROW_STYLE.equalsIgnoreCase(strKey.toString())) {
                    final StringBuilder cellStyle = new StringBuilder(100);
                    if (BasicStructuresClass.STR_NULL.equalsIgnoreCase(strValue.toString())) {
                        cellStyle.append("color:LightGrey;font-style:italic;");
                        strValue = "&lt;NULL&gt;";
                    } else if (strValue.toString().isBlank()) {
                        cellStyle.append("color:Grey;font-style:italic;");
                        strValue = "&lt;blank&gt;";
                    } else {
                        if (recordProperties.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                            cellStyle.append(recordProperties.get(BasicStructuresClass.STR_ROW_STYLE).toString());
                        }
                        if (BasicStructuresClass.StringEvaluationClass.isStringActuallyDecimal(strValue.toString())) {
                            cellStyle.append(CSS_TEXT_RIGHT);
                            strValue = String.format(Locale.US, "%,.2f", new BigDecimal(strValue.toString()));
                        } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyInteger(strValue.toString())) {
                            cellStyle.append(CSS_TEXT_RIGHT);
                            strValue = String.format(Locale.US, "%,d", Integer.parseInt(strValue.toString()));
                        } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyDate(strValue.toString())) {
                            cellStyle.append(CSS_TEXT_RIGHT);
                            strValue = TimingClass.Localization.formatDateFriendly(strValue.toString(), "yyyy-MM-dd", "EEE, dd MMM yyyy");
                        } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyTimestamp(strValue.toString())) {
                            cellStyle.append(CSS_TEXT_RIGHT);
                            strValue = TimingClass.Localization.convertTimestampFriendly(strValue.toString(), "yyyy-MM-dd HH:mm:ss", "EEE, dd MMM yyyy HH:mm:ss");
                        } else if (BasicStructuresClass.StringEvaluationClass.isStringActuallyTimestampWithMilliseconds(strValue.toString())) {
                            cellStyle.append(CSS_TEXT_RIGHT);
                            strValue = TimingClass.Localization.convertTimestampFriendly(strValue.toString(), "yyyy-MM-dd HH:mm:ss.SSS", "EEE, dd MMM yyyy HH:mm:ss.SSS");
                        } else if (strValue.toString().length() >= LARGE_STRING) {
                            strValue = TimingClass.Localization.replacePatterns(strValue.toString());
                        }
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

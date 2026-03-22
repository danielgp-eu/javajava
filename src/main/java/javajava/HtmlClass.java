package javajava;

import java.util.ArrayList;
import java.util.List;
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
        outHtml.add(String.format("<label for=\"%s\">%s:</label>", objFeatures.get("Id"), objFeatures.get("Label")));
        outHtml.add("<br/>");
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
     * List and Maps management
     */
    public static final class Table {
        /**
         * Row Counter variable
         */
        /* default */ static int rowCounter;

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
                    String cellStyle = "";
                    if (recordProperties.containsKey(BasicStructuresClass.STR_ROW_STYLE)) {
                        cellStyle = recordProperties.get(BasicStructuresClass.STR_ROW_STYLE).toString();
                    }
                    if (BasicStructuresClass.StringEvaluationClass.isStringActuallyInteger(strValue.toString())) {
                        cellStyle = cellStyle + "text-align:right;";
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

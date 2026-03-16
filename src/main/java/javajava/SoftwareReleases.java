package javajava;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Handling Software releases logic
 */
final public class SoftwareReleases {
    /**
     * Internal database name
     */
    private static final String RELEASES_DATABASE = "C:\\www\\Data\\GitRepositories\\GitHub\\danielgp\\PHP\\control_center\\source\\SoftwareReleases\\config\\configuration.sqlite";

    /**
     * friendly Aging logic
     * @param agingDays 
     * @return String aging
     */
    private static String calculateAging(final String agingDays) {
        String strAging = "";
        if (!agingDays.isEmpty()) {
            final int intAging = Integer.parseInt(agingDays);
            strAging = switch(intAging) {
                case 0 -> "TODAY";
                case 1 -> "YESTERDAY";
                case 2 -> "the day before yesterday";
                default -> agingDays + " days ago";
            };
        }
        return strAging;
    }

    /**
     * expose Software Release details from internal DB
     * @return List software releases details
     */
    public static List<Properties> consolidateSoftwareReleases() {
        final List<Properties> softwareReleases = new ArrayList<>();
        final List<Properties> resultReleases = getSoftwareReleasesFromDatabase();
        resultReleases.forEach( recordProperties -> {
            final Properties newProperties = new Properties();
            newProperties.put("Organization", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("OrganizationName"), recordProperties.get("OrganizationId")));
            newProperties.put("Product", String.format("<a href=\"%s\" target=\"_blank\"><span style=\"float:left;\">%s<br/>[%s]</span><span style=\"float:right;text-align:right;\">%s<br/>[%s]</span></a>", recordProperties.get("Releases"), recordProperties.get("ProductName"), recordProperties.get("ProductId"), recordProperties.get("BranchName"), recordProperties.get("BranchId")));
            newProperties.put("Version", String.format("%s<div style=\"text-align:right;\">[%s]</div>", recordProperties.get("Latest release version"), recordProperties.get("VersionId")));
            final String agingDays = recordProperties.get("Latest release aging").toString().replaceAll("\\.0", ""); 
            newProperties.put("Date", String.format("%s<br>==> %s", recordProperties.get("Latest release date"), calculateAging(agingDays)));
            newProperties.put("Files", String.format("%s [%s]<br/>==> %s [%s]", recordProperties.get("File Kit Name"), recordProperties.get("File Kit Id"), recordProperties.get("File Installed Name"), recordProperties.get("File Installed Id")));
            newProperties.put("Profile", recordProperties.get("Profile Name"));
            newProperties.put(BasicStructuresClass.STR_ROW_STYLE, establishRowStyle(agingDays));
            softwareReleases.add(newProperties);
        });
        return softwareReleases;
    }

    /**
     * Row Style logic
     * @param agingDays 
     * @return String row style
     */
    private static String establishRowStyle(final String agingDays) {
        String strRowColor = "#fff";
        if (!agingDays.isEmpty()) {
            final long[] longRanges = {14, 30, 90};
            final long longAging = Long.parseLong(agingDays);
            if (longAging <= longRanges[0]) {
                strRowColor = "#51ff6d";
            } else if (longAging <= longRanges[1]) {
                strRowColor = "#ccffe8";
            } else if (longAging <= longRanges[2]) {
                strRowColor = "#fdffcc";
            }
        }
        return String.format("background-color:%s;", strRowColor);
    }

    /**
     * expose Software Release details from internal DB
     * @return List software releases details
     */
    private static List<Properties> getSoftwareReleasesFromDatabase() {
        List<Properties> resultReleases = new ArrayList<>();
        try (Connection objConnection = DatabaseOperationsClass.SpecificSqLiteClass.getSqLiteConnection(RELEASES_DATABASE);
                Statement objStatement = DatabaseOperationsClass.ConnectivityClass.createSqlStatement(BasicStructuresClass.STR_SQLITE, objConnection);) {
            final Properties rsProperties = new Properties();
            rsProperties.put("Which", "Software Releases"); // purpose
            rsProperties.put("QueryToUse", DatabaseOperationsClass.getPreDefinedQuery(BasicStructuresClass.STR_SQLITE, "ListProductBranches"));
            rsProperties.put("Kind", "Values");
            final Properties queryProperties = new Properties();
            resultReleases = DatabaseOperationsClass.ResultSettingClass.getResultSetStandardized(objStatement, rsProperties, queryProperties);
        } catch (SQLException e) {
            final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nSQLconnectionCreationFailedLight"), BasicStructuresClass.STR_SQLITE, e.getLocalizedMessage());
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
        return resultReleases;
    }

    // Private constructor to prevent instantiation
    private SoftwareReleases() {
        // intentional empty
    }

}

package file;

import java.io.File;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.logging.log4j.Level;

import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;

/**
 * Locate files of folders class
 */
public class FileLocatingClass {
    /**
     * String constant Minified
     */
    private static final String STR_MINIFIED = "Minified";
    /**
     * String constant PrettyPrint
     */
    private static final String STR_PRTY_PRNT = "PrettyPrint";

    /**
     * Checking if a file exists and is readable
     * 
     * @param strFileName file name
     * @return Properties
     */
    public static Properties checkFileExistanceAndReadability(final String strFileName) {
        final Properties propertiesReturn = new Properties();
        if (strFileName == null) {
            propertiesReturn.put("NULL_FILE_NAME", JavaJavaLocalization.getMessage("i18nFileDoesNotExist"));
        } else {
            final File fileGiven = new File(strFileName);
            if (fileGiven.exists()) {
                if (fileGiven.isFile()) {
                    if (fileGiven.canRead()) {
                        propertiesReturn.put("OK", strFileName);
                    } else {
                        propertiesReturn.put("NOT_READABLE", String.format(JavaJavaLocalization.getMessage("i18nFileUnreadable"), strFileName));
                    }
                } else {
                    propertiesReturn.put("NOT_A_FILE", String.format(JavaJavaLocalization.getMessage("i18nFileNotAfile"), strFileName));
                }
            } else {
                propertiesReturn.put("DOES_NOT_EXIST", String.format(JavaJavaLocalization.getMessage("i18nFileDoesNotExist"), strFileName));
            }
        }
        return propertiesReturn;
    }


    /**
     * read Main configuration file
     * @param strFilePattern file pattern to use
     * @return String
     */
    public static String getJsonConfigurationFile(final String strFilePattern) {
        final Properties propsFile = new Properties();
        propsFile.put(STR_MINIFIED, String.format(strFilePattern, ".min"));
        propsFile.put(STR_PRTY_PRNT, String.format(strFilePattern, ""));
        String strFileJson = null;
        final Properties propsMinified = checkFileExistanceAndReadability(propsFile.getProperty(STR_MINIFIED));
        for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
            final boolean isItOk = "OK".equals(eMinified.getKey());
            if (isItOk) {
                strFileJson = eMinified.getValue().toString();
            } else {
                final Properties propsPreety = checkFileExistanceAndReadability(propsFile.getProperty(STR_PRTY_PRNT));
                for(final Entry<Object, Object> ePreety : propsPreety.entrySet()) {
                    final boolean isItOk2 = "OK".equals(ePreety.getKey());
                    getJsonFileName(isItOk2, ePreety, propsFile);
                }
            }
        }
        return strFileJson;
    }

    /**
     * Getting JSON file name
     * @param isItOk2 file check result
     * @param ePreety file name
     * @param propsFile file Properties
     * @return String
     */
    private static String getJsonFileName(final boolean isItOk2, final Entry<Object, Object> ePreety, final Properties propsFile) {
        final String strFileJson;
        if (isItOk2) {
            strFileJson = ePreety.getValue().toString();
        } else {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileConfigurationNotFound")
                , propsFile.getProperty(STR_MINIFIED, "")
                , propsFile.getProperty(STR_PRTY_PRNT, ""));
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
            throw new IllegalArgumentException(strFeedback);
        }
        return strFileJson;
    }
}

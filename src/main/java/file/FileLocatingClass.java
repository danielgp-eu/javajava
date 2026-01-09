package file;

import localization.JavaJavaLocalizationClass;
import log.LogExposure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Locate files of folders class
 */
public final class FileLocatingClass {
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
            propertiesReturn.put("NULL_FILE_NAME", JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"));
        } else {
            final File fileGiven = new File(strFileName);
            if (fileGiven.exists()) {
                if (fileGiven.isFile()) {
                    if (fileGiven.canRead()) {
                        propertiesReturn.put("OK", strFileName);
                    } else {
                        propertiesReturn.put("NOT_READABLE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileUnreadable"), strFileName));
                    }
                } else {
                    propertiesReturn.put("NOT_A_FILE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileNotAfile"), strFileName));
                }
            } else {
                propertiesReturn.put("DOES_NOT_EXIST", String.format(JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"), strFileName));
            }
        }
        return propertiesReturn;
    }

    /**
     * A simple record to hold our results
     */
    /* default */ record FolderStats(long fileCount, long folderCount, long totalSize) {
        /* default */ static FolderStats empty() { return new FolderStats(0, 0, 0); }
        /* default */ FolderStats add(final FolderStats other) {
            return new FolderStats(
                this.fileCount + other.fileCount,
                this.folderCount + other.folderCount,
                this.totalSize + other.totalSize
            );
        }
    }

    /**
     * get Folder statistics recursively
     * @param strFolderName folder name
     * @param pathProps path properties
     * @return Properties
     */
    public static Properties getFolderStatisticsRecursive(final String strFolderName, final Properties pathProps) {
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (Stream<Path> stream = Files.walk(directory)) {
            final FolderStats stats = stream
                .map(path -> {
                    if (Files.isDirectory(path)) {
                        // Don't count the root directory itself as a sub-folder
                        return path.equals(directory) ? FolderStats.empty() : new FolderStats(0, 1, 0);
                    } else {
                        try {
                            return new FolderStats(1, 0, Files.size(path));
                        } catch (IOException e) {
                            return FolderStats.empty();
                        }
                    }
                })
                .reduce(FolderStats.empty(), FolderStats::add);
            pathProps.put("TOTAL_OBJECTS", stats.folderCount() + stats.fileCount());
            pathProps.put("DIRECTORIES", stats.folderCount());
            pathProps.put("FILES", stats.fileCount());
            pathProps.put("SIZE_BYTES", stats.totalSize());
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), strFolderName, Arrays.toString(ex.getStackTrace())));
        }
        return pathProps;
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
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileConfigurationNotFound")
                , propsFile.getProperty(STR_MINIFIED, "")
                , propsFile.getProperty(STR_PRTY_PRNT, ""));
            LogExposure.exposeMessageToErrorLog(strFeedback);
            throw new IllegalArgumentException(strFeedback);
        }
        return strFileJson;
    }

    /**
     * Constructor
     */
    private FileLocatingClass() {
        // intentionally blank
    }

}

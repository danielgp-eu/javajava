package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

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
     * Checking if a file exists and is readable
     * @param fileSize file size
     * @param strFileName file name
     * @return Properties
     */
    public static Properties checkFileExistanceAndReadability(final long fileSize, final String strFileName) {
        final Properties propertiesReturn = new Properties();
        switch(String.valueOf(fileSize)) {
            case "-1":
                propertiesReturn.put("NOT_READABLE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileUnreadable"), strFileName));
                break;
            case "-2":
                propertiesReturn.put("NOT_A_FILE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileNotAfile"), strFileName));
                break;
            case "-3":
                propertiesReturn.put("DOES_NOT_EXIST", String.format(JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"), strFileName));
                break;
            case "-99":
                propertiesReturn.put("NULL_FILE_NAME", JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"));
                break;
            default:
                propertiesReturn.put("OK", strFileName);
                break;
        }
        return propertiesReturn;
    }

    /**
     * Checking if a file exists and is readable
     * @param strFileName file name
     * @return Properties
     */
    public static Properties checkFileExistanceAndReadability(final String strFileName) {
        final long fileSize = getFileSizeIfFileExistsAndIsReadable(strFileName);
        return checkFileExistanceAndReadability(fileSize, strFileName);
    }

    /**
     * Deletes all files matching given pattern from folder
     * @param strFolder input folder
     * @param strPattern input pattern
     */
    public static void deleteFilesMathingPatternFromFolder(final String strFolder, final String strPattern) {
        try {
            final String strFeedback = String.format("I will attempt to removed all matched files based on %s pattern from folder %s...", strPattern, strFolder);
            LogExposureClass.LOGGER.info(strFeedback);
            final Path dir = Paths.get(strFolder);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().matches(strPattern)) {
                        Files.delete(file);
                        final String strFeedbackD = String.format("File %s has been deleted", file);
                        LogExposureClass.LOGGER.info(strFeedbackD);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ei) {
            final String strFeedbackErr = String.format("Inout/Output exception on... %s", Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * Gets file size if exits and is readable
     * @param strFileName file name
     * @return long
     */
    public static long getFileSizeIfFileExistsAndIsReadable(final String strFileName) {
        final long fileSize;
        if (strFileName == null) {
            fileSize = -99;
        } else {
            final File fileGiven = new File(strFileName);
            if (fileGiven.exists()) {
                if (fileGiven.isFile()) {
                    if (fileGiven.canRead()) {
                        fileSize = fileGiven.length();
                    } else {
                        fileSize = -1;
                    }
                } else {
                    fileSize = -2;
                }
            } else {
                fileSize = -3;
            }
        }
        return fileSize;
    }

    /**
     * get Folder statistics recursively
     * @param strFolderName folder name
     * @param pathProps path properties
     * @return Properties
     */
    public static Properties getFolderStatisticsRecursive(final String strFolderName, final Properties pathProps) {
        final Path directory = Paths.get(strFolderName.replace("\"", ""));
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
                            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nEmptyFolder"), strFolderName, Arrays.toString(e.getStackTrace()));
                            LogExposureClass.LOGGER.debug(strFeedback);
                            return FolderStats.empty();
                        }
                    }
                })
                .reduce(FolderStats.empty(), FolderStats::add);
            pathProps.put("TOTAL_OBJECTS", stats.folderCount() + stats.fileCount());
            pathProps.put("DIRECTORIES", stats.folderCount());
            pathProps.put("FILES", stats.fileCount());
            pathProps.put("SIZE_BYTES", stats.totalSize());
        } catch (IOException ei) {
            final Path foderName = Path.of(strFolderName);
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), foderName.getParent(), foderName.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
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
            LogExposureClass.LOGGER.error(strFeedback);
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

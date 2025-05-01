package javajava;
/* Java IO classes */
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
/* Java NIO classes */
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/* Java util classes */
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * File operation class
 */
public final class FileHandlingClass {
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(FileHandlingClass.class);
    /**
     * Project Folder
     */
    public static String APP_FOLDER; // NOPMD by Daniel Popiniuc on 20.04.2025, 23:29

    /**
     * Checking if a file exists and is readable
     * 
     * @param strFileName
     * @return Properties
     */
    public static Properties checkFileExistanceAndReadability(final String strFileName) {
        final Properties propertiesReturn = new Properties();
        final File fileGiven = new File(strFileName);
        if (fileGiven.exists()) {
            if (fileGiven.isFile()) {
                if (fileGiven.canRead()) {
                    propertiesReturn.put("OK", strFileName);
                } else {
                    propertiesReturn.put("NOT_READABLE", String.format(DanielLocalization.getMessage("i18nFileUnreadable"), strFileName));
                }
            } else {
                propertiesReturn.put("NOT_A_FILE", String.format(DanielLocalization.getMessage("i18nFileNotAfile"), strFileName));
            }
        } else {
            propertiesReturn.put("DOES_NOT_EXIST", String.format(DanielLocalization.getMessage("i18nFileDoesNotExist"), strFileName));
        }
        return propertiesReturn;
    }

    /**
     * Getting current user
     * 
     * @return
     */
    public static File getCurrentUserFolder() {
        return new File(System.getProperty("user.home"));
    }

    /**
     * Get file content into String
     * 
     * @param strFileName
     * @return String
     */
    public static String getFileContentIntoString(final String strFileName) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nFileContentIntoString"), strFileName);
        LOGGER.debug(strFeedback);
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return strReturn;
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName
     * @return
     */
    public static InputStream getIncludedFileContentIntoInputStream(final String strFileName) {
        final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileContentIntoString"), strFileName);
        LOGGER.debug(strFeedback);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); // NOPMD by E303778 on 30.04.2025, 15:47
        final InputStream inputStream = classLoader.getResourceAsStream(strFileName); // NOPMD by E303778 on 30.04.2025, 15:47
        LOGGER.debug("Got entire stream");
        return inputStream;
    }

    /**
     * read Main configuration file
     * 
     * @return String
     */
    public static String getJsonConfigurationFile(final String strFilePattern) {
        final Properties propsFile = new Properties();
        propsFile.put("Minified", String.format(strFilePattern, ".min"));
        propsFile.put("PrettyPrint", String.format(strFilePattern, ""));
        String strFileJson = null;
        final Properties propsMinified = checkFileExistanceAndReadability(propsFile.getProperty("Minified"));
        for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
            final boolean isItOk = "OK".equals(eMinified.getKey());
            if (isItOk) {
                strFileJson = eMinified.getValue().toString();
            } else {
                final Properties propsPreety = checkFileExistanceAndReadability(propsFile.getProperty("PrettyPrint"));
                for(final Entry<Object, Object> ePreety : propsPreety.entrySet()) {
                    final boolean isItOk2 = "OK".equals(ePreety.getKey());
                    if (isItOk2) {
                        strFileJson = ePreety.getValue().toString();
                    } else {
                        final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileConfigurationNotFound"), propsFile.getProperty("Minified"), propsFile.getProperty("PrettyPrint"));
                        throw new IllegalArgumentException(strFeedback);
                    }
                }
            }
        }
        return strFileJson;
    }

    /**
     * Get list of files from a given folder
     * 
     * @param strFolderName
     * @param strExtension
     * @return
     */
    public static List<String> getSpecificFilesFromFolder(final String strFolderName, final String strExtension) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nFileAllCertainOnesFromFolder"), strExtension, strFolderName);
        LOGGER.debug(strFeedback);
        final List<String> arrayFiles = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            // with forEach loop get all the path of files present in directory  
            for (final Path file : stream) {
                if (file.getFileName().toString().endsWith(strExtension)) {
                    final String strFile  = file.getParent().toString()
                            + File.separator + file.getFileName();
                    arrayFiles.add(strFile);
                    strFeedback = String.format(DanielLocalization.getMessage("i18nFileFound"), strExtension, strFile);
                    LOGGER.debug(strFeedback);
                }
            }
        } catch (IOException ex) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nFileFindingError"), strExtension, strFolderName, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return arrayFiles;
    }

    /**
     * Get list of sub-folders from a given folder
     * 
     * @param strFolderName
     * @return
     */
    public static List<String> getSubFolderFromFolder(final String strFolderName) {
        String strFeedback = String.format(DanielLocalization.getMessage("i18nFileSubFoldersAttempt"), strFolderName);
        LOGGER.debug(strFeedback);
        final List<String> arraySubFolders = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    arraySubFolders.add(entry.toString());
                }
            }
        } catch (IOException ex) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
        return arraySubFolders;
    }

    /**
     * Getting current project folder
     */
    public static void loadProjectFolder() {
        if (Objects.isNull(APP_FOLDER)) { 
            final File directory = new File(""); // parameter is empty
            try {
                APP_FOLDER = directory.getCanonicalPath();
            } catch (IOException ex) {
                final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileFolderError"), Arrays.toString(ex.getStackTrace()));
                LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Archives single file to new location
     * 
     * @param strFileName
     * @param strDestFolder
     */
    public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
        try {
            final File strSourceFile = new File(strFileName); 
            final File strDestFile = new File(strDestFolder);
            String strFeedback = String.format(DanielLocalization.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder);
            LOGGER.info(strFeedback);
            org.apache.commons.io.FileUtils.moveFileToDirectory(strSourceFile, strDestFile, true);
            strFeedback = String.format(DanielLocalization.getMessage("i18nFileMoveSuccess"), strFileName, strDestFolder);
            LOGGER.info(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileMoveError"), strFileName, strDestFolder, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
    }

    /**
     * Removes a files if already exists
     * 
     * @param strFileName
     */
    private static void removeFileIfExists(final String strFileName) {
        try {
            final Path filePath = Paths.get(strFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
        }
    }

    /**
     * Write list of single values to File
     * 
     * @param listStrings
     * @param strFileName
     */
    public static void writeListToTextFile(final List<String> listStrings, final String strFileName) {
        removeFileIfExists(strFileName);
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            listStrings.forEach(strLine -> {
                try {
                    bwr.write(strLine);
                    bwr.newLine();
                } catch (IOException er) {
                    final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                    LOGGER.error(strFeedback);
                }
            });
            final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileWritingSuccess"), strFileName);
            LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(DanielLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(ex.getStackTrace()));
            LOGGER.error(strFeedback);
        }
    }

    // Private constructor to prevent instantiation
    private FileHandlingClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}

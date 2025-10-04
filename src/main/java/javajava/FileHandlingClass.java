package javajava;

import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * File operation class
 */
public final class FileHandlingClass {
    /**
     * Project Folder
     */
    public static String strAppFolder; // NOPMD by Daniel Popiniuc on 20.04.2025, 23:29
    /**
     * String constant
     */
    private static final String STR_MINIFIED = "Minified";
    /**
     * String constant
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
     * Getting current user
     * 
     * @return File
     */
    public static File getCurrentUserFolder() {
        return new File(System.getProperty("user.home"));
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

    /**
     * Get list of files from a given folder
     * 
     * @param strFolderName folder name to look into
     * @param strExtension extension to isolate
     * @return List of Strings
     */
    public static List<String> getSpecificFilesFromFolder(final String strFolderName, final String strExtension) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileAllCertainOnesFromFolder"), strExtension, strFolderName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> arrayFiles = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            // with forEach loop get all the path of files present in directory  
            for (final Path file : stream) {
                if (file.getFileName().toString().endsWith(strExtension)) {
                    final String strFile = file.getParent().toString() + File.separator + file.getFileName();
                    arrayFiles.add(strFile);
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileFound"), strExtension, strFile);
                        LoggerLevelProvider.LOGGER.debug(strFeedback);
                    }
                }
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileFindingError"), strExtension, strFolderName, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return arrayFiles;
    }

    /**
     * Get list of sub-folders from a given folder
     * 
     * @param strFolderName folder name to look into
     * @return List of String
     */
    public static List<String> getSubFolderFromFolder(final String strFolderName) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileSubFoldersAttempt"), strFolderName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final List<String> arraySubFolders = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    arraySubFolders.add(entry.toString());
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                        final String strFeedback = entry.toString();
                        LoggerLevelProvider.LOGGER.info(strFeedback);
                    }
                }
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return arraySubFolders;
    }

    /**
     * Getting current project folder
     */
    public static void loadProjectFolder() {
        if (Objects.isNull(strAppFolder)) { 
            final File directory = new File(""); // parameter is empty
            try {
                strAppFolder = directory.getCanonicalPath();
            } catch (IOException ex) {
                if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                    final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileFolderError"), Arrays.toString(ex.getStackTrace()));
                    LoggerLevelProvider.LOGGER.error(strFeedback);
                }
            }
        }
    }

    /**
     * Archives single file to new location
     * 
     * @param strFileName file name in scope for archivation
     * @param strDestFolder destination folder
     */
    public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
        try {
            final File strSourceFile = new File(strFileName); 
            final File strDestFile = new File(strDestFolder);
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder);
                LoggerLevelProvider.LOGGER.info(strFeedback);
            }
            org.apache.commons.io.FileUtils.moveFileToDirectory(strSourceFile, strDestFile, true);
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileMoveSuccess"), strFileName, strDestFolder);
                LoggerLevelProvider.LOGGER.info(strFeedback);
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileMoveError"), strFileName, strDestFolder, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Removes a files if already exists
     * 
     * @param strFileName file name to search
     */
    public static void removeFileIfExists(final String strFileName) {
        try {
            final Path filePath = Paths.get(strFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace()));
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Constructor
     */
    private FileHandlingClass() {
        throw new UnsupportedOperationException(Common.STR_I18N_AP_CL_WN);
    }
}

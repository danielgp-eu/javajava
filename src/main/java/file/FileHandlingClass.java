package file;

import org.apache.logging.log4j.Level;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;

/**
 * File operation class
 */
public final class FileHandlingClass {
    /**
     * String constant
     */
    private static final int INT_1DAY_MILISECS = 24 * 60 * 60 * 1000;

    /**
     * Getting current user
     * 
     * @return File
     */
    public static File getCurrentUserFolder() {
        return new File(System.getProperty("user.home"));
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
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileFindingError"), strExtension, strFolderName);
            Common.setInputOutputExecutionLoggedToError(strFeedback, Arrays.toString(ex.getStackTrace()));
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
     * @return application folder
     */
    public static String getProjectFolder() {
        String strAppFolder = null;
        final File directory = new File(""); // parameter is empty
        try {
            strAppFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileFolderError"), Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return strAppFolder;
    }

    /**
     * Archives single file to new location
     * 
     * @param strFileName file name in scope for archival
     * @param strDestFolder destination folder
     */
    public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
        try {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder);
                LoggerLevelProvider.LOGGER.info(strFeedback);
            }
            Files.move(Path.of(strFileName), Path.of(strDestFolder), StandardCopyOption.REPLACE_EXISTING);
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
     * Get list of sub-folders from a given folder
     * 
     * @param strFolderName folder name to look into
     * @param intOlderLimit older days limit
     */
    public static void removeFilesOlderThanGivenDays(final String strFolderName, final long intOlderLimit) {
        final long cutoff = new Date().getTime() - intOlderLimit * INT_1DAY_MILISECS;
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nRemovingModifiedFilesOlderFromFolder"), Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim(), strFolderName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    removeFilesOlderThanGivenDays(entry.toString(), intOlderLimit);
                } else if (Files.isRegularFile(entry)) {
                    final BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);
                    final long modifTime = attr.lastModifiedTime().toMillis();
                    if (modifTime <= cutoff) {
                        Files.delete(entry);
                    }
                }
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
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

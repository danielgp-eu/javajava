package file;

import localization.JavaJavaLocalizationClass;
import log.LogExposure;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileAllCertainOnesFromFolder"), strExtension, strFolderName));
        final List<String> arrayFiles = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            // with forEach loop get all the path of files present in directory  
            for (final Path file : stream) {
                if (file.getFileName().toString().endsWith(strExtension)) {
                    final String strFile = file.getParent().toString() + File.separator + file.getFileName();
                    arrayFiles.add(strFile);
                    LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileFound"), strExtension, strFile));
                }
            }
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), strExtension, strFolderName, Arrays.toString(ex.getStackTrace())));
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
        LogExposure.exposeMessageToDebugLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersAttempt"), strFolderName));
        final List<String> arraySubFolders = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    arraySubFolders.add(entry.toString());
                    LogExposure.exposeMessageToInfoLog(entry.toString());
                }
            }
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace())));
        }
        return arraySubFolders;
    }

    /**
     * Archives single file to new location
     * 
     * @param strFileName file name in scope for archival
     * @param strDestFolder destination folder
     */
    public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
        try {
            LogExposure.exposeMessageToInfoLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder));
            Files.move(Path.of(strFileName), Path.of(strDestFolder), StandardCopyOption.REPLACE_EXISTING);
            LogExposure.exposeMessageToInfoLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveSuccess"), strFileName, strDestFolder));
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveError"), strFileName, strDestFolder, Arrays.toString(ex.getStackTrace())));
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
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace())));
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
        LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nRemovingModifiedFilesOlderFromFolder"), Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim(), strFolderName));
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
            LogExposure.exposeMessageToErrorLog(String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace())));
        }
    }

    /**
     * Constructor
     */
    private FileHandlingClass() {
        // intentionally blank
    }
}

package file;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * File operation class
 */
public final class FileHandlingClass {
    /**
     * Cleaned Folder Statistics
     */
    private static boolean bolClnFldrStats;
    /**
     * String constant
     */
    private static final int INT_1DAY_MILISECS = 24 * 60 * 60 * 1000;
    /**
     * Counter for removed files
     */
    private static long lngFilesClnd;
    /**
     * Size in bytes for removed files
     */
    private static long lngByteSizeClnd;

    /**
     * Getter for Cleaned Folder Statistics
     * @return Map with folder statistics
     */
    public static Map<String, Long> getCleanedFolderStatistics() {
        final Map<String, Long> statsClndFldr = new ConcurrentHashMap<>();
        statsClndFldr.put("Files", lngFilesClnd);
        statsClndFldr.put("Size", lngByteSizeClnd);
        return statsClndFldr;
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
     * Get list of files from a given folder
     * 
     * @param strFolderName folder name to look into
     * @param strExtension extension to isolate
     * @return List of Strings
     */
    public static List<String> getSpecificFilesFromFolder(final String strFolderName, final String strExtension) {
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileAllCertainOnesFromFolder"), strExtension, strFolderName);
        LogExposureClass.LOGGER.debug(strFeedback);
        final List<String> arrayFiles = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            // with forEach loop get all the path of files present in directory  
            for (final Path file : stream) {
                if (file.getFileName().toString().endsWith(strExtension)) {
                    final String strFile = file.getParent().toString() + File.separator + file.getFileName();
                    arrayFiles.add(strFile);
                    final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFound"), strExtension, strFile);
                    LogExposureClass.LOGGER.debug(strFeedbackOk);
                }
            }
        } catch (IOException ex) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), strExtension, strFolderName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.debug(strFeedbackErr);
        }
        return arrayFiles;
    }

    /**
     * Get list of sub-folders from a given folder
     * 
     * @param strFolderName folder name to look into
     * @return List of String
     */
    public static List<String> getSubFoldersFromFolder(final String strFolderName) {
        final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersAttempt"), strFolderName);
        LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
        final List<String> arraySubFolders = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    arraySubFolders.add(entry.toString());
                    final String strFeedback = String.format("Folder %s was found", entry.toString());
                    LogExposureClass.LOGGER.info(strFeedback);
                }
            }
        } catch (IOException ex) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.debug(strFeedbackErr);
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
            final String strFeedbackBefore = String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder);
            LogExposureClass.LOGGER.info(strFeedbackBefore);
            Files.move(Path.of(strFileName), Path.of(strDestFolder), StandardCopyOption.REPLACE_EXISTING);
            final String strFeedbackAfter = String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveSuccess"), strFileName, strDestFolder);
            LogExposureClass.LOGGER.info(strFeedbackAfter);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileMoveError"), strFileName, strDestFolder, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
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
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Get list of sub-folders from a given folder
     * 
     * @param strFolderName folder name to look into
     * @param intOlderLimit older days limit
     */
    public static void removeFilesOlderThanGivenDays(final String strFolderName, final long intOlderLimit) {
        final Instant now = Instant.now(); // For timestamps
        final long cutoff = now.minusMillis(intOlderLimit * INT_1DAY_MILISECS).toEpochMilli();
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nRemovingModifiedFilesOlderFromFolder"), Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim(), strFolderName);
        LogExposureClass.LOGGER.debug(strFeedback);
        final Path directory = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    removeFilesOlderThanGivenDays(entry.toString(), intOlderLimit);
                } else if (Files.isRegularFile(entry)) {
                    final BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);
                    final long modifTime = attr.lastModifiedTime().toMillis();
                    if (modifTime <= cutoff) {
                        if (bolClnFldrStats) {
                            lngFilesClnd = lngFilesClnd + 1;
                            lngByteSizeClnd = lngByteSizeClnd + attr.size();
                        }
                        Files.delete(entry);
                    }
                }
            }
        } catch (IOException ex) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
    }

    /**
     * Setter for Cleaned Folder Statistics
     * @param inClnFldrStats boolean
     */
    public static void setCleanedFolderStatistics(final boolean inClnFldrStats) {
        if (bolClnFldrStats != inClnFldrStats) {
            setOrResetCleanedFolderStatistics();
        }
        bolClnFldrStats = inClnFldrStats;
    }

    /**
     * Setter Resetter for Cleaned Folder Statistics
     */
    public static void setOrResetCleanedFolderStatistics() {
        lngFilesClnd = 0;
        lngByteSizeClnd = 0;
    }

    /**
     * Constructor
     */
    private FileHandlingClass() {
        // intentionally blank
    }
}

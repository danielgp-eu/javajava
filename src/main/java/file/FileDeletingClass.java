package file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * File Deletion logic
 */
public final class FileDeletingClass {

    /**
     * Removes a files if already exists
     * 
     * @param strFileName file name to search
     */
    public static void deleteFileIfExists(final String strFileName) {
        try {
            final Path filePath = Paths.get(strFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Deletes all files matching given pattern from folder
     * @param strFolder input folder
     * @param strPattern input pattern
     */
    public static void deleteFilesMatchingPatternFromFolder(final String strFolder, final String strPattern) {
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
     * File deleting logic
     */
    public static final class OlderClass {
        /**
         * Cleaned Folder Statistics
         */
        private static boolean bolClnFldrStats;
        /**
         * String constant
         */
        /* default */ private static final int INT_1DAY_MILISECS = 24 * 60 * 60 * 1000;
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
         * Get list of sub-folders from a given folder
         * 
         * @param strFolderName folder name to look into
         * @param intOlderLimit older days limit
         */
        public static void deleteFilesOlderThanGivenDays(final String strFolderName, final long intOlderLimit) {
            final Instant now = Instant.now(); // For timestamps
            final long cutoff = now.minusMillis(intOlderLimit * INT_1DAY_MILISECS).toEpochMilli();
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nRemovingModifiedFilesOlderFromFolder"), Instant.ofEpochMilli(cutoff).toString().replaceAll("[TZ]", " ").trim(), strFolderName);
            LogExposureClass.LOGGER.debug(strFeedback);
            final Path directory = Paths.get(strFolderName);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (final Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        deleteFilesOlderThanGivenDays(entry.toString(), intOlderLimit);
                    } else if (Files.isRegularFile(entry)) {
                        deleteFilesOlderThanGivenDaysWithoutChecks(entry, cutoff);
                    }
                }
            } catch (IOException ex) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
        }

        /**
         * Remove files older than given days without checks
         * @param entry Path to file
         * @param cutoff cutoff time in milliseconds
         * @throws IOException check exception
         */
        private static void deleteFilesOlderThanGivenDaysWithoutChecks(final Path entry, final long cutoff) throws IOException {
            final BasicFileAttributes attr = Files.readAttributes(entry, BasicFileAttributes.class);
            final long modifTime = attr.lastModifiedTime().toMillis();
            if (modifTime <= cutoff) {
                Files.delete(entry);
                if (bolClnFldrStats) {
                    lngFilesClnd = lngFilesClnd + 1;
                    lngByteSizeClnd = lngByteSizeClnd + attr.size();
                }
            }
        }

        /**
         * Setter for Cleaned Folder Statistics
         * @param inStats boolean
         */
        public static void setCleanedFolderStatistics(final boolean inStats) {
            if (bolClnFldrStats != inStats) {
                setOrResetCleanedFolderStatistics();
            }
            bolClnFldrStats = inStats;
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
        private OlderClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private FileDeletingClass() {
        // intentionally blank
    }

}

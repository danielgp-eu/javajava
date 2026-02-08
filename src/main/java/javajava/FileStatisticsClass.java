package javajava;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Statistics
 */
public final class FileStatisticsClass {
    /**
     * Checksum algorithms
     */
    private static String[] listAlgorithms = {"SHA-256", "SHA-512", "SHA3-256", "SHA3-512"};

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
     * Get statistics for all files within a given folder
     * @param strFolderName input folder name
     */
    public static void captureFileStatisticsFromFolder(final String strFolderName, final String outCsvFile) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outCsvFile), StandardCharsets.UTF_8)) {
            writer.write("Folder;File;Size;Last Modified Time");
            for(final String crtAlgo: listAlgorithms) {
                writer.write(';' + crtAlgo);
            }
            writer.newLine();
            gatherFileStatisticsFromFolder(strFolderName, writer);
        } catch (IOException ei) {
            LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Compute checksum for a given file
     * @param file input file
     * @param algorithm checksum algorithm name
     * @return String
     */
    private static String computeSingleChecksum(final Path file, final String algorithm) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            final String strFeedbackErr = String.format("Checksum algorithm %s is not availble.... %s", algorithm, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        final StringBuilder sbChecksumValue = new StringBuilder();
        try (InputStream istrmFile = Files.newInputStream(file);
            DigestInputStream dis = new DigestInputStream(istrmFile, digest)) {
            // Read and discard all data while updating the digest
            dis.transferTo(OutputStream.nullOutputStream());
        } catch (IOException e) {
            final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nFileContentError"), "*", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        assert digest != null;
        final byte[] hashBytes = digest.digest();
        for (final byte byteVar : hashBytes) {
            sbChecksumValue.append(String.format("%02x", byteVar));
        }
        return sbChecksumValue.toString();
    }

    /**
     * Compute all known checksums for a given file
     * @param file input file
     * @param writer BufferedWriter to write the results
     */
    private static void computeFileMultipleChecksums(final Path file, final BufferedWriter writer) {
        try(ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            executor.submit(() -> {
                for (final String algo : listAlgorithms) {
                    final String crtChecksum = computeSingleChecksum(file, algo);
                    try {
                        writer.write(';' + crtChecksum);
                    } catch (IOException ei) {
                        final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, "*", file);
                        LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
                    }
                }
            });
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ei) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nAppInterruptedExecution"), Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
    }

    /**
     * performs statistics for all files within a given folder 
     * @param strFolderName input folder name
     */
    private static void gatherFileStatisticsFromFolder(final String strFolderName, final BufferedWriter writer) {
        final Path folder = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (final Path file : stream) {
                if (Files.isDirectory(file)) {
                    gatherFileStatisticsFromFolder(file.toString(), writer);
                } else if (Files.isRegularFile(file)) {
                    writer.write(file.getParent().toString()
                            + ';' + file.getFileName().toString()
                            + ';' + file.toFile().getTotalSpace()
                            + ';' + TimingClass.getFileLastModifiedTimeAsHumanReadableFormat(file));
                    computeFileMultipleChecksums(file, writer);
                    writer.newLine();
                }
            }
        } catch (IOException ei) {
            final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, "*", strFolderName);
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
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
                            final String strFeedback = String.format(LocalizationClass.getMessage("i18nEmptyFolder"), strFolderName, Arrays.toString(e.getStackTrace()));
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
            final String strFeedback = String.format(FileOperationsClass.I18N_FILE_FND_ERR, foderName.getParent(), foderName.getFileName());
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
        return pathProps;
    }

    /**
     * Setter for checksum algorithms 
     * @param inAlgorithms char
     */
    public static void setChecksumAlgorithms(final String... inAlgorithms) {
        listAlgorithms = inAlgorithms;
    }

    /**
     * File Content Reading
     */
    public static final class RetrievingClass {

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
                    propertiesReturn.put("NOT_READABLE", String.format(LocalizationClass.getMessage("i18nFileUnreadable"), strFileName));
                    break;
                case "-2":
                    propertiesReturn.put("NOT_A_FILE", String.format(LocalizationClass.getMessage("i18nFileNotAfile"), strFileName));
                    break;
                case "-3":
                    propertiesReturn.put("DOES_NOT_EXIST", String.format(LocalizationClass.getMessage("i18nFileDoesNotExist"), strFileName));
                    break;
                case "-99":
                    propertiesReturn.put("NULL_FILE_NAME", LocalizationClass.getMessage("i18nFileDoesNotExist"));
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
         * Getting current user
         * 
         * @return File
         */
        public static File getCurrentUserFolder() {
            return new File(System.getProperty("user.home"));
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
         * Get list of files from a given folder that may have sub-folders
         * @param inFolderName folder name to look into
         * @param strExtension extension to isolate
         * @return List of Strings
         */
        public static List<Path> getSpecificFilesFromFolderRecursive(final Path inFolderName, final String strExtension) {
            List<Path> arrayFiles = List.of();
            try (Stream<Path> stream = Files.walk(inFolderName)) {
                arrayFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(strExtension))
                    .toList();
            } catch (IOException ei) {
                final String strFeedbackErr = String.format(FileOperationsClass.I18N_FILE_FND_ERR, strExtension, inFolderName);
                LogExposureClass.exposeInputOutputException(strFeedbackErr, Arrays.toString(ei.getStackTrace()));
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
            final String strFeedbackAtmpt = String.format(LocalizationClass.getMessage("i18nFileSubFoldersAttempt"), strFolderName);
            LogExposureClass.LOGGER.debug(strFeedbackAtmpt);
            final List<String> arraySubFolders = new ArrayList<>();
            final Path directory = Paths.get(strFolderName);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (final Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        arraySubFolders.add(entry.toString());
                        final String strFeedback = String.format("Folder %s was found", entry);
                        LogExposureClass.LOGGER.info(strFeedback);
                    }
                }
            } catch (IOException ex) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.debug(strFeedbackErr);
            }
            return arraySubFolders;
        }

        /**
         * Constructor
         */
        private RetrievingClass() {
            // intentionally blank
        }

    }

    /**
     * File Content Reading
     */
    public static final class RetrievingCompactOrRegularFileClass {
        /**
         * String constant Minified
         */
        private static final String STR_MINIFIED = "Minified";
        /**
         * String constant PrettyPrint
         */
        private static final String STR_PRTY_PRNT = "PrettyPrint";

        /**
         * Establish pre-extensions for Regular and Compact file name
         * @param strFilePattern file pattern to use
         * @return Properties
         */
        private static Properties establishRegularOrCompactFileName(final String strFilePattern) {
            final Properties propsFile = new Properties();
            propsFile.put(STR_MINIFIED, String.format(strFilePattern, ".min"));
            propsFile.put(STR_PRTY_PRNT, String.format(strFilePattern, ""));
            return propsFile;
        }

        /**
         * read Main configuration file
         * @param strFilePattern file pattern to use
         * @return String
         */
        public static String getJsonConfigurationFile(final String strFilePattern) {
            final Properties propsFile = establishRegularOrCompactFileName(strFilePattern);
            String strFileJson = null;
            final Properties propsMinified = RetrievingClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_MINIFIED));
            for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
                final boolean isItOk = "OK".equals(eMinified.getKey());
                if (isItOk) {
                    strFileJson = eMinified.getValue().toString();
                } else {
                    final Properties propsPreety = RetrievingClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_PRTY_PRNT));
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
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileConfigurationNotFound")
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
        private RetrievingCompactOrRegularFileClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private FileStatisticsClass() {
        // intentionally blank
    }
}

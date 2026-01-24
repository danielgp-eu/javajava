package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import time.TimingClass;

/**
 * File reading methods
 */
public final class FileContentReadClass {
    /**
     * File Statistics variable
     */
    private static Map<String, Map<String, String>> fileStats = new ConcurrentHashMap<>();
    /**
     * Checksum algorithms
     */
    private static String[] listAlgorithms = {"MD5", "SHA-1", "SHA-256", "SHA-512"};

    /**
     * Compute checksum for a given file
     * @param file input file
     * @param algorithm checksum algorithm name
     * @return 
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
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentError"), "*", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        final byte[] hashBytes = digest.digest();
        for (final byte byteVar : hashBytes) {
            sbChecksumValue.append(String.format("%02x", byteVar));
        }
        return sbChecksumValue.toString();
    }

    /**
     * Compute all known checksums for a given file
     * @param file
     * @return
     */
    private static Map<String, String> computeFileMultipleChecksums(final Path file) {
        final Map<String, String> crtFileStats = new ConcurrentHashMap<>();
        crtFileStats.put("Size", String.valueOf(file.toFile().getTotalSpace()));
        crtFileStats.put("Last Modified Time", TimingClass.getFileLastModifiedTimeAsHumanReadableFormat(file));
        try(ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            executor.submit(() -> {
                for (final String algo : listAlgorithms) {
                    final String crtChecksum = computeSingleChecksum(file, algo);
                    crtFileStats.put("Checksum " + algo, crtChecksum);
                }
            });
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ei) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nAppInterruptedExecution"), Arrays.toString(ei.getStackTrace()));
            LogExposureClass.LOGGER.warn(strFeedback);
            /* Clean up whatever needs to be handled before interrupting  */
            Thread.currentThread().interrupt();
        }
        return crtFileStats;
    }

    /**
     * performs statistics for all files within a given folder 
     * @param strFolderName input folder name
     */
    private static void gatherFileStatisticsFromFolder(final String strFolderName) {
        final Path folder = Paths.get(strFolderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (final Path file : stream) {
                if (Files.isDirectory(file)) {
                    gatherFileStatisticsFromFolder(file.toString());
                } else if (Files.isRegularFile(file)) {
                    fileStats.put(file.getParent() + File.separator + file.getFileName().toString(),
                            computeFileMultipleChecksums(file));
                }
            }
        } catch (IOException ei) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), "*", strFolderName);
            LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Get file content into String
     * 
     * @param strFileName file name
     * @return String
     */
    public static String getFileContentIntoString(final String strFileName) {
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
        LogExposureClass.LOGGER.debug(strFeedback);
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return strReturn;
    }

    /**
     * Get statistics for all files within a given folder
     * @param strFolderName input folder name
     * @return Map with file statistics
     */
    public static Map<String, Map<String, String>> getFileStatisticsFromFolder(final String strFolderName) {
        if (fileStats != null) {
            fileStats.clear();
        }
        gatherFileStatisticsFromFolder(strFolderName);
        return fileStats;
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName file name
     * @return input stream
     */
    public static String getIncludedFileContentIntoString(final String strFileName) {
        String strContent = null;
        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
        LogExposureClass.LOGGER.debug(strFeedback);
        try (InputStream iStream = FileContentReadClass.class.getResourceAsStream(strFileName);
                InputStreamReader inputStreamReader = new InputStreamReader(iStream);
                BufferedReader bReader = new BufferedReader(inputStreamReader)) {
            strContent = bReader.readAllAsString();
            final String strFeedbackOk = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), strFileName);
            LogExposureClass.LOGGER.debug(strFeedbackOk);
        } catch (IOException ex) {
            final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nError"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedbackErr);
        }
        return strContent;
    }

    /**
     * Getting list of values from a column grouped by another column
     * @param strFileName target file name to be written to
     * @param intColToEval number of column to evaluate (build values list from it)
     * @param intColToGrpBy number of column to group list of values by
     * @return Map with String and List or String
     */
    public static Map<String, List<String>> getListOfValuesFromColumnsGroupedByAnotherColumnValuesFromCsvFile(
            final String strFileName,
            final Integer intColToEval,
            final Integer intColToGrpBy) {
        Map<String, List<String>> grouped = null;
        try {
            // Group values by category
            grouped = Files.lines(Path.of(strFileName))
                    .skip(1)
                    .map(line -> line.split("\",\"")) // split by comma
                    .collect(Collectors.groupingBy(
                            cols -> cols[intColToGrpBy], // key = Category
                            Collectors.mapping(cols -> cols[intColToEval].replace("\"", ""), Collectors.toList()) // values
                    ));
        } catch (IOException ex) {
            final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return grouped;
    }

    /**
     * Setter for checksum algorithms 
     * @param inAlgorithms char
     */
    public static void setChecksumAlgorithms(final String... inAlgorithms) {
        listAlgorithms = inAlgorithms;
    }

    /**
     * Constructor
     */
    private FileContentReadClass() {
        // intentionally blank
    }

}

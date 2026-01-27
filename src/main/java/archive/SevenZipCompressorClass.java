package archive;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZMethod;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;

import log.LogExposureClass;

/**
 * Archiving 7z
 */
public final class SevenZipCompressorClass {
    /**
     * optional password
     */
    private static char[] archivePassword;

    /**
     * Compresses a source directory into a 7z archive.
     * @param sourceDir Path to the directory to compress.
     * @param output7zPath Path to the output 7z file (e.g., "archive.7z").
     * @throws IOException If an I/O error occurs.
     */
    public static void compressFolder(final Path sourceDir, final Path output7zPath) throws IOException {
        // Validate source directory
        if (!Files.isDirectory(sourceDir)) {
            final String strFeedback = String.format("Source must be a directory: %s", sourceDir);
            LogExposureClass.LOGGER.error(strFeedback);
            throw new IllegalArgumentException("Source must be a directory: " + sourceDir);
        }
        // Commons Compress accepts the org.tukaani.xz.LZMA2Options object here
        final LZMA2Options options = compressionLevel();
        final SevenZMethodConfiguration config = new SevenZMethodConfiguration(SevenZMethod.LZMA2, options);
        // Using try-with-resources to auto-close the 7z output file
        try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(output7zPath.toFile(), archivePassword)) {
            sevenZOutput.setContentMethods(Collections.singletonList(config)); // solid archive
            // Walk through all files in the source directory
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                    // Don't add the root folder itself as an entry (optional)
                    if (!sourceDir.equals(dir)) {
                        final String entryName = sourceDir.relativize(dir).toString().replace("\\", "/") + "/";
                        final SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(dir.toFile(), entryName);
                        sevenZOutput.putArchiveEntry(entry);
                        sevenZOutput.closeArchiveEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    // Relativize path to keep structure: e.g., "logs/2026/jan.log"
                    final String entryName = sourceDir.relativize(file).toString().replace("\\", "/");
                    final SevenZArchiveEntry entry = sevenZOutput.createArchiveEntry(file.toFile(), entryName);
                    sevenZOutput.putArchiveEntry(entry);
                    try (InputStream inStream = Files.newInputStream(file)) {
                        // Efficiently stream the file content
                        transferData(inStream, sevenZOutput);
                    } catch (IOException ei) {
                        LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
                    }
                    sevenZOutput.closeArchiveEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        final String strFeedback = String.format("Compression complete: %s", output7zPath);
        LogExposureClass.LOGGER.info(strFeedback);
    }

    /**
     * Sets compression level
     * @return LZMA2Options
     */
    private static LZMA2Options compressionLevel() {
        // Initialize the XZ LZMA2Options
        final LZMA2Options options = new LZMA2Options();
        try {
            options.setPreset(9); // Level 9 Ultra
        } catch (UnsupportedOptionsException eu) {
            final String strFeedback = String.format("UnsupportedOptionsException: %s", Arrays.toString(eu.getStackTrace()));
            LogExposureClass.LOGGER.info(strFeedback);
        }
        return options;
    }

    /**
     * Helper to bridge InputStream to SevenZOutputFile without loading file into RAM.
     */
    private static void transferData(final InputStream inStream, final SevenZOutputFile out) throws IOException {
        final byte[] buffer = new byte[8192];
        int number;
        while ((number = inStream.read(buffer)) != -1) {
            out.write(buffer, 0, number);
        }
    }

    /**
     * Setter for archivePassword
     * @param inPassword provided password
     */
    public static void setArchivePassword(final String inPassword) {
        if (inPassword != null && !inPassword.isEmpty()) {
            archivePassword = inPassword.toCharArray();
        }
    }

    /**
     * Constructor
     */
    private SevenZipCompressorClass() {
        // intentionally blank
    }


}

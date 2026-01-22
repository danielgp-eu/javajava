package file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import log.LogExposureClass;

/**
 * Mass changes to multiple files
 */
public final class FileChangeClass {
    /**
     * variable for folder
     */
    private static String strFolder;

    /**
     * Change String to all files within a folder based on a pattern
     * @param strPattern pattern to filter files
     * @param matchingContent characters being replaced
     * @param replacedContent characters to replace it with
     */
    public static void massChangeToFilesWithinFolder(final String strPattern, final String matchingContent, final String replacedContent) {
        try {
            final String strFeedback = String.format("I will attempt to mass change all matched files based on %s pattern from folder %s...", strPattern, strFolder);
            LogExposureClass.LOGGER.info(strFeedback);
            final Path dir = Paths.get(strFolder);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().matches(strPattern)) {
                        // Use a temporary file to handle the modified content
                        Path tempFile = null;
                        try {
	                        tempFile = Files.createTempFile("secure-", ".tmp");
	                        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
	                                BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
	                            String line;
	                            while ((line = reader.readLine()) != null) {
	                                writer.write(line.replace(matchingContent, replacedContent));
	                                writer.newLine(); // Reintroduce the line separator
	                            }
	                        }
	                        // Replace the original file with the modified content
	                        Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
                        } finally {
                            Files.deleteIfExists(tempFile);
                        }
                        final String strFeedback = String.format("File %s has been modified...", file);
                        LogExposureClass.LOGGER.debug(strFeedback);
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
     * Setter for strDestinationFolder
     * @param inFolder destination folder
     */
    public static void setSearchingFolder(final String inFolder) {
        strFolder = inFolder;
    }

    /**
     * Constructor
     */
    private FileChangeClass() {
        // intentionally blank
    }

}

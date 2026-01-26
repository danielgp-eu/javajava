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
     * holding characters being replaced
     */
    private static String existingContent;
    /**
     * holding characters to replace it with
     */
    private static String replacedContent;
    /**
     * variable for folder
     */
    private static String strFolder;
    /**
     * variable for pattern
     */
    private static String strPattern;

    /**
     * Build new file from existing one
     * @param existingFile existing file as Path
     * @return Path
     */
    private static Path getNewFile(final Path existingFile) {
        final String newPath = existingFile.getParent().toString();
        final String newFileName = existingFile.getFileName().toString().replace(".json", "-new.json");
        return Path.of(newPath).resolve(newFileName);
    }

    /**
     * Change String to all files within a folder based on a pattern
     */
    public static void massChangeToFilesWithinFolder() {
        try {
            final String strFeedback = String.format("I will attempt to mass change all matched files based on %s pattern from folder %s...", strPattern, strFolder);
            LogExposureClass.LOGGER.info(strFeedback);
            final Path dir = Paths.get(strFolder);
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (file.getFileName().toString().matches(strPattern)) {
                        secureModify(file);
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
     * secure modification
     * @param file file to write to
     */
    private static void secureModify(final Path file) {
        final Path newFile = getNewFile(file);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
            BufferedWriter writer = Files.newBufferedWriter(newFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.replace(existingContent, replacedContent));
                writer.newLine(); // Reintroduce the line separator
            }
            // Replace the original file with the modified content
            Files.move(newFile, file, StandardCopyOption.REPLACE_EXISTING);
            final String strFeedback = String.format("File %s has been modified...", file);
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ei) {
            LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
        }
    }

    /**
     * Setter for replacedContent
     * @param inNewContent new content
     */
    public static void setNewContent(final String inNewContent) {
        replacedContent = inNewContent;
    }

    /**
     * Setter for existingContent
     * @param inOldContent old content
     */
    public static void setOldContent(final String inOldContent) {
        existingContent = inOldContent;
    }

    /**
     * Setter for strPattern
     * @param inPattern pattern for file matching
     */
    public static void setPattern(final String inPattern) {
        strPattern = inPattern;
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

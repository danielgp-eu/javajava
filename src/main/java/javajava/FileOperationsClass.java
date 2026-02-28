package javajava;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File Operations
 */
public final class FileOperationsClass {
    /**
     * Localized String for File Finding error 
     */
    public static final String I18N_FILE_FND_ERR = LocalizationClass.getMessage("i18nFileFindingError");

    /**
     * File Content Reading
     */
    public static final class ContentReadingClass {

        /**
         * Capture Import Statements from Java source files into CSV
         * @param inJavaSources folder with Java source files
         * @param outCsvFile CSV file to write results into
         */
        public static void extractImportStatementsFromJavaSourceFilesIntoCsvFile(final Path inJavaSources, final Path outCsvFile) {
            final String strImport = "import ";
            try (BufferedWriter writer = Files.newBufferedWriter(outCsvFile, StandardCharsets.UTF_8)) {
                writer.write("Path;File;Imported;Timestamp");
                writer.newLine();
                final List<Path> arrayFiles = FileStatisticsClass.RetrievingClass.getSpecificFilesFromFolderRecursive(inJavaSources, "java");
                arrayFiles.forEach(crtFileName -> {
                    try (BufferedReader reader = Files.newBufferedReader(crtFileName, StandardCharsets.UTF_8)) {
                        String line = reader.readLine();  // Initialize the variable outside the loop
                        long lineCounter = 0;
                        while (Objects.nonNull(line) && (lineCounter < 100)) {
                            if (line.startsWith(strImport)) {
                                writer.write(crtFileName.getParent().toString()
                                        + ';' + crtFileName.getFileName().toString()
                                        + ';' + line.replace(strImport, "").replace(";", "")
                                        + ';' + TimingClass.getCurrentTimestamp("yyyy-MM-dd HH:mm:ss.SSS"));
                                writer.newLine();
                            }
                            line = reader.readLine();  // Update the variable within the loop, not in the condition
                            lineCounter++;
                        }
                        final String strFeedback = String.format("File %s has been digested...", crtFileName);
                        LogExposureClass.LOGGER.debug(strFeedback);
                    } catch (IOException ei) {
                        LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
                    }
                });
            } catch (IOException ei) {
                LogExposureClass.exposeInputOutputException(Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Get file content into String
         * (either included in JAR or from Disk/Storage)
         * @param strFileName file name in scope
         * @return file content
         */
        public static String getFileContentIntoString(final String strFileName) {
            final String strOutput;
            if (ProjectClass.isRunningFromJar()) {
                strOutput = getJarIncludedFileContentIntoString(strFileName);
            } else {
                strOutput = getDiskFileContentIntoString(strFileName);
            }
            return strOutput;
        }

        /**
         * Get file content into String
         * (good for small files, bad for JAR included files)
         * @param strFileName file name
         * @return String
         */
        private static String getDiskFileContentIntoString(final String strFileName) {
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            String strReturn = "";
            try {
                strReturn = Files.readString(Path.of(strFileName));
            } catch (IOException e) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedbackErr);
            }
            return strReturn;
        }

        /**
         * Get file content into InputStream
         * @param strFileName file name
         * @return input stream
         */
        private static String getJarIncludedFileContentIntoString(final String strFileName) {
            String strContent = null;
            final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            try (InputStream iStream = ContentReadingClass.class.getResourceAsStream(strFileName);
                    InputStreamReader inputStreamReader = new InputStreamReader(iStream, StandardCharsets.UTF_8);
                    BufferedReader bReader = new BufferedReader(inputStreamReader)) {
                strContent = bReader.readAllAsString();
                final String strFeedbackOk = String.format(LocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), strFileName);
                LogExposureClass.LOGGER.debug(strFeedbackOk);
            } catch (IOException ex) {
                final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nError"), Arrays.toString(ex.getStackTrace()));
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
        public static Map<String, List<String>> getListOfValuesFromColumnGroupedByAnotherColumnValuesFromCsvFile(
                final String strFileName,
                final Integer intColToEval,
                final Integer intColToGrpBy) {
            Map<String, List<String>> grouped = null;
            try (Stream<String> lines = Files.lines(Path.of(strFileName))) {
                // Group values by category
                grouped = lines
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
         * Constructor
         */
        private ContentReadingClass() {
            // intentionally blank
        }

    }

    /**
     * File Content Reading
     */
    public static final class ContentWritingClass {
        /**
         * Column Separator for CSV file writing methods
         */
        private static char chCsvColSeparator = ',';
        /**
         * Line Prefix for CSV content writing methods
         */
        private static String strCsvLinePrefix = "";

        /**
         * Setter for Column Separator for CSV file writing methods
         * @param inCsvColSeparator char
         */
        public static void setCsvColumnSeparator(final char inCsvColSeparator) {
            chCsvColSeparator = inCsvColSeparator;
        }

        /**
         * Setter for Line prefix for CSV file writing methods
         * @param inCsvLinePrefix String
         */
        public static void setCsvLinePrefix(final String inCsvLinePrefix) {
            strCsvLinePrefix = inCsvLinePrefix + chCsvColSeparator;
        }

        /**
         * storing into a CSV file a LinkedHashMap
         * @param strFileName target file name to be written to
         * @param strHeader header values
         * @param listHsMp LinkedHashMap
         */
        public static void writeLinkedHashMapToCsvFile(final String strFileName, final String strHeader, final Map<String, Long> listHsMp) {
            try {
                final List<String> strLines;
                final File strFile = new File(strFileName);
                if (strFile.exists()) {
                    strLines = listHsMp.entrySet().stream()
                            .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                            .toList();
                } else {
                    strLines = Stream.concat(
                            Stream.of(strHeader), // header
                            listHsMp.entrySet().stream()
                                    .map(e -> strCsvLinePrefix + e.getKey() + chCsvColSeparator + e.getValue())
                    ).toList();
                }
                Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Write list of single values to File
         * 
         * @param listStrings List of Strings
         * @param strFileName file name to write to
         */
        public static void writeListToTextFile(final String strFileName, final List<String> listStrings) {
            DeletingClass.deleteFileIfExists(strFileName);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                listStrings.forEach(strLine -> {
                    try {
                        bwr.write(strLine);
                        bwr.newLine();
                    } catch (IOException er) {
                        final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                        LogExposureClass.LOGGER.error(strFeedback);
                    }
                });
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileWritingSuccess"), strFileName);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Write list of Properties to CSV File
         *
         * @param strFileName target File
         * @param propertiesList list of Properties
         */
        public static void writePropertiesListToCsvFile(final String strFileName, final List<Properties> propertiesList) {
            // Collect all unique keys
            final Set<String> allKeys = new LinkedHashSet<>();
            for (final Properties properties : propertiesList) {
                allKeys.addAll(properties.stringPropertyNames());
            }
            final String strClmnSeparator = String.valueOf(chCsvColSeparator);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                // Write the header
                bwr.write(String.join(strClmnSeparator, allKeys));
                bwr.newLine();
                final Set<String> row = new LinkedHashSet<>();
                // Write each row
                for (final Properties properties : propertiesList) {
                    row.clear();
                    for (final String key : allKeys) {
                        row.add(properties.getProperty(key, "")); // Supply default value "" if key is absent
                    }
                    bwr.write(String.join(strClmnSeparator, row));
                    bwr.newLine();
                }
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Store small content into file
         * @param strFileName destination file name
         * @param strRawText content
         */
        public static void writeRawTextToFile(final String strFileName, final String strRawText) {
            DeletingClass.deleteFileIfExists(strFileName);
            try (BufferedWriter bwr = Files.newBufferedWriter(Path.of(strFileName), StandardCharsets.UTF_8)) {
                bwr.write(strRawText);
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileWritingSuccess"), strFileName);
                LogExposureClass.LOGGER.debug(strFeedback);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * storing into a CSV file a LinkedHashMap
         * @param strFileName target file name to be written to
         * @param strHeader header values
         * @param listStrings List of String
         */
        public static void writeStringListToCsvFile(final String strFileName, final String strHeader, final List<String> listStrings) {
            try {
                final List<String> strLines;
                final File strFile = new File(strFileName);
                if (strFile.exists()) {
                    strLines = listStrings.stream()
                            .map(value -> strCsvLinePrefix + value)
                            .toList();
                } else {
                    strLines = Stream.concat(
                            Stream.of(strHeader), // header
                            listStrings.stream()
                                    .map(value -> strCsvLinePrefix + value)
                    ).toList();
                }
                Files.write(Path.of(strFileName), strLines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException ex) {
                final String strFeedback = LogExposureClass.getFileErrorMessage(strFileName, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Constructor
         */
        private ContentWritingClass() {
            // intentionally blank
        }

    }

    /**
     * File Deletion logic
     */
    public static final class DeletingClass {

        /**
         * Removes a files if already exists
         *
         * @param strFileName file name to search
         */
        public static void deleteFileIfExists(final String strFileName) {
            try {
                final Path filePath = Path.of(strFileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(e.getStackTrace()));
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
                final Path dir = Path.of(strFolder);
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
                final long cutoff = TimingClass.getDaysAgoWithMilisecondsPrecision(now, intOlderLimit);
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nRemovingModifiedFilesOlderFromFolder"), TimingClass.getDaysAgoWithMilisecondsPrecisionAsString(cutoff), strFolderName);
                LogExposureClass.LOGGER.debug(strFeedback);
                final Path directory = Path.of(strFolderName);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                    for (final Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            deleteFilesOlderThanGivenDays(entry.toString(), intOlderLimit);
                        } else if (Files.isRegularFile(entry)) {
                            deleteFilesOlderThanGivenDaysWithoutChecks(entry, cutoff);
                        }
                    }
                } catch (IOException ex) {
                    final String strFeedbackErr = String.format(LocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
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
        private DeletingClass() {
            // intentionally blank
        }

    }

    /**
     * File Mass Change logic
     */
    public static final class MassChangeClass {
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
                final Path dir = Path.of(strFolder);
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
                String line = reader.readLine();  // Initialize the variable outside the loop
                while (Objects.nonNull(line)) {
                    writer.write(line.replace(existingContent, replacedContent));
                    writer.newLine(); // Reintroduce the line separator
                    line = reader.readLine();  // Update the variable within the loop, not in the condition
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
        private MassChangeClass() {
            // intentionally blank
        }
    }

    /**
     * File Moving logic
     */
    public static final class MovingClass {

        /**
         * Archives single file to new location
         * 
         * @param strFileName file name in scope for archival
         * @param strDestFolder destination folder
         */
        public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
            try {
                final String strFeedbackBefore = String.format(LocalizationClass.getMessage("i18nFileMoveAttempt"), strFileName, strDestFolder);
                LogExposureClass.LOGGER.info(strFeedbackBefore);
                Files.move(Path.of(strFileName), Path.of(strDestFolder), StandardCopyOption.REPLACE_EXISTING);
                final String strFeedbackAfter = String.format(LocalizationClass.getMessage("i18nFileMoveSuccess"), strFileName, strDestFolder);
                LogExposureClass.LOGGER.info(strFeedbackAfter);
            } catch (IOException ex) {
                final String strFeedback = String.format(LocalizationClass.getMessage("i18nFileMoveError"), strFileName, strDestFolder, Arrays.toString(ex.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }

        /**
         * Constructor
         */
        private MovingClass() {
            // intentionally blank
        }

    }

    /**
     * Constructor
     */
    private FileOperationsClass() {
        // intentionally blank
    }

}

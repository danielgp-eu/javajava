package file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;
import project.ProjectClass;
import time.TimingClass;

/**
 * File Operations
 */
public final class FileOperationsClass {

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
                final List<Path> arrayFiles = RetrievingClass.getSpecificFilesFromFolderRecursive(inJavaSources, "java");
                arrayFiles.forEach((crtFileName) -> {
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
         * @param strFileName
         * @return
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
         * Get file content into InputStream
         * 
         * @param strFileName file name
         * @return input stream
         */
        private static String getJarIncludedFileContentIntoString(final String strFileName) {
            String strContent = null;
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoString"), strFileName);
            LogExposureClass.LOGGER.debug(strFeedback);
            try (InputStream iStream = ContentReadingClass.class.getResourceAsStream(strFileName);
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
         * get variable
         * @param strVariables variables to pick
         * @return Properties
         */
        public static Properties getVariableFromProjectProperties(final String propFile, final String... strVariables) {
            final Properties svProperties = new Properties();
            try {
                final PropertiesReaderClass reader = new PropertiesReaderClass(propFile);
                final List<String> arrayVariables = Arrays.asList(strVariables);
                arrayVariables.forEach(crtVariable -> svProperties.put(crtVariable, reader.getProperty(crtVariable)));
                if (!propFile.startsWith("/META-INF/maven/")) {
                    final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), svProperties);
                    LogExposureClass.LOGGER.debug(strFeedback);
                }
            } catch (IOException ei) {
                final Path ptPrjProps = Path.of(propFile);
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), ptPrjProps.getParent(), ptPrjProps.getFileName());
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
            return svProperties;
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
            FileOperationsClass.DeletingClass.deleteFileIfExists(strFileName);
            try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
                listStrings.forEach(strLine -> {
                    try {
                        bwr.write(strLine);
                        bwr.newLine();
                    } catch (IOException er) {
                        final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                        LogExposureClass.LOGGER.error(strFeedback);
                    }
                });
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileWritingSuccess"), strFileName);
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
            try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
                // Write the header
                bwr.write(String.join(strClmnSeparator, allKeys));
                bwr.newLine();
                final List<String> row = new ArrayList<>();
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
     * File deleting logic
     */
    public static final class DeletingClass {

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
         * Constructor
         */
        private DeletingClass() {
            // intentionally blank
        }

    }

    /**
     * File deleting logic
     */
    public static final class DeletingOlderClass {
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
        private DeletingOlderClass() {
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
                    propertiesReturn.put("NOT_READABLE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileUnreadable"), strFileName));
                    break;
                case "-2":
                    propertiesReturn.put("NOT_A_FILE", String.format(JavaJavaLocalizationClass.getMessage("i18nFileNotAfile"), strFileName));
                    break;
                case "-3":
                    propertiesReturn.put("DOES_NOT_EXIST", String.format(JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"), strFileName));
                    break;
                case "-99":
                    propertiesReturn.put("NULL_FILE_NAME", JavaJavaLocalizationClass.getMessage("i18nFileDoesNotExist"));
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
                    .collect(Collectors.toList());
            } catch (IOException ei) {
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), strExtension, inFolderName);
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
            final String strFeedbackAtmpt = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersAttempt"), strFolderName);
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
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileSubFoldersError"), strFolderName, Arrays.toString(ex.getStackTrace()));
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
            final Properties propsMinified = FileOperationsClass.RetrievingClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_MINIFIED));
            for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
                final boolean isItOk = "OK".equals(eMinified.getKey());
                if (isItOk) {
                    strFileJson = eMinified.getValue().toString();
                } else {
                    final Properties propsPreety = FileOperationsClass.RetrievingClass.checkFileExistanceAndReadability(propsFile.getProperty(STR_PRTY_PRNT));
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
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileConfigurationNotFound")
                    , propsFile.getProperty(STR_MINIFIED, "")
                    , propsFile.getProperty(STR_PRTY_PRNT, ""));
                LogExposureClass.LOGGER.error(strFeedback);
                throw new IllegalArgumentException(strFeedback);
            }
            return strFileJson;
        }

    }

    /**
     * File statistics from folder
     */
    public static final class FolderStatisticsClass {
        /**
         * File Statistics variable
         */
        /* default */ private static final Map<String, Map<String, String>> FILE_STAT_W_CHKSM = new ConcurrentHashMap<>();
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
                final String strFeedbackErr = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentError"), "*", Arrays.toString(e.getStackTrace()));
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
         * @return Map
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
                        FILE_STAT_W_CHKSM.put(file.getParent() + File.separator + file.getFileName().toString(),
                                computeFileMultipleChecksums(file));
                    }
                }
            } catch (IOException ei) {
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), "*", strFolderName);
                LogExposureClass.exposeInputOutputException(strFeedback, Arrays.toString(ei.getStackTrace()));
            }
        }

        /**
         * Get statistics for all files within a given folder
         * @param strFolderName input folder name
         * @return Map with file statistics
         */
        public static Map<String, Map<String, String>> getFileStatisticsFromFolder(final String strFolderName) {
            FILE_STAT_W_CHKSM.clear();
            gatherFileStatisticsFromFolder(strFolderName);
            return FILE_STAT_W_CHKSM;
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
                                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nEmptyFolder"), strFolderName, Arrays.toString(e.getStackTrace()));
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
                final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), foderName.getParent(), foderName.getFileName());
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
         * Constructor
         */
        private FolderStatisticsClass() {
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

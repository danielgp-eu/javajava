package danielgp;
/* Java IO classes */
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
/* Java NIO classes */
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/* Java util classes */
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * File operation class
 */
public final class FileHandlingClass {

    /**
     * Checking if a file exists and is readable
     * 
     * @param strFileName
     * @return Properties
     */
    public static Properties checkFileExistanceAndReadability(final String strFileName) {
        final Properties propertiesReturn = new Properties();
        final File fileGiven = new File(strFileName);
        if (fileGiven.exists()) {
            if (fileGiven.isFile()) {
                if (fileGiven.canRead()) {
                    propertiesReturn.put("OK", strFileName);
                } else {
                    propertiesReturn.put("NOT_READABLE", String.format("Given file %s is a readable file...", strFileName));
                }
            } else {
                propertiesReturn.put("NOT_A_FILE", String.format("Given file %s is not really a file...", strFileName));
            }
        } else {
            propertiesReturn.put("DOES_NOT_EXIST", String.format("Given file %s does NOT exist!", strFileName));
        }
        return propertiesReturn;
    }

    /**
     * Getting current project folder
     * 
     * @return
     */
    public static String getCurrentProjectRootFolder() {
        String strFolder = "";
        final File directory = new File("");// parameter is empty
        try {
            strFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            final String strFeedback = String.format("Error encountered... %s", ex.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return strFolder;
    }

    /**
     * Getting current user
     * 
     * @return
     */
    public static File getCurrentUserFolder() {
        return new File(System.getProperty("user.home"));
    }

    /**
     * Get file content into String
     * 
     * @param strFileName
     * @return String
     */
    public static String getFileContentIntoString(final String strFileName) {
        String strFeedback = String.format("Attempting to get File Content into a String from %s file...", strFileName);
        LogHandlingClass.LOGGER.debug(strFeedback);
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            strFeedback = String.format("Error when attempting to get content of file \"%s\": %s", strFileName, e.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return strReturn;
    }

    /**
     * read Main configuration file
     * 
     * @return String
     */
    public static String getJsonConfigurationFile(final String strFilePattern) {
        final Properties propsFile = new Properties();
        propsFile.put("Minified", String.format(strFilePattern, "min."));
        propsFile.put("PreetyPrint", String.format(strFilePattern, ""));
        String strFileJson = null;
        final Properties propsMinified = checkFileExistanceAndReadability(propsFile.getProperty("Minified"));
        for(final Entry<Object, Object> eMinified : propsMinified.entrySet()) {
            final Boolean isItOk = "OK".equals(eMinified.getKey()); 
            if (isItOk) {
                strFileJson = eMinified.getValue().toString();
            } else {
                final Properties propsPreety = checkFileExistanceAndReadability(propsFile.getProperty("PreetyPrint"));
                for(final Entry<Object, Object> ePreety : propsPreety.entrySet()) {
                    final Boolean isItOk2 = "OK".equals(ePreety.getKey()); 
                    if (isItOk2) {
                        strFileJson = ePreety.getValue().toString();
                    } else {
                        throw new IllegalArgumentException("No configuration file found!");
                    }
                }
            }
        }
        return strFileJson;
    }

    /**
     * Get list of files from a given folder
     * 
     * @param strFolderName
     * @param strExtension
     * @return
     */
    public static List<String> getSpecificFilesFromFolder(final String strFolderName, final String strExtension) {
        String strFeedback = String.format("Will attempt to get all files with \"%s\" extensions from within \"%s\" folder...", strExtension, strFolderName);
        LogHandlingClass.LOGGER.debug(strFeedback);
        final List<String> arrayFiles = new ArrayList<>();
        final Path directory = Paths.get(strFolderName);
        // use DirectoryStream to list files which are present in specific
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            // with forEach loop get all the path of files present in directory  
            for (final Path file : stream) {
                if (file.getFileName().toString().endsWith(strExtension)) {
                    final String strFile  = file.getParent().toString()
                            + File.separator + file.getFileName().toString();
                    arrayFiles.add(strFile);
                    strFeedback = String.format("Found a file with %s extension: %s", strExtension, strFile);
                    LogHandlingClass.LOGGER.debug(strFeedback);
                }
            }
        } catch (IOException ex) {
            strFeedback = String.format("Error encountered when attempting to get %s file(s) from %s folder... %s", strExtension, strFolderName, ex.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
        return arrayFiles;
    }

    /**
     * Archives single file to new location
     * 
     * @param strFileName
     * @param strDestFolder
     */
    public static void moveFileToNewLocation(final String strFileName, final String strDestFolder) {
        try {
            final File strSourceFile = new File(strFileName); 
            final File strDestFile = new File(strDestFolder);
            LogHandlingClass.LOGGER.info("Will attempt to move \"%s\" file to \"%s\" folder", strFileName, strDestFolder);
            org.apache.commons.io.FileUtils.moveFileToDirectory(strSourceFile, strDestFile, true);
            LogHandlingClass.LOGGER.info("Success file \"%s\" has been moved to \"%s\" folder", strFileName, strDestFolder);
        } catch (IOException ex) {
            final String strFeedback = String.format("Error when attempting to move \"%s\" file to \"%s\": %s", strFileName, strDestFolder, ex.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Removes a files if already exists
     * 
     * @param strFileName
     */
    private static void removeFileIfExists(final String strFileName) {
        try {
            final Path filePath = Paths.get(strFileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            final String strFeedback = String.format("Error encountered when attempting to write to %s file(s) ... %s", strFileName, e.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    /**
     * Write list of single values to File
     * 
     * @param listStrings
     * @param strFileName
     */
    public static void writeListToTextFile(final List<String> listStrings, final String strFileName) {
        removeFileIfExists(strFileName);
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            listStrings.forEach(strLine -> {
                try {
                    bwr.write(strLine);
                    bwr.newLine();
                } catch (IOException er) {
                    final String strFeedback = String.format("Error encountered when attempting to write to %s file(s) ... %s", strFileName, er.getStackTrace().toString());
                    LogHandlingClass.LOGGER.error(strFeedback);
                }
            });
            final String strFeedback = String.format("Writing list to to %s file(s) completed successfuly!", strFileName);
            LogHandlingClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format("Error encountered when attempting to write to %s file(s) ... %s", strFileName, ex.getStackTrace().toString());
            LogHandlingClass.LOGGER.error(strFeedback);
        }
    }

    // Private constructor to prevent instantiation
    private FileHandlingClass() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

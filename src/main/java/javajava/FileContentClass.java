package javajava;
/* Java IO classes */
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
/* Java NIO classes */
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
/* Java util classes */
import java.util.Arrays;
import java.util.List;
/* Logging */
import org.apache.logging.log4j.Level;

/**
 * File content logic
 */
public final class FileContentClass {

    /**
     * Get file content into String
     * 
     * @param strFileName file name
     * @return String
     */
    @SuppressWarnings("unused")
    public static String getFileContentIntoString(final String strFileName) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        String strReturn = "";
        try {
            strReturn = new String(Files.readAllBytes(Paths.get(strFileName)));
        } catch (IOException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.WARN)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentError"), strFileName, Arrays.toString(e.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return strReturn;
    }

    /**
     * Get file content into InputStream
     * 
     * @param strFileName file name
     * @return input stream
     */
    public static InputStream getIncludedFileContentIntoInputStream(final String strFileName) {
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoString"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); // NOPMD by E303778 on 30.04.2025, 15:47
        final InputStream inputStream = classLoader.getResourceAsStream(strFileName); // NOPMD by E303778 on 30.04.2025, 15:47
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoStreamSuccess"), strFileName);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return inputStream;
    }

    /**
     * Write list of single values to File
     * 
     * @param listStrings List of Strings
     * @param strFileName file name to write to
     */
    public static void writeListToTextFile(final List<String> listStrings, final String strFileName) {
    	FileHandlingClass.removeFileIfExists(strFileName);
        try (BufferedWriter bwr = Files.newBufferedWriter(Paths.get(strFileName), StandardCharsets.UTF_8)) {
            listStrings.forEach(strLine -> {
                try {
                    bwr.write(strLine);
                    bwr.newLine();
                } catch (IOException er) {
                    if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                        final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(er.getStackTrace()));
                        LoggerLevelProvider.LOGGER.error(strFeedback);
                    }
                }
            });
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingSuccess"), strFileName);
                LoggerLevelProvider.LOGGER.debug(strFeedback);
            }
        } catch (IOException ex) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileWritingError"), strFileName, Arrays.toString(ex.getStackTrace()));
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
    }

    /**
     * Constructor
     */
    private FileContentClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }

}

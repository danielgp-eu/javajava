package project;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Project related goodies
 */
public final class ProjectClass {

    /**
     * establish current POM file
     * @return String
     */
    private static String getCurrentProjectObjectModelFile() {
        final StringBuilder strPomFile = new StringBuilder(getProjectFolder());
        final PropertiesReaderClass reader;
        try {
            reader = new PropertiesReaderClass("/project.properties");
            if (isRunningFromJar()) {
                strPomFile.append(String.format("/%s-%s.pom",
                        reader.getProperty("artifactId"),
                        reader.getProperty("version")));
            } else {
                strPomFile.append(File.separator).append("pom.xml");
            }
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), strPomFile);
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), "pom.xml", Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strPomFile.toString();
    }

    /**
     * get POM into Model
     * @return Model
     */
    public static Model getProjectObjectModelFileIntoModel() {
        final String pomFile = getCurrentProjectObjectModelFile();
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try(BufferedReader bReader = Files.newBufferedReader(Path.of(pomFile), StandardCharsets.UTF_8)) {
            model = reader.read(bReader);
        } catch (IOException | XmlPullParserException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nErrorOnGettingDependencies"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return model;
    }

    /**
     * Getting current project folder
     * @return application folder
     */
    public static String getProjectFolder() {
        String strAppFolder = null;
        final File directory = new File(""); // parameter is empty
        try {
            strAppFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFolderError"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strAppFolder;
    }

    /**
     * detects if current execution is from JAR or not
     * @return boolean
     */
    public static boolean isRunningFromJar() {
        // Get the URL of the current class's bytecode
        final URL classUrl = ProjectClass.class.getResource("ProjectClass.class");
        if (classUrl == null) {
            throw new IllegalStateException("Class resource not found");
        }
        // Check if the protocol is "jar" (JAR execution) or "file" (IDE execution)
        final String protocol = classUrl.getProtocol();
        return "jar".equals(protocol);
    }

    private ProjectClass () {
        // intentionally left blank
    }

}

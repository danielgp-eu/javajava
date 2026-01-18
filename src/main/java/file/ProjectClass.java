package file;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Project related goodies
 */
public final class ProjectClass {

    /**
     * establish current POM file
     * @return String
     */
    public static String getCurrentProjectObjectModelFile() {
        final StringBuilder strPomFile = new StringBuilder(getProjectFolder());
        if (isRunningFromJar()) {
            final String[] varsToPick = {"artifactId", "version"};
            final Properties svProperties = getVariableFromProjectProperties(varsToPick);
            strPomFile.append(String.format("/%s-%s.pom",
                    svProperties.getProperty("artifactId"),
                    svProperties.getProperty("version")));
        } else {
            strPomFile.append(File.separator).append("pom.xml");
        }
        return strPomFile.toString();
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
     * get variable
     * @param strVariables variables to pick
     * @return Properties
     */
    public static Properties getVariableFromProjectProperties(final String... strVariables) {
        final Properties svProperties = new Properties();
        final String prjProps = "/project.properties";
        try {
            final PropertiesReaderClass reader = new PropertiesReaderClass(prjProps);
            final List<String> arrayVariables = Arrays.asList(strVariables);
            arrayVariables.forEach(crtVariable -> svProperties.put(crtVariable, reader.getProperty(crtVariable)));
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), svProperties.toString());
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFindingError"), prjProps, Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return svProperties;
    }

    /**
     * detects if current execution is from JAR or not
     * @return boolean
     */
    public static boolean isRunningFromJar() {
        // Get the URL of the current class's byte-code
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

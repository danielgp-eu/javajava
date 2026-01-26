package file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Project Properties
 */
public final class ProjectPropertiesClass {

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
    private ProjectPropertiesClass () {
        // intentionally left blank
    }

}

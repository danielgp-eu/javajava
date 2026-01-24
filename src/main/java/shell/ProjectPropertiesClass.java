package shell;

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
    public static Properties getVariableFromProjectProperties(final String... strVariables) {
        final Properties svProperties = new Properties();
        final String prjProps = "/project.properties";
        try {
            final PropertiesReaderClass reader = new PropertiesReaderClass(prjProps);
            final List<String> arrayVariables = Arrays.asList(strVariables);
            arrayVariables.forEach(crtVariable -> svProperties.put(crtVariable, reader.getProperty(crtVariable)));
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileContentIntoStreamSuccess"), svProperties.toString());
            LogExposureClass.LOGGER.debug(strFeedback);
        } catch (IOException ei) {
            final Path ptPrjProps = Path.of(prjProps);
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

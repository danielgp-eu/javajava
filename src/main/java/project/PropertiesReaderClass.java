package project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import log.LogExposure;

/**
 * Project Properties Reader
 */
public class PropertiesReaderClass {
    /**
     * Project Properties
     */
    private final Properties properties;

    /**
     * Reading Project Properties
     * @param propertyFileName Property file name
     * @throws IOException if something goes wrong
     */
    public PropertiesReaderClass(final String propertyFileName) throws IOException {
        this.properties = new Properties();
        try(InputStream inputStream = PropertiesReaderClass.class.getResourceAsStream(propertyFileName)) {
            this.properties.load(inputStream);
        } catch (IOException ex) {
            LogExposure.exposeMessageToErrorLog(String.format("IO exception on getting %s resource... %s", propertyFileName, Arrays.toString(ex.getStackTrace())));
        }
    }

    /**
     * Constructor
     * @param propertyName Property name
     * @return single Property value based on key
     */
    public String getProperty(final String propertyName) {
        return this.properties.getProperty(propertyName);
    }

}

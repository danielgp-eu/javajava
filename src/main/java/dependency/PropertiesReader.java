package dependency;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Project Properties Reader
 */
public class PropertiesReader {
    /**
     * Project Properties
     */
    private final Properties properties;

    /**
     * Reading
     * @param propertyFileName
     * @throws IOException
     */
    public PropertiesReader(final String propertyFileName) throws IOException {
        final ClassLoader cLoader = Thread.currentThread().getContextClassLoader();
        final InputStream iStream = cLoader.getResourceAsStream(propertyFileName);
        this.properties = new Properties();
        this.properties.load(iStream);
    }

    /**
     * Constructor
     * @param propertyName
     * @return
     */
    public String getProperty(final String propertyName) {
        return this.properties.getProperty(propertyName);
    }
}

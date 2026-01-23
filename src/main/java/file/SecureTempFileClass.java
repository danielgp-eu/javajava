package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * secure way to handle temporary files
 */
public final class SecureTempFileClass implements AutoCloseable {
    /**
     * internal temporary file
     */
    private final Path tempFile;

    /**
     * 
     * @param prefix
     * @param suffix
     * @throws IOException
     */
    private SecureTempFileClass(final String prefix, final String suffix) throws IOException {
        this.tempFile = Files.createTempFile(prefix, suffix);
    }

    /**
     * create helper
     * @param prefix for the file
     * @param suffix for the file
     * @return SecureTempFileClass
     * @throws IOException
     */
    public static SecureTempFileClass create(final String prefix, final String suffix) throws IOException {
        return new SecureTempFileClass(prefix, suffix);
    }

    /**
     * getting path of the file
     * @return
     */
    public Path getPath() {
        return tempFile;
    }

    /**
     * Closer
     */
    @Override
    public void close() throws IOException {
        Files.deleteIfExists(tempFile);
    }

}

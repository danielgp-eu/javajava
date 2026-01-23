package file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * secure way to handle temporary files
 */
public final class SecureTempFileClass implements AutoCloseable {
    /**
     * internal temporary file
     */
    private final Path tempFile;
    /**
     * internal temporary folder
     */
    private final Path secureDir;

    /**
     * 
     * @param prefix
     * @param suffix
     * @throws IOException
     */
    private SecureTempFileClass(final String prefix, final String suffix) throws IOException {
        FileAttribute<?> attr = null;
        if (!System.getProperty("os.name").contains("Win")) {
            final Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
            attr = PosixFilePermissions.asFileAttribute(perms);
        }
        secureDir = (attr != null) ? Files.createTempDirectory("secureDir", attr)
                                   : Files.createTempDirectory("secureDir");
        this.tempFile = Files.createTempFile(secureDir, prefix, suffix);
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
        Files.deleteIfExists(secureDir);
    }

}

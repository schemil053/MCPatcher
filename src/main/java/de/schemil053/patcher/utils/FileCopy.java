package de.schemil053.patcher.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

public class FileCopy {
    private static FileSystem fileSystem;

    public static void copyFile(File orig, File copy)
          throws IOException {
            try (
                    InputStream in = new BufferedInputStream(
                            new FileInputStream(orig));
                    OutputStream out = new BufferedOutputStream(
                            new FileOutputStream(copy))) {

                byte[] buffer = new byte[1024];
                int lengthRead;
                while ((lengthRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, lengthRead);
                    out.flush();
                }
            }
    }

    private static void setUp() {
        if(fileSystem != null) {
            if(!fileSystem.isOpen()) {
                return;
            }
        }
        try {
            URI resource = FileCopy.class.getResource("").toURI();
            fileSystem = FileSystems.newFileSystem(
                    resource,
                    Collections.<String, String>emptyMap()
            );
        } catch (Exception exception) {

        }
    }
    public boolean canCopyFromJar(String source) {
        try {
            setUp();
            return fileSystem.getPath(source) != null;
        } catch (Exception exception) {
            return false;
        }
    }
    public void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
        setUp();
        final Path jarPath = fileSystem.getPath(source);

        Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {

            private Path currentTarget;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                currentTarget = target.resolve(jarPath.relativize(dir).toString());
                Files.createDirectories(currentTarget);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

        });
    }
}

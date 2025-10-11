package org.example.herizon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Defines the base directory for persisted uploads and ensures it exists.
 */
@Component
public class FileStorageProperties {

    private final Path uploadDir;

    public FileStorageProperties(@Value("${file.upload-dir:uploads}") String uploadDir) {
        Path basePath = Paths.get(uploadDir);
        if (!basePath.isAbsolute()) {
            basePath = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        }
        this.uploadDir = basePath.normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create upload directory: " + this.uploadDir, e);
        }
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public String getUploadDirResourceLocation() {
        return uploadDir.toUri().toString();
    }
}

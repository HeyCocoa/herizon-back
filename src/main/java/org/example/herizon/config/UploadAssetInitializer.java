package org.example.herizon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Copies bundled static upload assets into the writable uploads directory so that
 * pre-seeded data referencing those files can be resolved in fresh deployments.
 */
@Component
public class UploadAssetInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UploadAssetInitializer.class);
    private static final String CLASSPATH_PATTERN = "classpath:/static/uploads/**/*.*";
    private static final String STATIC_UPLOADS_PREFIX = "/static/uploads/";

    private final FileStorageProperties fileStorageProperties;
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public UploadAssetInitializer(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Resource[] resources = resourceResolver.getResources(CLASSPATH_PATTERN);
        Path targetRoot = fileStorageProperties.getUploadDir();

        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            String relativePath = extractRelativePath(resource);
            if (relativePath == null) {
                continue;
            }

            Path targetFile = targetRoot.resolve(relativePath);
            if (Files.exists(targetFile)) {
                continue;
            }

            Files.createDirectories(targetFile.getParent());
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, targetFile);
                log.info("Copied default upload asset to {}", targetFile);
            } catch (IOException ex) {
                log.warn("Failed to copy default asset {}: {}", relativePath, ex.getMessage());
            }
        }
    }

    private String extractRelativePath(Resource resource) throws IOException {
        String url = resource.getURL().toString();
        int idx = url.indexOf(STATIC_UPLOADS_PREFIX);
        if (idx == -1) {
            return null;
        }
        return url.substring(idx + STATIC_UPLOADS_PREFIX.length());
    }
}
